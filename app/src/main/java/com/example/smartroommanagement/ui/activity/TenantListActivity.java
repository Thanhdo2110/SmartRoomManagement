package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.TenantWithRoom;
import com.example.smartroommanagement.databinding.ActivityTenantListBinding;
import com.example.smartroommanagement.databinding.DialogAddTenantBinding;
import com.example.smartroommanagement.databinding.DialogContractDetailBinding;
import com.example.smartroommanagement.ui.adapter.TenantAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TenantListActivity extends AppCompatActivity {
    private ActivityTenantListBinding binding;
    private RoomDetailViewModel viewModel;
    private TenantAdapter adapter;
    private List<TenantWithRoom> fullTenantList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTenantListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupViewModel();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Quản lý khách thuê");
        }

        adapter = new TenantAdapter();
        binding.recyclerViewTenants.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTenants.setAdapter(adapter);

        adapter.setOnTenantMoreClickListener(this::showTenantOptionsDialog);

        adapter.setOnTenantClickListener(tenantWithRoom -> {
            Intent intent = new Intent(this, TenantDetailActivity.class);
            intent.putExtra(TenantDetailActivity.EXTRA_TENANT_ID, tenantWithRoom.tenant.getId());
            if (tenantWithRoom.room != null) {
                intent.putExtra(TenantDetailActivity.EXTRA_ROOM_ID, tenantWithRoom.room.getId());
            }
            startActivity(intent);
        });

        // Xử lý tìm kiếm
        binding.editSearchTenant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTenants(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTenants(String query) {
        if (TextUtils.isEmpty(query)) {
            adapter.submitList(new ArrayList<>(fullTenantList));
            binding.layoutEmptyState.setVisibility(fullTenantList.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        String lowerCaseQuery = removeAccents(query.toLowerCase().trim());
        List<TenantWithRoom> filteredList = fullTenantList.stream().filter(item -> {
            String tenantName = removeAccents(item.tenant.getName().toLowerCase());
            String phone = item.tenant.getPhone() != null ? item.tenant.getPhone() : "";
            String identity = item.tenant.getIdentityCard() != null ? item.tenant.getIdentityCard() : "";
            String roomName = item.room != null ? removeAccents(item.room.getName().toLowerCase()) : "";

            return tenantName.contains(lowerCaseQuery) || 
                   phone.contains(lowerCaseQuery) || 
                   identity.contains(lowerCaseQuery) || 
                   roomName.contains(lowerCaseQuery);
        }).collect(Collectors.toList());

        adapter.submitList(filteredList);
        binding.layoutEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String removeAccents(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        viewModel.getAllTenantsWithRoom().observe(this, tenants -> {
            if (tenants != null) {
                fullTenantList = new ArrayList<>(tenants);
                filterTenants(binding.editSearchTenant.getText().toString());
            }
        });
    }

    private void showTenantOptionsDialog(TenantWithRoom item) {
        String[] options = {"Xem hợp đồng", "Chỉnh sửa thông tin", "Xóa khách thuê"};
        new AlertDialog.Builder(this)
            .setTitle("Tùy chọn: " + item.tenant.getName())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    if (item.room != null) {
                        showContractDetailDialog(item.tenant, item.room);
                    } else {
                        Toast.makeText(this, "Chưa gán phòng cho khách này", Toast.LENGTH_SHORT).show();
                    }
                } else if (which == 1) {
                    showEditTenantDialog(item.tenant);
                } else {
                    confirmDeleteTenant(item.tenant, item.room);
                }
            })
            .show();
    }

    private void showContractDetailDialog(TenantEntity tenant, RoomEntity room) {
        DialogContractDetailBinding cb = DialogContractDetailBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(cb.getRoot())
                .create();

        SimpleDateFormat sdf = new SimpleDateFormat("'Hôm nay, ngày' dd 'tháng' MM 'năm' yyyy", new Locale("vi", "VN"));
        cb.textContractDate.setText(sdf.format(new Date()));
        cb.textContractTenantName.setText("Ông/Bà: " + tenant.getName());
        cb.textContractTenantId.setText("CCCD số: " + (TextUtils.isEmpty(tenant.getIdentityCard()) ? "...................." : tenant.getIdentityCard()));
        cb.textContractTenantPhone.setText("Số điện thoại: " + tenant.getPhone());
        cb.textContractTenantHometown.setText("Quê quán: " + (TextUtils.isEmpty(tenant.getHometown()) ? "...................." : tenant.getHometown()));
        cb.textContractRoomName.setText("1. Bên A đồng ý cho bên B thuê phòng số: " + room.getName());
        cb.textContractPrice.setText("2. Giá thuê phòng: " + FinanceUtils.formatCurrency(room.getBasePrice()) + "/tháng");
        cb.textContractDeposit.setText("3. Tiền đặt cọc: " + FinanceUtils.formatCurrency(tenant.getDeposit()));
        cb.textContractStartDate.setText("4. Thời hạn thuê bắt đầu từ ngày: " + (TextUtils.isEmpty(tenant.getStartDate()) ? "...................." : tenant.getStartDate()));

        int term = tenant.getContractTerm() != null ? tenant.getContractTerm() : 12;
        cb.textContractTermInfo.setText("5. Thời hạn hợp đồng: " + term + " tháng. Nếu bên B trả phòng trước thời hạn trên sẽ mất toàn bộ số tiền đặt cọc đã nêu ở mục 3.");

        cb.btnCloseContract.setOnClickListener(v -> dialog.dismiss());
        cb.btnShareContract.setOnClickListener(v -> shareViewAsImage(cb.cardContractPaper));
        dialog.show();
    }

    private void shareViewAsImage(View view) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            File cachePath = new File(getExternalCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "contract_" + System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hợp đồng:"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showEditTenantDialog(TenantEntity tenant) {
        DialogAddTenantBinding db = DialogAddTenantBinding.inflate(getLayoutInflater());
        FinanceUtils.addCurrencyFormatter(db.editTenantDeposit);
        db.editTenantName.setText(tenant.getName());
        db.editTenantPhone.setText(tenant.getPhone());
        db.editTenantIdentity.setText(tenant.getIdentityCard());
        db.editTenantDeposit.setText(String.valueOf((int)tenant.getDeposit()));
        db.editStartDate.setText(tenant.getStartDate());
        db.editTenantHometown.setText(tenant.getHometown());
        db.editTenantBirthdate.setText(tenant.getBirthDate());
        if (tenant.getContractTerm() != null) db.editContractTerm.setText(String.valueOf(tenant.getContractTerm()));

        new AlertDialog.Builder(this)
            .setTitle("Cập nhật thông tin")
            .setView(db.getRoot())
            .setPositiveButton("Lưu", (d, w) -> {
                // SỬ DỤNG COPY CONSTRUCTOR để tạo đối tượng mới hoàn toàn
                // Giúp DiffUtil nhận diện thay đổi ngay lập tức
                TenantEntity updatedTenant = new TenantEntity(tenant);
                
                updatedTenant.setName(db.editTenantName.getText().toString().trim());
                updatedTenant.setPhone(db.editTenantPhone.getText().toString().trim());
                updatedTenant.setIdentityCard(db.editTenantIdentity.getText().toString().trim());
                updatedTenant.setBirthDate(db.editTenantBirthdate.getText().toString().trim());
                updatedTenant.setHometown(db.editTenantHometown.getText().toString().trim());
                updatedTenant.setStartDate(db.editStartDate.getText().toString().trim());
                updatedTenant.setDeposit(FinanceUtils.parseFormattedCurrency(db.editTenantDeposit.getText().toString()));
                
                String termStr = db.editContractTerm.getText().toString();
                updatedTenant.setContractTerm(FinanceUtils.parseIntegerOrDefault(termStr, 12));

                viewModel.updateTenant(updatedTenant);
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void confirmDeleteTenant(TenantEntity tenant, RoomEntity room) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa khách thuê " + tenant.getName() + "?")
            .setPositiveButton("Xóa", (d, w) -> {
                viewModel.deleteTenant(tenant);
                if (room != null) {
                    viewModel.hasTenantsInRoom(room.getId()).observe(this, hasTenants -> {
                        if (!Boolean.TRUE.equals(hasTenants)) {
                            room.setStatus("Trống");
                            viewModel.updateRoom(room);
                        }
                    });
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
