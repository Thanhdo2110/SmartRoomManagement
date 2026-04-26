package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.databinding.ActivityRoomListBinding;
import com.example.smartroommanagement.databinding.DialogAddRoomBinding;
import com.example.smartroommanagement.ui.adapter.RoomAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomViewModel;
import com.example.smartroommanagement.util.FinanceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomListActivity extends AppCompatActivity {
    private ActivityRoomListBinding binding;
    private RoomViewModel viewModel;
    private RoomAdapter adapter;
    private List<RoomEntity> allRooms = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentFilterStatus = "All"; // "All", "Occupied", "Vacant"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupViewModel();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        adapter = new RoomAdapter();
        binding.recyclerViewRooms.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRooms.setAdapter(adapter);

        adapter.setOnRoomClickListener(room -> {
            Intent intent = new Intent(this, RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.getId());
            startActivity(intent);
        });

        adapter.setOnRoomLongClickListener(this::showRoomOptionsDialog);

        binding.fabAddRoom.setOnClickListener(v -> showRoomDialog(null));
        binding.btnAddFirstRoom.setOnClickListener(v -> showRoomDialog(null));

        // Thiết lập chức năng tìm kiếm
        binding.editSearchRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                filterRooms();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Thiết lập lọc theo Category
        binding.cardFilterAll.setOnClickListener(v -> {
            currentFilterStatus = "All";
            updateFilterUI();
            filterRooms();
        });

        binding.cardFilterOccupied.setOnClickListener(v -> {
            currentFilterStatus = "Occupied";
            updateFilterUI();
            filterRooms();
        });

        binding.cardFilterVacant.setOnClickListener(v -> {
            currentFilterStatus = "Vacant";
            updateFilterUI();
            filterRooms();
        });

        // Khởi tạo UI filter ban đầu
        updateFilterUI();
    }

    private void updateFilterUI() {
        // Màu mặc định (Chưa chọn)
        int defaultStroke = Color.parseColor("#F1F5F9");
        int defaultBg = Color.parseColor("#FFFFFF");
        int defaultText = Color.parseColor("#64748B");

        // Màu Indigo (Chọn "Tất cả" hoặc "Đã thuê")
        int indigoStroke = Color.parseColor("#6366F1");
        int indigoBg = Color.parseColor("#EEF2FF");
        int indigoText = Color.parseColor("#6366F1");

        // Màu Green (Chọn "Trống")
        int greenStroke = Color.parseColor("#10B981");
        int greenBg = Color.parseColor("#ECFDF5");
        int greenText = Color.parseColor("#10B981");

        // Reset tất cả về mặc định
        binding.cardFilterAll.setStrokeColor(defaultStroke);
        binding.cardFilterAll.setCardBackgroundColor(defaultBg);
        binding.textAllLabel.setTextColor(defaultText);

        binding.cardFilterOccupied.setStrokeColor(defaultStroke);
        binding.cardFilterOccupied.setCardBackgroundColor(defaultBg);
        binding.textOccupiedCount.setTextColor(defaultText);
        binding.labelOccupied.setTextColor(defaultText);

        binding.cardFilterVacant.setStrokeColor(defaultStroke);
        binding.cardFilterVacant.setCardBackgroundColor(defaultBg);
        binding.textVacantCount.setTextColor(defaultText);
        binding.labelVacant.setTextColor(defaultText);

        // Highlight mục đang được chọn
        switch (currentFilterStatus) {
            case "All":
                binding.cardFilterAll.setStrokeColor(indigoStroke);
                binding.cardFilterAll.setCardBackgroundColor(indigoBg);
                binding.textAllLabel.setTextColor(indigoText);
                break;
            case "Occupied":
                binding.cardFilterOccupied.setStrokeColor(indigoStroke);
                binding.cardFilterOccupied.setCardBackgroundColor(indigoBg);
                binding.textOccupiedCount.setTextColor(indigoText);
                binding.labelOccupied.setTextColor(indigoText);
                break;
            case "Vacant":
                binding.cardFilterVacant.setStrokeColor(greenStroke);
                binding.cardFilterVacant.setCardBackgroundColor(greenBg);
                binding.textVacantCount.setTextColor(greenText);
                binding.labelVacant.setTextColor(greenText);
                break;
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        viewModel.getAllRooms().observe(this, rooms -> {
            if (rooms != null) {
                allRooms = new ArrayList<>(rooms);
                updateStatistics(rooms);
                filterRooms();
            }
        });
    }

    private void updateStatistics(List<RoomEntity> rooms) {
        long occupied = rooms.stream().filter(r -> "Đã thuê".equalsIgnoreCase(r.getStatus())).count();
        long vacant = rooms.size() - occupied;
        
        binding.textOccupiedCount.setText(String.valueOf(occupied));
        binding.textVacantCount.setText(String.valueOf(vacant));
    }

    private void filterRooms() {
        List<RoomEntity> filteredList = new ArrayList<>(allRooms);

        // 1. Lọc theo Category trạng thái
        if ("Occupied".equals(currentFilterStatus)) {
            filteredList = filteredList.stream()
                    .filter(room -> "Đã thuê".equalsIgnoreCase(room.getStatus()))
                    .collect(Collectors.toList());
        } else if ("Vacant".equals(currentFilterStatus)) {
            filteredList = filteredList.stream()
                    .filter(room -> "Trống".equalsIgnoreCase(room.getStatus()))
                    .collect(Collectors.toList());
        }

        // 2. Lọc theo tìm kiếm tên phòng
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            filteredList = filteredList.stream()
                    .filter(room -> room.getName().toLowerCase().contains(currentSearchQuery))
                    .collect(Collectors.toList());
        }

        adapter.submitList(filteredList);

        // Hiển thị trạng thái trống nếu không có kết quả
        if (filteredList.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewRooms.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(currentSearchQuery) || !"All".equals(currentFilterStatus)) {
                binding.textEmptyTitle.setText("Không tìm thấy kết quả");
                binding.textEmptyDesc.setText("Thử đổi bộ lọc hoặc tìm tên phòng khác");
                binding.btnAddFirstRoom.setVisibility(View.GONE);
            } else {
                binding.textEmptyTitle.setText("Chưa có phòng nào");
                binding.textEmptyDesc.setText("Nhấn nút phía dưới để bắt đầu thêm phòng");
                binding.btnAddFirstRoom.setVisibility(View.VISIBLE);
            }
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewRooms.setVisibility(View.VISIBLE);
        }
    }

    private void showRoomOptionsDialog(RoomEntity room) {
        String[] options = {"Chỉnh sửa phòng", "Xóa phòng"};
        new AlertDialog.Builder(this)
            .setTitle("Tùy chọn phòng " + room.getName())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    showRoomDialog(room);
                } else {
                    confirmDeleteRoom(room);
                }
            })
            .show();
    }

    private void confirmDeleteRoom(RoomEntity room) {
        if (!"Trống".equalsIgnoreCase(room.getStatus())) {
            Toast.makeText(this, "Không thể xóa phòng đang có khách thuê!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa phòng " + room.getName() + "?")
            .setPositiveButton("Xóa", (d, w) -> viewModel.delete(room))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showRoomDialog(RoomEntity existingRoom) {
        DialogAddRoomBinding db = DialogAddRoomBinding.inflate(getLayoutInflater());
        FinanceUtils.addCurrencyFormatter(db.editRoomPrice);
        
        if (existingRoom != null) {
            db.editRoomName.setText(existingRoom.getName());
            db.editRoomPrice.setText(String.valueOf((int)existingRoom.getBasePrice()));
            db.editRoomDescription.setText(existingRoom.getDescription());
        }

        new AlertDialog.Builder(this)
            .setTitle(existingRoom == null ? "Thêm phòng mới" : "Chỉnh sửa phòng")
            .setView(db.getRoot())
            .setPositiveButton("Lưu", (d, w) -> {
                String name = db.editRoomName.getText().toString().trim();
                String priceStr = db.editRoomPrice.getText().toString().trim();
                String description = db.editRoomDescription.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                    Toast.makeText(this, "Vui lòng nhập đủ tên và giá phòng", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double price = FinanceUtils.parseFormattedCurrency(priceStr);
                    if (existingRoom == null) {
                        viewModel.insert(new RoomEntity(name, price, "Trống", description));
                    } else {
                        RoomEntity updatedRoom = new RoomEntity(name, price, existingRoom.getStatus(), description);
                        updatedRoom.setId(existingRoom.getId());
                        viewModel.update(updatedRoom);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi định dạng giá", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
