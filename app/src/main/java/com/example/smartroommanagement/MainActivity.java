package com.example.smartroommanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartroommanagement.databinding.ActivityMainBinding;
import com.example.smartroommanagement.ui.activity.BillManagementActivity;
import com.example.smartroommanagement.ui.activity.ContractManagementActivity;
import com.example.smartroommanagement.ui.activity.EditNotesActivity;
import com.example.smartroommanagement.ui.activity.RoomListActivity;
import com.example.smartroommanagement.ui.activity.TenantListActivity;
import com.example.smartroommanagement.ui.activity.StatisticsActivity;
import com.example.smartroommanagement.ui.viewmodel.RoomViewModel;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private RoomViewModel roomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        setupGreeting();
        setupDashboard();
        observeStatistics();
    }

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) greeting = "Chào buổi sáng, Chủ trọ! ☀️";
        else if (hour < 18) greeting = "Chào buổi chiều, Chủ trọ! 🌤️";
        else greeting = "Chào buổi tối, Chủ trọ! 🌙";
        
        binding.textGreeting.setText(greeting);
    }

    private void observeStatistics() {
        roomViewModel.getAllRooms().observe(this, rooms -> {
            if (rooms != null) {
                int total = rooms.size();
                long occupied = rooms.stream().filter(r -> "Đã thuê".equals(r.getStatus())).count();
                long vacant = total - occupied;

                binding.textTotalRooms.setText(String.valueOf(total));
                binding.textOccupiedRooms.setText(String.valueOf(occupied));
                binding.textVacantRooms.setText(String.valueOf(vacant));
            }
        });
    }

    private void setupDashboard() {
        binding.cardRooms.setOnClickListener(v -> 
            startActivity(new Intent(this, RoomListActivity.class)));
        
        binding.cardTenants.setOnClickListener(v -> 
            startActivity(new Intent(this, TenantListActivity.class)));

        binding.cardBills.setOnClickListener(v -> 
            startActivity(new Intent(this, BillManagementActivity.class)));

        binding.cardStatistics.setOnClickListener(v -> 
            startActivity(new Intent(this, StatisticsActivity.class)));

        binding.cardContracts.setOnClickListener(v -> 
            startActivity(new Intent(this, ContractManagementActivity.class)));

        // CHUYỂN TỪ HỒ SƠ SANG GHI CHÚ PRO
        binding.cardNotes.setOnClickListener(v -> 
            startActivity(new Intent(this, EditNotesActivity.class)));

        binding.toolbar.setNavigationOnClickListener(v -> {
            showToast("Smart Room Management v1.0 PRO");
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
