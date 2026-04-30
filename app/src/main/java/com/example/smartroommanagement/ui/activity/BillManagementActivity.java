package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smartroommanagement.data.AppDatabase;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.signature.ObjectKey;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.example.smartroommanagement.data.entity.UserEntity;
import com.example.smartroommanagement.databinding.ActivityBillManagementBinding;
import com.example.smartroommanagement.databinding.DialogAddBillBinding;
import com.example.smartroommanagement.databinding.DialogBillDetailBinding;
import com.example.smartroommanagement.ui.adapter.BillAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;
import com.example.smartroommanagement.util.PaymentUtils;
import com.example.smartroommanagement.util.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BillManagementActivity extends AppCompatActivity {
    private ActivityBillManagementBinding binding;
    private RoomDetailViewModel viewModel;
    private BillAdapter adapter;
    private boolean isOptimisticUpdateActive = false;
    private SessionManager sessionManager;
    private UserEntity currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
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

        // Sử dụng Menu 3 chấm thay cho nhấn giữ
        adapter.setOnBillMoreClickListener(this::showBillOptionsDialog);
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
                togglePaidStatus(item);
            }

            @Override
            public void onPayQR(BillWithRoomAndTenant item) {
                showBillDetailDialog(item);
            }
        });
    }

    private void showBillOptionsDialog(BillWithRoomAndTenant item) {
        String payOption = item.bill.isPaid() ? "Đánh dấu chưa thanh toán" : "Đánh dấu đã thanh toán";
        String[] options = {"Xem chi tiết", "Chỉnh sửa hóa đơn", "Xóa hóa đơn", payOption};
        
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn hóa đơn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showBillDetailDialog(item);
                    } else if (which == 1) {
                        showEditBillDialog(item);
                    } else if (which == 2) {
                        confirmDeleteBill(item.bill);
                    } else {
                        togglePaidStatus(item);
                    }
                })
                .show();
    }

    private void togglePaidStatus(BillWithRoomAndTenant item) {
        if (item.bill.isPaid()) {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn hủy trạng thái đã thanh toán?")
                    .setPositiveButton("Đồng ý", (d, w) -> performOptimisticUpdate(item))
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            performOptimisticUpdate(item);
        }
    }

    private void showEditBillDialog(BillWithRoomAndTenant item) {
        DialogAddBillBinding db = DialogAddBillBinding.inflate(getLayoutInflater());
        FinanceUtils.addCurrencyFormatter(db.editRoomPriceAtBill);
        FinanceUtils.addCurrencyFormatter(db.editElectricityPrice);
        FinanceUtils.addCurrencyFormatter(db.editWaterPrice);
        FinanceUtils.addCurrencyFormatter(db.editLaundryFee);
        FinanceUtils.addCurrencyFormatter(db.editTrashFee);
        FinanceUtils.addCurrencyFormatter(db.editWifiFee);
        FinanceUtils.addCurrencyFormatter(db.editOtherFee);
        
        // Điền dữ liệu cũ
        db.editBillMonth.setText(item.bill.getMonthYear());
        db.editRoomPriceAtBill.setText(String.valueOf((int)(item.room != null ? item.room.getBasePrice() : 0)));
        db.editElectricity.setText(String.valueOf(item.bill.getElectricityUsage()));
        db.editWater.setText(String.valueOf(item.bill.getWaterUsage()));
        db.editLaundryFee.setText(String.valueOf((int)item.bill.getLaundryFee()));
        db.editTrashFee.setText(String.valueOf((int)item.bill.getTrashFee()));
        db.editWifiFee.setText(String.valueOf((int)item.bill.getWifiFee()));
        db.editOtherFee.setText(String.valueOf((int)item.bill.getOtherFee()));
        db.editOtherFeeNote.setText(item.bill.getOtherFeeNote());

        // Điền thông tin ngân hàng hiện tại
        if (currentUser != null) {
            db.editBankId.setText(currentUser.getBankId());
            db.editAccountNo.setText(currentUser.getAccountNo());
            db.editAccountName.setText(currentUser.getAccountName());
        }

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa hóa đơn")
                .setView(db.getRoot())
                .setPositiveButton("Cập nhật", (d, w) -> {
                    try {
                        String monthYear = db.editBillMonth.getText().toString().trim();
                        double roomPrice = FinanceUtils.parseFormattedCurrency(db.editRoomPriceAtBill.getText().toString());
                        double elecUsage = FinanceUtils.parsePlainNumber(db.editElectricity.getText().toString());
                        double elecPrice = FinanceUtils.parseFormattedCurrency(db.editElectricityPrice.getText().toString());
                        double waterUsage = FinanceUtils.parsePlainNumber(db.editWater.getText().toString());
                        double waterPrice = FinanceUtils.parseFormattedCurrency(db.editWaterPrice.getText().toString());
                        
                        double laundry = FinanceUtils.parseFormattedCurrency(db.editLaundryFee.getText().toString());
                        double trash = FinanceUtils.parseFormattedCurrency(db.editTrashFee.getText().toString());
                        double wifi = FinanceUtils.parseFormattedCurrency(db.editWifiFee.getText().toString());
                        double other = FinanceUtils.parseFormattedCurrency(db.editOtherFee.getText().toString());
                        String otherNote = db.editOtherFeeNote.getText().toString().trim();

                        String bankId = db.editBankId.getText().toString().trim();
                        String accNo = db.editAccountNo.getText().toString().trim();
                        String accName = db.editAccountName.getText().toString().trim();

                        double total = roomPrice + (elecUsage * elecPrice) + (waterUsage * waterPrice) + laundry + trash + wifi + other;

                        // Cập nhật ngân hàng nếu đổi
                        if (currentUser != null) {
                            boolean changed = !TextUtils.equals(bankId, currentUser.getBankId()) || 
                                              !TextUtils.equals(accNo, currentUser.getAccountNo()) || 
                                              !TextUtils.equals(accName, currentUser.getAccountName());
                            if (changed) {
                                currentUser.setBankId(bankId);
                                currentUser.setAccountNo(accNo);
                                currentUser.setAccountName(accName);
                                viewModel.updateUser(currentUser);
                            }
                        }

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
                        Toast.makeText(this, "Lỗi nhập liệu số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteBill(BillEntity bill) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hóa đơn này?")
                .setPositiveButton("Xóa", (d, w) -> viewModel.deleteBill(bill))
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
        db.textBillTenant.setText(!TextUtils.isEmpty(item.bill.getTenantName()) ? item.bill.getTenantName() : (item.tenant != null ? item.tenant.getName() : "N/A"));

        db.textTotalAmount.setText(FinanceUtils.formatCurrency(item.bill.getTotalAmount()));

        String description = "Thanh toan " + (item.room != null ? item.room.getName() : "phong") + " thang " + item.bill.getMonthYear();
        
        // Tạo QR dựa trên Bank Info của chủ trọ
        loadQrIntoBillDetail(db, item.bill.getTotalAmount(), description);

        db.textPriceRoom.setText(FinanceUtils.formatCurrency(item.room != null ? item.room.getBasePrice() : 0));
        db.labelElec.setText("Tiền điện (" + item.bill.getElectricityUsage() + " số x 4.000)");
        db.textPriceElec.setText(FinanceUtils.formatCurrency(item.bill.getElectricityUsage() * 4000));
        db.labelWater.setText("Tiền nước (" + item.bill.getWaterUsage() + " khối x 10.000)");
        db.textPriceWater.setText(FinanceUtils.formatCurrency(item.bill.getWaterUsage() * 10000));
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

        db.btnCloseBill.setOnClickListener(v -> dialog.dismiss());
        db.btnShareBill.setOnClickListener(v -> shareViewAsImage(db.cardBillPaper));
        dialog.show();
    }

    private void loadQrIntoBillDetail(DialogBillDetailBinding db, double amount, String description) {
        Runnable renderQr = () -> {
            String qrUrl;
            if (currentUser != null && !TextUtils.isEmpty(currentUser.getAccountNo())) {
                qrUrl = PaymentUtils.getQrUrl(amount, description,
                        currentUser.getBankId(), currentUser.getAccountNo(), currentUser.getAccountName());
            } else {
                qrUrl = PaymentUtils.getQrUrl(amount, description);
            }

            Glide.with(this)
                    .load(qrUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .signature(new ObjectKey(qrUrl + System.currentTimeMillis()))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            db.progressQrBill.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            db.progressQrBill.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(db.imgBillQr);
        };

        if (currentUser != null) {
            renderQr.run();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = AppDatabase.getDatabase(this).userDao().getUserById(sessionManager.getUserId());
            if (user != null) {
                currentUser = user;
            }
            runOnUiThread(renderQr);
        });
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
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/png");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hóa đơn:"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void performOptimisticUpdate(BillWithRoomAndTenant item) {
        isOptimisticUpdateActive = true;
        List<Object> currentList = new ArrayList<>(adapter.getCurrentList());
        int index = -1;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i) instanceof BillWithRoomAndTenant && ((BillWithRoomAndTenant) currentList.get(i)).bill.getId() == item.bill.getId()) {
                index = i; break;
            }
        }
        if (index != -1) {
            BillWithRoomAndTenant newItem = new BillWithRoomAndTenant();
            newItem.bill = new BillEntity(item.bill);
            newItem.bill.setPaid(!item.bill.isPaid());
            newItem.room = item.room;
            newItem.tenant = item.tenant;
            currentList.set(index, newItem);
            adapter.submitList(currentList);
            viewModel.updateBill(newItem.bill);
            new Handler(Looper.getMainLooper()).postDelayed(() -> isOptimisticUpdateActive = false, 800);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        viewModel.getAllBillsWithDetails().observe(this, bills -> {
            if (bills != null && !isOptimisticUpdateActive) {
                adapter.submitList(new ArrayList<>(bills));
            }
        });
        viewModel.getUserById(sessionManager.getUserId()).observe(this, user -> currentUser = user);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
