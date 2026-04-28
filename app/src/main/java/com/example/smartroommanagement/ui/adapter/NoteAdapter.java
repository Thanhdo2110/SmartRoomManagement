package com.example.smartroommanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.entity.NoteEntity;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<NoteEntity> notes = new ArrayList<>();
    private OnNoteListener listener;

    public interface OnNoteListener {
        void onDeleteClick(NoteEntity note);
        void onNoteClick(NoteEntity note);
    }

    public NoteAdapter(OnNoteListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<NoteEntity> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity note = notes.get(position);
        holder.txtContent.setText(note.getContent());
        holder.txtTime.setText(note.getTimestamp());

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(note);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView txtContent, txtTime;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtContent = itemView.findViewById(R.id.txt_note_content);
            txtTime = itemView.findViewById(R.id.txt_note_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_note);
        }
    }
}
