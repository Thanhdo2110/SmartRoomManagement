package com.example.smartroommanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.dao.TenantDao;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.TenantWithRoom;
import com.example.smartroommanagement.util.SessionManager;
import java.util.List;

public class TenantRepository {
    private final TenantDao tenantDao;
    private final SessionManager sessionManager;

    public TenantRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        tenantDao = db.tenantDao();
        sessionManager = new SessionManager(application);
    }

    public LiveData<List<TenantEntity>> getTenantsByRoom(int roomId) {
        return tenantDao.getTenantsByRoom(roomId, sessionManager.getUserId());
    }

    public LiveData<List<TenantEntity>> getAllTenants() {
        return tenantDao.getAllTenants(sessionManager.getUserId());
    }

    public LiveData<List<TenantWithRoom>> getAllTenantsWithRoom() {
        return tenantDao.getAllTenantsWithRoom(sessionManager.getUserId());
    }

    public int countTenantsInRoom(int roomId) {
        return tenantDao.countTenantsInRoom(roomId, sessionManager.getUserId());
    }

    public void insert(TenantEntity tenant) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            tenant.setUserId(sessionManager.getUserId());
            tenantDao.insert(tenant);
        });
    }

    public void update(TenantEntity tenant) {
        AppDatabase.databaseWriteExecutor.execute(() -> tenantDao.update(tenant));
    }

    public void delete(TenantEntity tenant) {
        AppDatabase.databaseWriteExecutor.execute(() -> tenantDao.delete(tenant));
    }
}
