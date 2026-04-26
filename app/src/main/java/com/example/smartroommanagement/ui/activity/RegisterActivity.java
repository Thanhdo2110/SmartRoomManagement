package com.example.smartroommanagement.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.entity.UserEntity;
import com.example.smartroommanagement.data.repository.UserRepository;
import com.example.smartroommanagement.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userRepository = new UserRepository(getApplication());

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.editFullname.getText().toString().trim();
            String user = binding.editUsername.getText().toString().trim();
            String pass = binding.editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                if (userRepository.isUsernameTaken(user)) {
                    runOnUiThread(() -> Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show());
                    return;
                }

                userRepository.register(new UserEntity(user, pass, name));
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}
