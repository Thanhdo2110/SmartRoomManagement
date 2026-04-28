package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.entity.UserEntity;
import com.example.smartroommanagement.databinding.ActivityProfileBinding;
import com.example.smartroommanagement.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private UserEntity currentUser;
    private AlertDialog activeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        loadUserData();
        setupListeners();
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = AppDatabase.getDatabase(this).userDao().getUserById(userId);
            if (user != null) {
                runOnUiThread(() -> {
                    currentUser = user;
                    binding.textFullName.setText(user.getFullName());
                    binding.textUsername.setText("@" + user.getUsername());
                });
            }
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // HIỂN THỊ DIALOG ĐỔI MẬT KHẨU
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // CHUYỂN SANG MÀN HÌNH GHI CHÚ
        binding.btnEditNotes.setOnClickListener(v -> 
            startActivity(new Intent(this, EditNotesActivity.class)));

        // HIỂN THỊ DIALOG ĐĂNG XUẤT
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void dismissActiveDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }
        activeDialog = null;
    }

    private void showChangePasswordDialog() {
        if (isFinishing() || isDestroyed()) return;
        dismissActiveDialog(); // Đóng mọi dialog cũ để tránh chồng lấp

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        final TextInputEditText editOld = view.findViewById(R.id.edit_old_password);
        final TextInputEditText editNew = view.findViewById(R.id.edit_new_password);

        activeDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Bảo mật & Mật khẩu")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create();

        activeDialog.show();

        // Ghi đè sự kiện nút Cập nhật để kiểm tra dữ liệu mà không đóng dialog nếu sai
        activeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đợi hệ thống tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                loadUserData();
                return;
            }

            String oldP = editOld.getText().toString().trim();
            String newP = editNew.getText().toString().trim();

            if (oldP.isEmpty() || newP.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (oldP.equals(currentUser.getPassword())) {
                updatePassword(newP);
                activeDialog.dismiss();
            } else {
                Toast.makeText(this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        if (isFinishing() || isDestroyed()) return;
        dismissActiveDialog();

        activeDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Quay lại", (dialog, which) -> dialog.dismiss())
                .create();
        
        activeDialog.show();
    }

    private void updatePassword(String newPass) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser.setPassword(newPass);
            AppDatabase.getDatabase(this).userDao().update(currentUser);
            sessionManager.updateSavedPassword(newPass);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void performLogout() {
        sessionManager.logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissActiveDialog(); // Tránh rò rỉ bộ nhớ và chồng lấp khi quay lại activity
    }
}
