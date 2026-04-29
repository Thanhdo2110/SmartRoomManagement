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

        // THAY ĐỔI: Chuyển sang màn hình Chi tiết khách thuê khi click
        adapter.setOnTenantClickListener(tenantWithRoom -> {
            Intent intent = new Intent(this, TenantDetailActivity.class);
            intent.putExtra(TenantDetailActivity.EXTRA_TENANT_ID, tenantWithRoom.tenant.getId());
            if (tenantWithRoom.room != null) {
                intent.putExtra(TenantDetailActivity.EXTRA_ROOM_ID, tenantWithRoom.room.getId());
            }
            startActivity(intent);
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        viewModel.getAllTenantsWithRoom().observe(this, tenants -> adapter.submitList(tenants));
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
