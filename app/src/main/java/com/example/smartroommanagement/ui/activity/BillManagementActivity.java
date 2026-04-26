package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.bumptech.glide.Glide;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.example.smartroommanagement.databinding.ActivityBillManagementBinding;
import com.example.smartroommanagement.databinding.DialogAddBillBinding;
import com.example.smartroommanagement.databinding.DialogBillDetailBinding;
import com.example.smartroommanagement.ui.adapter.BillAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;
import com.example.smartroommanagement.util.PaymentUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BillManagementActivity extends AppCompatActivity {
    private ActivityBillManagementBinding binding;
    private RoomDetailViewModel viewModel;
    private BillAdapter adapter;
    private boolean isOptimisticUpdateActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        setupViewModel();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý hóa đơn");
        }

        adapter = new BillAdapter();
        binding.recyclerViewBills.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBills.setAdapter(adapter);

        // NHẤN GIỮ ĐỂ HIỆN OPTIONS (GIỐNG QUẢN LÝ PHÒNG)
        adapter.setOnBillLongClickListener(this::showBillOptionsDialog);

        adapter.setOnBillActionListener(new BillAdapter.OnBillActionListener() {
            @Override
            public void onBillClick(Object item) {
                if (item instanceof BillWithRoomAndTenant) {
                    showBillDetailDialog((BillWithRoomAndTenant) item);
                }
            }

            @Override
            public void onShareBill(BillWithRoomAndTenant item) {
                showBillDetailDialog(item);
            }

            @Override
            public void onMarkAsPaid(BillWithRoomAndTenant item) {
                if (item.bill.isPaid()) {
                    new AlertDialog.Builder(BillManagementActivity.this)
                            .setTitle("Xác nhận hủy thanh toán")
                            .setMessage("Bạn có chắc chắn muốn hủy trạng thái đã thanh toán của hóa đơn này?")
                            .setPositiveButton("Đồng ý", (d, w) -> performOptimisticUpdate(item))
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    performOptimisticUpdate(item);
                }
            }

            @Override
            public void onPayQR(BillWithRoomAndTenant item) {
                showBillDetailDialog(item);
            }
        });
    }

    private void showBillOptionsDialog(BillWithRoomAndTenant item) {
        String[] options = {"Xem chi tiết", "Chỉnh sửa hóa đơn", "Xóa hóa đơn"};
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn hóa đơn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showBillDetailDialog(item);
                    } else if (which == 1) {
                        showEditBillDialog(item);
                    } else {
                        confirmDeleteBill(item.bill);
                    }
                })
                .show();
    }

    private void showEditBillDialog(BillWithRoomAndTenant item) {
        DialogAddBillBinding db = DialogAddBillBinding.inflate(getLayoutInflater());
        
        // Thêm định dạng tiền tệ cho các ô nhập liệu
        FinanceUtils.addCurrencyFormatter(db.editRoomPriceAtBill);
        FinanceUtils.addCurrencyFormatter(db.editElectricityPrice);
        FinanceUtils.addCurrencyFormatter(db.editWaterPrice);
        FinanceUtils.addCurrencyFormatter(db.editLaundryFee);
        FinanceUtils.addCurrencyFormatter(db.editTrashFee);
        FinanceUtils.addCurrencyFormatter(db.editWifiFee);
        FinanceUtils.addCurrencyFormatter(db.editOtherFee);
        
        // Điền dữ liệu cũ
        db.editBillMonth.setText(item.bill.getMonthYear());
        db.editRoomPriceAtBill.setText(String.valueOf((int)(item.room != null ? item.room.getBasePrice() : item.bill.getTotalAmount())));
        db.editElectricity.setText(String.valueOf(item.bill.getElectricityUsage()));
        db.editWater.setText(String.valueOf(item.bill.getWaterUsage()));
        db.editLaundryFee.setText(String.valueOf((int)item.bill.getLaundryFee()));
        db.editTrashFee.setText(String.valueOf((int)item.bill.getTrashFee()));
        db.editWifiFee.setText(String.valueOf((int)item.bill.getWifiFee()));
        db.editOtherFee.setText(String.valueOf((int)item.bill.getOtherFee()));
        db.editOtherFeeNote.setText(item.bill.getOtherFeeNote());

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa hóa đơn")
                .setView(db.getRoot())
                .setPositiveButton("Cập nhật", (d, w) -> {
                    try {
                        String monthYear = db.editBillMonth.getText().toString().trim();
                        double roomPrice = FinanceUtils.parseFormattedCurrency(db.editRoomPriceAtBill.getText().toString().trim());
                        double elecUsage = Double.parseDouble(db.editElectricity.getText().toString().trim());
                        double elecPrice = FinanceUtils.parseFormattedCurrency(db.editElectricityPrice.getText().toString().trim());
                        double waterUsage = Double.parseDouble(db.editWater.getText().toString().trim());
                        double waterPrice = FinanceUtils.parseFormattedCurrency(db.editWaterPrice.getText().toString().trim());
                        
                        double laundry = FinanceUtils.parseFormattedCurrency(db.editLaundryFee.getText().toString());
                        double trash = FinanceUtils.parseFormattedCurrency(db.editTrashFee.getText().toString());
                        double wifi = FinanceUtils.parseFormattedCurrency(db.editWifiFee.getText().toString());
                        double other = FinanceUtils.parseFormattedCurrency(db.editOtherFee.getText().toString());
                        String otherNote = db.editOtherFeeNote.getText().toString().trim();

                        double total = roomPrice + (elecUsage * elecPrice) + (waterUsage * waterPrice) + laundry + trash + wifi + other;

                        BillEntity updatedBill = new BillEntity(item.bill);
                        updatedBill.setMonthYear(monthYear);
                        updatedBill.setElectricityUsage(elecUsage);
                        updatedBill.setWaterUsage(waterUsage);
                        updatedBill.setLaundryFee(laundry);
                        updatedBill.setTrashFee(trash);
                        updatedBill.setWifiFee(wifi);
                        updatedBill.setOtherFee(other);
                        updatedBill.setOtherFeeNote(otherNote);
                        updatedBill.setTotalAmount(total);

                        viewModel.updateBill(updatedBill);
                        Toast.makeText(this, "Đã cập nhật hóa đơn", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteBill(BillEntity bill) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hóa đơn này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    viewModel.deleteBill(bill);
                    Toast.makeText(this, "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBillDetailDialog(BillWithRoomAndTenant item) {
        DialogBillDetailBinding db = DialogBillDetailBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(db.getRoot())
                .create();

        db.textBillRoom.setText(item.room != null ? item.room.getName() : "Phòng " + item.bill.getRoomId());
        db.textBillMonth.setText(item.bill.getMonthYear());
        
        String tenantName = item.bill.getTenantName();
        if (TextUtils.isEmpty(tenantName)) {
            tenantName = (item.tenant != null ? item.tenant.getName() : "Chưa có tên");
        }
        db.textBillTenant.setText(tenantName);

        double roomPrice = (item.room != null) ? item.room.getBasePrice() : (item.bill.getTotalAmount() - (item.bill.getElectricityUsage()*4000) - (item.bill.getWaterUsage()*10000));
        db.textPriceRoom.setText(FinanceUtils.formatCurrency(roomPrice));

        double elecUsage = item.bill.getElectricityUsage();
        db.labelElec.setText("Tiền điện (" + elecUsage + " số x 4.000)");
        db.textPriceElec.setText(FinanceUtils.formatCurrency(elecUsage * 4000));

        double waterUsage = item.bill.getWaterUsage();
        db.labelWater.setText("Tiền nước (" + waterUsage + " khối x 10.000)");
        db.textPriceWater.setText(FinanceUtils.formatCurrency(waterUsage * 10000));

        if (item.bill.getLaundryFee() > 0) {
            db.rowLaundry.setVisibility(View.VISIBLE);
            db.textPriceLaundry.setText(FinanceUtils.formatCurrency(item.bill.getLaundryFee()));
        }
        if (item.bill.getTrashFee() > 0) {
            db.rowTrash.setVisibility(View.VISIBLE);
            db.textPriceTrash.setText(FinanceUtils.formatCurrency(item.bill.getTrashFee()));
        }
        if (item.bill.getWifiFee() > 0) {
            db.rowWifi.setVisibility(View.VISIBLE);
            db.textPriceWifi.setText(FinanceUtils.formatCurrency(item.bill.getWifiFee()));
        }
        if (item.bill.getOtherFee() > 0) {
            db.rowOther.setVisibility(View.VISIBLE);
            db.labelOther.setText(!TextUtils.isEmpty(item.bill.getOtherFeeNote()) ? item.bill.getOtherFeeNote() : "Phí khác");
            db.textPriceOther.setText(FinanceUtils.formatCurrency(item.bill.getOtherFee()));
        }

        db.textTotalAmount.setText(FinanceUtils.formatCurrency(item.bill.getTotalAmount()));

        String description = "Thanh toan " + (item.room != null ? item.room.getName() : "phong") + " thang " + item.bill.getMonthYear();
        String qrUrl = PaymentUtils.getQrUrl(item.bill.getTotalAmount(), description);

        Glide.with(this)
                .load(qrUrl)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        db.progressQrBill.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        db.progressQrBill.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(db.imgBillQr);

        db.btnCloseBill.setOnClickListener(v -> dialog.dismiss());
        db.btnShareBill.setOnClickListener(v -> shareViewAsImage(db.cardBillPaper));

        dialog.show();
    }

    private void shareViewAsImage(View view) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            File cachePath = new File(getExternalCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "bill_" + System.currentTimeMillis() + ".png");
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
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hóa đơn qua:"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performOptimisticUpdate(BillWithRoomAndTenant item) {
        isOptimisticUpdateActive = true;

        List<Object> currentList = new ArrayList<>(adapter.getCurrentList());
        int index = -1;
        for (int i = 0; i < currentList.size(); i++) {
            Object obj = currentList.get(i);
            if (obj instanceof BillWithRoomAndTenant && 
                ((BillWithRoomAndTenant) obj).bill.getId() == item.bill.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            BillWithRoomAndTenant oldItem = (BillWithRoomAndTenant) currentList.get(index);
            BillWithRoomAndTenant newItem = new BillWithRoomAndTenant();
            newItem.bill = new BillEntity(oldItem.bill);
            newItem.bill.setPaid(!oldItem.bill.isPaid()); // Toggle state
            newItem.room = oldItem.room;
            newItem.tenant = oldItem.tenant;
            
            currentList.set(index, newItem);
            adapter.submitList(currentList); 
            
            viewModel.updateBill(newItem.bill);
            Toast.makeText(this, newItem.bill.isPaid() ? "Đã cập nhật thanh toán" : "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isOptimisticUpdateActive = false;
            }, 800);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        viewModel.getAllBillsWithDetails().observe(this, bills -> {
            if (bills != null && !isOptimisticUpdateActive) {
                adapter.submitList(new ArrayList<>(bills));
            }
        });
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
