package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.example.smartroommanagement.data.entity.RoomWithTenants;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.databinding.ActivityContractManagementBinding;
import com.example.smartroommanagement.databinding.DialogContractDetailBinding;
import com.example.smartroommanagement.ui.adapter.ContractAdapter;
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

public class ContractManagementActivity extends AppCompatActivity {
    private static final String FILTER_ALL = "all";
    private static final String FILTER_HAS_CONTRACT = "has_contract";
    private static final String FILTER_NO_CONTRACT = "no_contract";

    private ActivityContractManagementBinding binding;
    private RoomDetailViewModel viewModel;
    private ContractAdapter adapter;
    private String currentFilter = FILTER_ALL;
    private String currentSearchQuery = "";
    private final List<RoomWithTenants> allContractRooms = new ArrayList<>();

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

        setupFilterListeners();
        setupSearch();
    }

    private void setupFilterListeners() {
        binding.cardFilterAllContract.setOnClickListener(v -> selectContractFilter(FILTER_ALL));
        binding.cardFilterHasContract.setOnClickListener(v -> selectContractFilter(FILTER_HAS_CONTRACT));
        binding.cardFilterNoContract.setOnClickListener(v -> selectContractFilter(FILTER_NO_CONTRACT));
        updateFilterUi();
    }

    private void setupSearch() {
        binding.editSearchContract.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = normalizeSearch(s == null ? "" : s.toString().trim());
                applyContractFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void selectContractFilter(String filter) {
        currentFilter = filter;
        updateFilterUi();
        applyContractFilter();
    }

    private void updateContractCounts() {
        int hasContract = 0;
        int noContract = 0;
        for (RoomWithTenants item : allContractRooms) {
            if (hasContract(item)) {
                hasContract++;
            } else {
                noContract++;
            }
        }

        binding.textAllContractCount.setText(String.valueOf(allContractRooms.size()));
        binding.textHasContractCount.setText(String.valueOf(hasContract));
        binding.textNoContractCount.setText(String.valueOf(noContract));
    }

    private void applyContractFilter() {
        List<RoomWithTenants> filtered = new ArrayList<>();
        for (RoomWithTenants item : allContractRooms) {
            boolean hasContract = hasContract(item);
            if (FILTER_HAS_CONTRACT.equals(currentFilter) && !hasContract) continue;
            if (FILTER_NO_CONTRACT.equals(currentFilter) && hasContract) continue;
            if (!matchesContractSearch(item)) continue;
            filtered.add(item);
        }

        adapter.submitList(filtered);
        binding.textNoContracts.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean hasContract(RoomWithTenants item) {
        return item != null && item.tenants != null && !item.tenants.isEmpty();
    }

    private boolean matchesContractSearch(RoomWithTenants item) {
        if (TextUtils.isEmpty(currentSearchQuery)) {
            return true;
        }

        StringBuilder searchableText = new StringBuilder();
        if (item.room != null) {
            searchableText.append(safeText(item.room.getName())).append(' ')
                    .append(item.room.getId()).append(' ')
                    .append(item.room.getBasePrice()).append(' ')
                    .append(safeText(item.room.getStatus())).append(' ')
                    .append(safeText(item.room.getDescription())).append(' ');
        }

        searchableText.append(hasContract(item)
                ? "da co hop dong đã có hợp đồng contract rented "
                : "chua co hop dong chưa có hợp đồng no contract empty ");

        if (item.tenants != null) {
            for (TenantEntity tenant : item.tenants) {
                searchableText.append(safeText(tenant.getName())).append(' ')
                        .append(safeText(tenant.getPhone())).append(' ')
                        .append(safeText(tenant.getIdentityCard())).append(' ')
                        .append(safeText(tenant.getHometown())).append(' ')
                        .append(safeText(tenant.getBirthDate())).append(' ')
                        .append(safeText(tenant.getStartDate())).append(' ')
                        .append(tenant.getDeposit()).append(' ')
                        .append(tenant.getContractTerm() != null ? tenant.getContractTerm() : "").append(' ');
            }
        }

        return normalizeSearch(searchableText.toString()).contains(currentSearchQuery);
    }

    private void updateFilterUi() {
        int defaultStroke = Color.parseColor("#F1F5F9");
        int defaultBg = Color.parseColor("#FFFFFF");
        int defaultText = Color.parseColor("#64748B");
        int allColor = Color.parseColor("#6366F1");
        int allBg = Color.parseColor("#EEF2FF");
        int hasColor = Color.parseColor("#10B981");
        int hasBg = Color.parseColor("#ECFDF5");
        int noColor = Color.parseColor("#EF4444");
        int noBg = Color.parseColor("#FEF2F2");

        binding.cardFilterAllContract.setStrokeColor(defaultStroke);
        binding.cardFilterAllContract.setCardBackgroundColor(defaultBg);
        binding.labelFilterAllContract.setTextColor(defaultText);
        binding.textAllContractCount.setTextColor(defaultText);

        binding.cardFilterHasContract.setStrokeColor(defaultStroke);
        binding.cardFilterHasContract.setCardBackgroundColor(defaultBg);
        binding.labelFilterHasContract.setTextColor(defaultText);
        binding.textHasContractCount.setTextColor(defaultText);

        binding.cardFilterNoContract.setStrokeColor(defaultStroke);
        binding.cardFilterNoContract.setCardBackgroundColor(defaultBg);
        binding.labelFilterNoContract.setTextColor(defaultText);
        binding.textNoContractCount.setTextColor(defaultText);

        if (FILTER_HAS_CONTRACT.equals(currentFilter)) {
            binding.cardFilterHasContract.setStrokeColor(hasColor);
            binding.cardFilterHasContract.setCardBackgroundColor(hasBg);
            binding.labelFilterHasContract.setTextColor(hasColor);
            binding.textHasContractCount.setTextColor(hasColor);
        } else if (FILTER_NO_CONTRACT.equals(currentFilter)) {
            binding.cardFilterNoContract.setStrokeColor(noColor);
            binding.cardFilterNoContract.setCardBackgroundColor(noBg);
            binding.labelFilterNoContract.setTextColor(noColor);
            binding.textNoContractCount.setTextColor(noColor);
        } else {
            binding.cardFilterAllContract.setStrokeColor(allColor);
            binding.cardFilterAllContract.setCardBackgroundColor(allBg);
            binding.labelFilterAllContract.setTextColor(allColor);
            binding.textAllContractCount.setTextColor(allColor);
        }
    }

    private String normalizeSearch(String value) {
        String normalized = Normalizer.normalize(safeText(value), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void setupViewModel() {
        try {
            viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
            viewModel.getRoomsWithTenants().observe(this, rooms -> {
                if (rooms != null) {
                    allContractRooms.clear();
                    allContractRooms.addAll(rooms);
                    updateContractCounts();
                    applyContractFilter();
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
