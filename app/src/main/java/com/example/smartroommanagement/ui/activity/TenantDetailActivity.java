package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.databinding.ActivityTenantDetailBinding;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;

public class TenantDetailActivity extends AppCompatActivity {
    public static final String EXTRA_TENANT_ID = "extra_tenant_id";
    public static final String EXTRA_ROOM_ID = "extra_room_id";

    private ActivityTenantDetailBinding binding;
    private RoomDetailViewModel viewModel;
    private int tenantId;
    private Integer roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTenantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tenantId = getIntent().getIntExtra(EXTRA_TENANT_ID, -1);
        int rId = getIntent().getIntExtra(EXTRA_ROOM_ID, -1);
        if (rId != -1) roomId = rId;

        setupToolbar();
        setupViewModel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        
        // Quan sát danh sách khách thuê để tìm khách hiện tại
        viewModel.getAllTenantsWithRoom().observe(this, tenantsWithRoom -> {
            for (var item : tenantsWithRoom) {
                if (item.tenant.getId() == tenantId) {
                    displayTenantInfo(item.tenant, item.room);
                    break;
                }
            }
        });
    }

    private void displayTenantInfo(TenantEntity tenant, RoomEntity room) {
        binding.textTenantName.setText(tenant.getName());
        binding.textTenantRoom.setText(room != null ? "Phòng " + room.getName() : "Chưa gán phòng");
        
        binding.textTenantPhone.setText(tenant.getPhone());
        binding.textTenantIc.setText(TextUtils.isEmpty(tenant.getIdentityCard()) ? "Chưa cập nhật" : tenant.getIdentityCard());
        binding.textTenantHometown.setText(TextUtils.isEmpty(tenant.getHometown()) ? "Chưa cập nhật" : tenant.getHometown());
        
        binding.textTenantStartDate.setText(TextUtils.isEmpty(tenant.getStartDate()) ? "Chưa cập nhật" : tenant.getStartDate());
        binding.textTenantDeposit.setText(FinanceUtils.formatCurrency(tenant.getDeposit()));

        binding.btnViewContract.setOnClickListener(v -> {
            // Tạm thời hiển thị Toast, logic xem hợp đồng chi tiết có thể copy từ TenantListActivity
            Toast.makeText(this, "Đang mở hợp đồng số hóa...", Toast.LENGTH_SHORT).show();
        });
    }
}
