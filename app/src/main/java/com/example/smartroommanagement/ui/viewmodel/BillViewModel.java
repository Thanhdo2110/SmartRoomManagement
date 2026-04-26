package com.example.smartroommanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.example.smartroommanagement.data.repository.BillRepository;
import java.util.List;

public class BillViewModel extends AndroidViewModel {
    private final BillRepository repository;

    public BillViewModel(@NonNull Application application) {
        super(application);
        repository = new BillRepository(application);
    }

    public LiveData<List<BillWithRoomAndTenant>> getAllBillsWithDetails() {
        return repository.getAllBillsWithDetails();
    }

    public void insert(BillEntity bill) {
        repository.insert(bill);
    }

    public void update(BillEntity bill) {
        repository.update(bill);
    }

    public void delete(BillEntity bill) {
        repository.delete(bill);
    }
}
