package com.example.smartroommanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.dao.BillDao;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import java.util.List;

public class BillRepository {
    private final BillDao billDao;

    public BillRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        billDao = db.billDao();
    }

    public LiveData<List<BillEntity>> getBillsByRoom(int roomId) {
        return billDao.getBillsByRoom(roomId);
    }

    public LiveData<List<BillWithRoomAndTenant>> getBillsWithDetailsByRoom(int roomId) {
        return billDao.getBillsWithDetailsByRoom(roomId);
    }

    public LiveData<List<BillWithRoomAndTenant>> getAllBillsWithDetails() {
        return billDao.getAllBillsWithDetails();
    }

    public LiveData<BillWithRoomAndTenant> getBillWithDetailsById(int billId) {
        return billDao.getBillWithDetailsById(billId);
    }

    public void insert(BillEntity bill) {
        AppDatabase.databaseWriteExecutor.execute(() -> billDao.insert(bill));
    }

    public void update(BillEntity bill) {
        AppDatabase.databaseWriteExecutor.execute(() -> billDao.update(bill));
    }

    public void delete(BillEntity bill) {
        AppDatabase.databaseWriteExecutor.execute(() -> billDao.delete(bill));
    }
}
