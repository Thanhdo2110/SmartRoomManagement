package com.example.smartroommanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.dao.BillDao;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.example.smartroommanagement.util.SessionManager;
import java.util.List;

public class BillRepository {
    private final BillDao billDao;
    private final SessionManager sessionManager;

    public BillRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        billDao = db.billDao();
        sessionManager = new SessionManager(application);
    }

    public LiveData<List<BillEntity>> getBillsByRoom(int roomId) {
        return billDao.getBillsByRoom(roomId, sessionManager.getUserId());
    }

    public LiveData<List<BillWithRoomAndTenant>> getBillsWithDetailsByRoom(int roomId) {
        return billDao.getBillsWithDetailsByRoom(roomId, sessionManager.getUserId());
    }

    public LiveData<List<BillWithRoomAndTenant>> getAllBillsWithDetails() {
        return billDao.getAllBillsWithDetails(sessionManager.getUserId());
    }

    public LiveData<BillWithRoomAndTenant> getBillWithDetailsById(int billId) {
        return billDao.getBillWithDetailsById(billId, sessionManager.getUserId());
    }

    public void insert(BillEntity bill) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            bill.setUserId(sessionManager.getUserId());
            billDao.insert(bill);
        });
    }

    public void update(BillEntity bill) {
        AppDatabase.databaseWriteExecutor.execute(() -> billDao.update(bill));
    }

    public void delete(BillEntity bill) {
        AppDatabase.databaseWriteExecutor.execute(() -> billDao.delete(bill));
    }
}
