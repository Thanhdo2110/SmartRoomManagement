package com.example.smartroommanagement.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartroommanagement.MainActivity;
import com.example.smartroommanagement.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // ĐÃ TẠM ẨN ĐĂNG NHẬP ĐỂ TEST NHANH
            // Sau khi hoàn thiện, hãy đổi lại thành LoginActivity.class
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 500); // Giảm thời gian chờ xuống 0.5 giây để vào app nhanh hơn
    }
}
