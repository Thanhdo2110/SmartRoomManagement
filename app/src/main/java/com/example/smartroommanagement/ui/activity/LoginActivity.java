package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartroommanagement.MainActivity;
import com.example.smartroommanagement.databinding.ActivityLoginBinding;
import com.example.smartroommanagement.ui.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        authViewModel.getUserLiveData().observe(this, user -> {
            binding.progressBar.setVisibility(View.GONE);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String user = binding.editUsername.getText().toString().trim();
            String pass = binding.editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            authViewModel.login(user, pass);
        });

        binding.textRegister.setOnClickListener(v -> 
            startActivity(new Intent(this, RegisterActivity.class)));
    }
}
