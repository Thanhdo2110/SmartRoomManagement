package com.example.smartroommanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.dao.TenantDao;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.TenantWithRoom;
import java.util.List;

public class TenantRepository {
    private final TenantDao tenantDao;

    public TenantRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        tenantDao = db.tenantDao();
    }

    public LiveData<List<TenantEntity>> getTenantsByRoom(int roomId) {
        return tenantDao.getTenantsByRoom(roomId);
    }

    public LiveData<List<TenantEntity>> getAllTenants() {
        return tenantDao.getAllTenants();
    }

    public LiveData<List<TenantWithRoom>> getAllTenantsWithRoom() {
        return tenantDao.getAllTenantsWithRoom();
    }

    public void insert(TenantEntity tenant) {
        AppDatabase.databaseWriteExecutor.execute(() -> tenantDao.insert(tenant));
    }

    public void update(TenantEntity tenant) {
        AppDatabase.databaseWriteExecutor.execute(() -> tenantDao.update(tenant));
    }

    public void delete(TenantEntity tenant) {
        AppDatabase.databaseWriteExecutor.execute(() -> tenantDao.delete(tenant));
    }
}
