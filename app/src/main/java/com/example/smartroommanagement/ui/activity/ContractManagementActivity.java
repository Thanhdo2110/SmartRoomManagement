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
import com.example.smartroommanagement.databinding.ActivityContractManagementBinding;
import com.example.smartroommanagement.databinding.DialogContractDetailBinding;
import com.example.smartroommanagement.ui.adapter.ContractAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ContractManagementActivity extends AppCompatActivity {
    private ActivityContractManagementBinding binding;
    private RoomDetailViewModel viewModel;
    private ContractAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContractManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupViewModel();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Quản lý hợp đồng");
        }

        adapter = new ContractAdapter();
        binding.recyclerViewContracts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewContracts.setAdapter(adapter);

        adapter.setOnContractClickListener(item -> {
            if (item.tenants != null && !item.tenants.isEmpty()) {
                showContractDetailDialog(item.tenants.get(0), item.room);
            } else {
                Toast.makeText(this, "Phòng trống, chưa có hợp đồng để hiển thị", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
            viewModel.getRoomsWithTenants().observe(this, rooms -> {
                if (rooms != null) {
                    adapter.submitList(rooms);
                    binding.textNoContracts.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showContractDetailDialog(TenantEntity tenant, RoomEntity room) {
        try {
            DialogContractDetailBinding cb = DialogContractDetailBinding.inflate(getLayoutInflater());
            
            AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                    .setView(cb.getRoot())
                    .create();

            SimpleDateFormat sdf = new SimpleDateFormat("'Hôm nay, ngày' dd 'tháng' MM 'năm' yyyy", new Locale("vi", "VN"));
            cb.textContractDate.setText(sdf.format(new Date()));

            cb.textContractTenantName.setText("Ông/Bà: " + (tenant.getName() != null ? tenant.getName().toUpperCase() : "---"));
            cb.textContractTenantId.setText("CCCD số: " + (TextUtils.isEmpty(tenant.getIdentityCard()) ? "...................." : tenant.getIdentityCard()));
            cb.textContractTenantPhone.setText("Số điện thoại: " + (tenant.getPhone() != null ? tenant.getPhone() : "---"));
            cb.textContractTenantHometown.setText("Quê quán: " + (TextUtils.isEmpty(tenant.getHometown()) ? "...................." : tenant.getHometown().toUpperCase()));

            cb.textContractRoomName.setText("1. Bên A đồng ý cho bên B thuê phòng số: " + (room.getName() != null ? room.getName().toUpperCase() : "---"));
            cb.textContractPrice.setText("2. Giá thuê phòng: " + FinanceUtils.formatCurrency(room.getBasePrice()) + "/tháng");
            cb.textContractDeposit.setText("3. Tiền đặt cọc: " + FinanceUtils.formatCurrency(tenant.getDeposit()));
            cb.textContractStartDate.setText("4. Thời hạn thuê bắt đầu từ ngày: " + (TextUtils.isEmpty(tenant.getStartDate()) ? "...................." : tenant.getStartDate()));

            int term = tenant.getContractTerm() != null ? tenant.getContractTerm() : 12;
            cb.textContractTermInfo.setText("5. Thời hạn hợp đồng: " + term + " tháng. Nếu bên B trả phòng trước thời hạn trên sẽ mất toàn bộ số tiền đặt cọc đã nêu ở mục 3.");

            cb.btnCloseContract.setOnClickListener(v -> dialog.dismiss());
            cb.btnShareContract.setOnClickListener(v -> shareViewAsImage(cb.cardContractPaper));

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViewAsImage(View view) {
        view.post(() -> {
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
        });
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
