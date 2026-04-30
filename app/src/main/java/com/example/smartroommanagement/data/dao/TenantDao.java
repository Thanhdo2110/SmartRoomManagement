package com.example.smartroommanagement.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.TenantWithRoom;

import java.util.List;

@Dao
public interface TenantDao {
    @Insert
    void insert(TenantEntity tenant);

    @Update
    void update(TenantEntity tenant);

    @Delete
    void delete(TenantEntity tenant);

    @Query("SELECT * FROM tenants WHERE roomId = :roomId AND userId = :userId")
    LiveData<List<TenantEntity>> getTenantsByRoom(int roomId, int userId);

    @Query("SELECT * FROM tenants WHERE userId = :userId")
    LiveData<List<TenantEntity>> getAllTenants(int userId);

    @Query("SELECT COUNT(*) FROM tenants WHERE roomId = :roomId AND userId = :userId")
    int countTenantsInRoom(int roomId, int userId);

    @Transaction
    @Query("SELECT * FROM tenants WHERE userId = :userId")
    LiveData<List<TenantWithRoom>> getAllTenantsWithRoom(int userId);
}
