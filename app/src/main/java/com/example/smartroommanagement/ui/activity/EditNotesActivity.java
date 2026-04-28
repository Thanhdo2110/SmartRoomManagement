package com.example.smartroommanagement.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.entity.NoteEntity;
import com.example.smartroommanagement.databinding.ActivityEditNotesBinding;
import com.example.smartroommanagement.ui.adapter.NoteAdapter;
import com.example.smartroommanagement.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EditNotesActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener {
    private ActivityEditNotesBinding binding;
    private NoteAdapter adapter;
    private SessionManager sessionManager;
    private int userId;
    private List<NoteEntity> allNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        loadNotes();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(this);
        binding.recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotes.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchNotes.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });
    }

    private void filterNotes(String query) {
        if (query.isEmpty()) {
            adapter.setNotes(allNotes);
        } else {
            List<NoteEntity> filtered = allNotes.stream()
                    .filter(note -> note.getContent().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.setNotes(filtered);
        }
    }

    private void setupListeners() {
        binding.fabAddNote.setOnClickListener(v -> showNoteDialog(null));
    }

    private void loadNotes() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            allNotes = AppDatabase.getDatabase(this).noteDao().getNotesByUserId(userId);
            runOnUiThread(() -> {
                adapter.setNotes(allNotes);
                binding.layoutEmpty.setVisibility(allNotes.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showNoteDialog(NoteEntity note) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_note, null);
        TextInputEditText editContent = view.findViewById(R.id.edit_note_content);
        TextView txtTitle = view.findViewById(R.id.dialog_title);

        if (note != null) {
            txtTitle.setText("Chỉnh sửa ghi chú");
            editContent.setText(note.getContent());
        }

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton(note == null ? "Thêm" : "Cập nhật", (dialog, which) -> {
                    String content = editContent.getText().toString().trim();
                    if (content.isEmpty()) {
                        Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveNote(note, content);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveNote(NoteEntity note, String content) {
        String timestamp = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (note == null) {
                // Thêm mới
                NoteEntity newNote = new NoteEntity(userId, content, timestamp);
                AppDatabase.getDatabase(this).noteDao().insert(newNote);
            } else {
                // Cập nhật
                note.setContent(content);
                note.setTimestamp(timestamp + " (đã sửa)");
                AppDatabase.getDatabase(this).noteDao().update(note);
            }
            loadNotes();
            runOnUiThread(() -> Toast.makeText(this, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDeleteClick(NoteEntity note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(this).noteDao().delete(note);
                        loadNotes();
                        runOnUiThread(() -> Toast.makeText(this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onNoteClick(NoteEntity note) {
        showNoteDialog(note);
    }
}
