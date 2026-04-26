package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.smartroommanagement.databinding.ActivityTenantListBinding;
import com.example.smartroommanagement.databinding.DialogContractDetailBinding;
import com.example.smartroommanagement.ui.adapter.TenantAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TenantListActivity extends AppCompatActivity {
    private ActivityTenantListBinding binding;
    private RoomDetailViewModel viewModel;
    private TenantAdapter adapter;

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

        adapter.setOnTenantClickListener(tenantWithRoom -> {
            showTenantOptionsDialog(tenantWithRoom.tenant, tenantWithRoom.room);
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        viewModel.getAllTenantsWithRoom().observe(this, tenants -> adapter.submitList(tenants));
    }

    private void showTenantOptionsDialog(TenantEntity tenant, RoomEntity room) {
        String[] options = {"Xem hợp đồng", "Xem chi tiết phòng", "Chỉnh sửa thông tin"};
        new AlertDialog.Builder(this)
            .setTitle("Tùy chọn: " + tenant.getName())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    if (room != null) {
                        showContractDetailDialog(tenant, room);
                    } else {
                        Toast.makeText(this, "Khách thuê chưa được gán phòng!", Toast.LENGTH_SHORT).show();
                    }
                } else if (which == 1) {
                    if (tenant.getRoomId() != null) {
                        Intent intent = new Intent(this, RoomDetailActivity.class);
                        intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, (int)tenant.getRoomId());
                        startActivity(intent);
                    }
                } else if (which == 2) {
                    // Logic chỉnh sửa thông tin (nếu cần)
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
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hợp đồng qua:"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể chia sẻ hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
