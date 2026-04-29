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
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.UserEntity;
import com.example.smartroommanagement.databinding.ActivityRoomDetailBinding;
import com.example.smartroommanagement.databinding.DialogAddBillBinding;
import com.example.smartroommanagement.databinding.DialogAddTenantBinding;
import com.example.smartroommanagement.databinding.DialogBillDetailBinding;
import com.example.smartroommanagement.databinding.DialogContractDetailBinding;
import com.example.smartroommanagement.ui.adapter.BillAdapter;
import com.example.smartroommanagement.ui.viewmodel.RoomDetailViewModel;
import com.example.smartroommanagement.util.FinanceUtils;
import com.example.smartroommanagement.util.PaymentUtils;
import com.example.smartroommanagement.util.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoomDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ROOM_ID = "extra_room_id";
    private ActivityRoomDetailBinding binding;
    private RoomDetailViewModel viewModel;
    private BillAdapter billAdapter;
    private int roomId;
    private RoomEntity currentRoom;
    private TenantEntity currentTenant;
    private UserEntity currentUser;
    private boolean isOptimisticUpdateActive = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        roomId = getIntent().getIntExtra(EXTRA_ROOM_ID, -1);
        if (roomId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin phòng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupViewModel();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        billAdapter = new BillAdapter();
        
        // NHẤN GIỮ LỊCH SỬ HÓA ĐƠN ĐỂ HIỆN SỬA/XÓA
        billAdapter.setOnBillLongClickListener(wrap -> showBillOptionsDialog(wrap.bill));

        billAdapter.setOnBillClickListener(bill -> {
            List<Object> list = billAdapter.getCurrentList();
            for (Object obj : list) {
                if (obj instanceof BillWithRoomAndTenant) {
                    BillWithRoomAndTenant wrap = (BillWithRoomAndTenant) obj;
                    if (wrap.bill.getId() == bill.getId()) {
                        showBillDetailDialog(wrap);
                        return;
                    }
                }
            }
            BillWithRoomAndTenant manualWrap = new BillWithRoomAndTenant();
            manualWrap.bill = bill;
            manualWrap.room = currentRoom;
            manualWrap.tenant = currentTenant;
            showBillDetailDialog(manualWrap);
        });
        
        billAdapter.setOnBillActionListener(new BillAdapter.OnBillActionListener() {
            @Override
            public void onBillClick(Object item) {
                if (item instanceof BillWithRoomAndTenant) {
                    showBillDetailDialog((BillWithRoomAndTenant) item);
                }
            }

            @Override
            public void onShareBill(BillWithRoomAndTenant bill) {
                showBillDetailDialog(bill);
            }

            @Override
            public void onMarkAsPaid(BillWithRoomAndTenant item) {
                if (item.bill.isPaid()) {
                    new AlertDialog.Builder(RoomDetailActivity.this)
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

        binding.recyclerViewBills.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBills.setAdapter(billAdapter);

        binding.btnAddTenant.setOnClickListener(v -> {
            if (currentTenant != null) {
                showTenantOptionsDialog(currentTenant);
            } else {
                showAddTenantDialog(null);
            }
        });
        
        binding.layoutTenantContent.setOnClickListener(v -> {
            if (currentTenant != null) {
                showTenantOptionsDialog(currentTenant);
            }
        });

        binding.fabAddBill.setOnClickListener(v -> showAddBillDialog(null));
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

        double roomPrice = (item.room != null) ? item.room.getBasePrice() : 0;
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
        
        // SỬ DỤNG THÔNG TIN NGÂN HÀNG CỦA CHỦ TRỌ
        String qrUrl;
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getAccountNo())) {
            qrUrl = PaymentUtils.getQrUrl(item.bill.getTotalAmount(), description, 
                    currentUser.getBankId(), currentUser.getAccountNo(), currentUser.getAccountName());
        } else {
            qrUrl = PaymentUtils.getQrUrl(item.bill.getTotalAmount(), description);
        }

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

        // Hiển thị thông tin thời hạn hợp đồng và ràng buộc mất cọc
        int term = tenant.getContractTerm() != null ? tenant.getContractTerm() : 12; // Mặc định 12 tháng nếu chưa nhập
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
            File file = new File(cachePath, "doc_" + System.currentTimeMillis() + ".png");
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
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ tài liệu qua:"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể chia sẻ hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performOptimisticUpdate(BillWithRoomAndTenant item) {
        isOptimisticUpdateActive = true;

        List<Object> currentList = new ArrayList<>(billAdapter.getCurrentList());
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
            newItem.bill.setPaid(!oldItem.bill.isPaid()); // Toggle
            newItem.room = oldItem.room;
            newItem.tenant = oldItem.tenant;
            
            currentList.set(index, newItem);
            billAdapter.submitList(currentList);
            
            viewModel.updateBill(newItem.bill);
            Toast.makeText(this, newItem.bill.isPaid() ? "Đã cập nhật thanh toán" : "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isOptimisticUpdateActive = false;
            }, 800);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RoomDetailViewModel.class);
        
        viewModel.getRoomById(roomId).observe(this, room -> {
            if (room != null) {
                currentRoom = room;
                binding.textDetailName.setText(room.getName());
                binding.textDetailStatus.setText(room.getStatus());
                binding.textDetailPrice.setText(FinanceUtils.formatCurrency(room.getBasePrice()) + " / tháng");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(room.getName());
                }
            }
        });

        viewModel.getTenantsByRoom(roomId).observe(this, tenants -> {
            if (tenants != null && !tenants.isEmpty()) {
                currentTenant = tenants.get(0);
                binding.textTenantName.setText(currentTenant.getName());
                binding.textTenantPhone.setText("Liên hệ: " + currentTenant.getPhone());
                binding.btnCallTenant.setVisibility(View.VISIBLE);
                binding.btnCallTenant.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + currentTenant.getPhone()));
                    startActivity(intent);
                });
                binding.btnAddTenant.setText("Quản lý khách thuê");
                
                binding.layoutRentalInfo.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(currentTenant.getStartDate())) {
                    binding.textTenantStartDate.setText("Ngày thuê: " + currentTenant.getStartDate());
                } else {
                    binding.textTenantStartDate.setText("Ngày thuê: --/--/----");
                }
                binding.textTenantDeposit.setText("Tiền cọc: " + FinanceUtils.formatCurrency(currentTenant.getDeposit()));
            } else {
                currentTenant = null;
                binding.textTenantName.setText("Chưa có khách thuê");
                binding.textTenantPhone.setText("Liên hệ: --");
                binding.btnCallTenant.setVisibility(View.GONE);
                binding.btnAddTenant.setText("Thêm khách thuê");
                binding.layoutRentalInfo.setVisibility(View.GONE);
            }
        });

        viewModel.getBillsWithDetailsByRoom(roomId).observe(this, bills -> {
            if (bills != null && !isOptimisticUpdateActive) {
                billAdapter.submitList(new ArrayList<>(bills));
            }
        });

        // LẤY THÔNG TIN NGƯỜI DÙNG ĐỂ PRE-FILL QR
        viewModel.getUserById(sessionManager.getUserId()).observe(this, user -> {
            if (user != null) {
                currentUser = user;
            }
        });
    }

    private void showTenantOptionsDialog(TenantEntity tenant) {
        String[] options = {"Xem hợp đồng", "Chỉnh sửa thông tin", "Xóa khách thuê"};
        new AlertDialog.Builder(this)
            .setTitle("Tùy chọn khách thuê")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    if (currentRoom != null) {
                        showContractDetailDialog(tenant, currentRoom);
                    }
                } else if (which == 1) {
                    showAddTenantDialog(tenant);
                } else {
                    confirmDeleteTenant(tenant);
                }
            })
            .show();
    }

    private void confirmDeleteTenant(TenantEntity tenant) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa khách thuê này?")
            .setPositiveButton("Xóa", (d, w) -> {
                viewModel.deleteTenant(tenant);
                if (currentRoom != null) {
                    currentRoom.setStatus("Trống");
                    viewModel.updateRoom(currentRoom);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showAddTenantDialog(TenantEntity existingTenant) {
        DialogAddTenantBinding db = DialogAddTenantBinding.inflate(getLayoutInflater());
        
        FinanceUtils.addCurrencyFormatter(db.editTenantDeposit);

        if (existingTenant != null) {
            db.editTenantName.setText(existingTenant.getName());
            db.editTenantPhone.setText(existingTenant.getPhone());
            db.editTenantIdentity.setText(existingTenant.getIdentityCard());
            db.editTenantDeposit.setText(String.valueOf((int)existingTenant.getDeposit()));
            db.editStartDate.setText(existingTenant.getStartDate());
            db.editTenantHometown.setText(existingTenant.getHometown());
            db.editTenantBirthdate.setText(existingTenant.getBirthDate());
            if (existingTenant.getContractTerm() != null) {
                db.editContractTerm.setText(String.valueOf(existingTenant.getContractTerm()));
            }
        }

        new AlertDialog.Builder(this)
            .setTitle(existingTenant == null ? "Thêm khách thuê" : "Cập nhật khách thuê")
            .setView(db.getRoot())
            .setPositiveButton("Lưu", (d, w) -> {
                String name = db.editTenantName.getText().toString().trim();
                String phone = db.editTenantPhone.getText().toString().trim();
                String identity = db.editTenantIdentity.getText().toString().trim();
                String depositStr = db.editTenantDeposit.getText().toString().trim();
                String startDate = db.editStartDate.getText().toString().trim();
                String hometown = db.editTenantHometown.getText().toString().trim();
                String birthdate = db.editTenantBirthdate.getText().toString().trim();
                String termStr = db.editContractTerm.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    Toast.makeText(this, "Vui lòng nhập tên và số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }

                double deposit = FinanceUtils.parseFormattedCurrency(depositStr);
                Integer contractTerm = TextUtils.isEmpty(termStr) ? 12 : Integer.parseInt(termStr);

                if (existingTenant == null) {
                    int userId = sessionManager.getUserId();
                    TenantEntity newTenant = new TenantEntity(name, phone, identity, birthdate, hometown, roomId, startDate, deposit, userId);
                    newTenant.setContractTerm(contractTerm);
                    viewModel.addTenant(newTenant);
                    
                    if (currentRoom != null) {
                        currentRoom.setStatus("Đã thuê");
                        viewModel.updateRoom(currentRoom);
                    }
                } else {
                    existingTenant.setName(name);
                    existingTenant.setPhone(phone);
                    existingTenant.setIdentityCard(identity);
                    existingTenant.setDeposit(deposit);
                    existingTenant.setStartDate(startDate);
                    existingTenant.setHometown(hometown);
                    existingTenant.setBirthDate(birthdate);
                    existingTenant.setContractTerm(contractTerm);
                    viewModel.updateTenant(existingTenant);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showAddBillDialog(BillEntity existingBill) {
        if (currentRoom == null) return;
        DialogAddBillBinding db = DialogAddBillBinding.inflate(getLayoutInflater());
        
        FinanceUtils.addCurrencyFormatter(db.editRoomPriceAtBill);
        FinanceUtils.addCurrencyFormatter(db.editElectricityPrice);
        FinanceUtils.addCurrencyFormatter(db.editWaterPrice);
        FinanceUtils.addCurrencyFormatter(db.editLaundryFee);
        FinanceUtils.addCurrencyFormatter(db.editTrashFee);
        FinanceUtils.addCurrencyFormatter(db.editWifiFee);
        FinanceUtils.addCurrencyFormatter(db.editOtherFee);

        // Pre-fill bank info from currentUser
        if (currentUser != null) {
            db.editBankId.setText(currentUser.getBankId());
            db.editAccountNo.setText(currentUser.getAccountNo());
            db.editAccountName.setText(currentUser.getAccountName());
        }

        if (existingBill != null) {
            db.editBillMonth.setText(existingBill.getMonthYear());
            db.editElectricity.setText(String.valueOf(existingBill.getElectricityUsage()));
            db.editWater.setText(String.valueOf(existingBill.getWaterUsage()));
            db.editRoomPriceAtBill.setText(String.valueOf((int)currentRoom.getBasePrice()));
            db.editLaundryFee.setText(String.valueOf((int)existingBill.getLaundryFee()));
            db.editTrashFee.setText(String.valueOf((int)existingBill.getTrashFee()));
            db.editWifiFee.setText(String.valueOf((int)existingBill.getWifiFee()));
            db.editOtherFee.setText(String.valueOf((int)existingBill.getOtherFee()));
            db.editOtherFeeNote.setText(existingBill.getOtherFeeNote());
            db.editElectricityPrice.setText("4000");
            db.editWaterPrice.setText("10000");
        } else {
            db.editRoomPriceAtBill.setText(String.valueOf((int)currentRoom.getBasePrice()));
            db.editElectricityPrice.setText("4000");
            db.editWaterPrice.setText("10000");
        }

        new AlertDialog.Builder(this)
            .setTitle(existingBill == null ? "Tạo hóa đơn tháng" : "Chỉnh sửa hóa đơn")
            .setView(db.getRoot())
            .setPositiveButton("Lưu", (d, w) -> {
                String roomPriceStr = db.editRoomPriceAtBill.getText().toString().trim();
                String electricityStr = db.editElectricity.getText().toString().trim();
                String electricityPriceStr = db.editElectricityPrice.getText().toString().trim();
                String waterStr = db.editWater.getText().toString().trim();
                String waterPriceStr = db.editWaterPrice.getText().toString().trim();
                String month = db.editBillMonth.getText().toString().trim();
                
                String laundryStr = db.editLaundryFee.getText().toString().trim();
                String trashStr = db.editTrashFee.getText().toString().trim();
                String wifiStr = db.editWifiFee.getText().toString().trim();
                String otherStr = db.editOtherFee.getText().toString().trim();
                String otherNote = db.editOtherFeeNote.getText().toString().trim();

                // Bank info fields
                String bankId = db.editBankId.getText().toString().trim();
                String accountNo = db.editAccountNo.getText().toString().trim();
                String accountName = db.editAccountName.getText().toString().trim();

                if (TextUtils.isEmpty(roomPriceStr) || TextUtils.isEmpty(electricityStr) || 
                    TextUtils.isEmpty(waterStr) || TextUtils.isEmpty(month)) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ các thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double roomPrice = FinanceUtils.parseFormattedCurrency(roomPriceStr);
                    double elecUsage = Double.parseDouble(electricityStr);
                    double elecPrice = FinanceUtils.parseFormattedCurrency(electricityPriceStr);
                    double waterUsage = Double.parseDouble(waterStr);
                    double waterPrice = FinanceUtils.parseFormattedCurrency(waterPriceStr);
                    
                    double laundry = FinanceUtils.parseFormattedCurrency(laundryStr);
                    double trash = FinanceUtils.parseFormattedCurrency(trashStr);
                    double wifi = FinanceUtils.parseFormattedCurrency(wifiStr);
                    double other = FinanceUtils.parseFormattedCurrency(otherStr);

                    double total = roomPrice + (elecUsage * elecPrice) + (waterUsage * waterPrice) + laundry + trash + wifi + other;
                    
                    // CẬP NHẬT THÔNG TIN NGÂN HÀNG CỦA CHỦ TRỌ (NẾU CÓ THAY ĐỔI)
                    if (currentUser != null) {
                        boolean changed = false;
                        if (!TextUtils.equals(bankId, currentUser.getBankId())) { currentUser.setBankId(bankId); changed = true; }
                        if (!TextUtils.equals(accountNo, currentUser.getAccountNo())) { currentUser.setAccountNo(accountNo); changed = true; }
                        if (!TextUtils.equals(accountName, currentUser.getAccountName())) { currentUser.setAccountName(accountName); changed = true; }
                        if (changed) {
                            viewModel.updateUser(currentUser);
                        }
                    }

                    if (existingBill == null) {
                        int userId = sessionManager.getUserId();
                        BillEntity newBill = new BillEntity(roomId, month, elecUsage, waterUsage, total, false, userId);
                        newBill.setLaundryFee(laundry);
                        newBill.setTrashFee(trash);
                        newBill.setWifiFee(wifi);
                        newBill.setOtherFee(other);
                        newBill.setOtherFeeNote(otherNote);
                        if (currentTenant != null) {
                            newBill.setTenantName(currentTenant.getName());
                        }
                        viewModel.addBill(newBill);
                    } else {
                        // TẠO ĐỐI TƯỢNG MỚI ĐỂ DIFFUTIL NHẬN BIẾT SỰ THAY ĐỔI (QUAN TRỌNG)
                        BillEntity updatedBill = new BillEntity(existingBill);
                        updatedBill.setMonthYear(month);
                        updatedBill.setElectricityUsage(elecUsage);
                        updatedBill.setWaterUsage(waterUsage);
                        updatedBill.setTotalAmount(total);
                        updatedBill.setLaundryFee(laundry);
                        updatedBill.setTrashFee(trash);
                        updatedBill.setWifiFee(wifi);
                        updatedBill.setOtherFee(other);
                        updatedBill.setOtherFeeNote(otherNote);
                        if (currentTenant != null) {
                            updatedBill.setTenantName(currentTenant.getName());
                        }
                        viewModel.updateBill(updatedBill);
                    }
                    Toast.makeText(this, "Đã lưu hóa đơn", Toast.LENGTH_SHORT).show();
                } catch (Exception e) { 
                    Toast.makeText(this, "Lỗi định dạng số", Toast.LENGTH_SHORT).show(); 
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showBillOptionsDialog(BillEntity bill) {
        String[] options = {"Xem chi tiết", "Chỉnh sửa hóa đơn", "Xóa hóa đơn", (bill.isPaid() ? "Đánh dấu chưa thanh toán" : "Đánh dấu đã thanh toán")};
        new AlertDialog.Builder(this)
            .setTitle("Tùy chọn hóa đơn")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    List<Object> list = billAdapter.getCurrentList();
                    for (Object obj : list) {
                        if (obj instanceof BillWithRoomAndTenant) {
                            BillWithRoomAndTenant wrap = (BillWithRoomAndTenant) obj;
                            if (wrap.bill.getId() == bill.getId()) {
                                showBillDetailDialog(wrap);
                                return;
                            }
                        }
                    }
                    BillWithRoomAndTenant manualWrap = new BillWithRoomAndTenant();
                    manualWrap.bill = bill;
                    manualWrap.room = currentRoom;
                    manualWrap.tenant = currentTenant;
                    showBillDetailDialog(manualWrap);
                } else if (which == 1) {
                    showAddBillDialog(bill);
                } else if (which == 2) {
                    confirmDeleteBill(bill);
                } else {
                    BillWithRoomAndTenant wrap = new BillWithRoomAndTenant();
                    wrap.bill = bill;
                    wrap.room = currentRoom;
                    wrap.tenant = currentTenant;
                    if (bill.isPaid()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận hủy thanh toán")
                                .setMessage("Bạn có chắc chắn muốn hủy trạng thái đã thanh toán của hóa đơn này?")
                                .setPositiveButton("Đồng ý", (d, w) -> performOptimisticUpdate(wrap))
                                .setNegativeButton("Hủy", null)
                                .show();
                    } else {
                        performOptimisticUpdate(wrap);
                    }
                }
            })
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
