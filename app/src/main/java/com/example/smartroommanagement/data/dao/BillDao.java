package com.example.smartroommanagement.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;

import java.util.List;

@Dao
public interface BillDao {
    @Insert
    void insert(BillEntity bill);

    @Update
    void update(BillEntity bill);

    @Delete
    void delete(BillEntity bill);

    @Query("SELECT * FROM bills WHERE roomId = :roomId ORDER BY id DESC")
    LiveData<List<BillEntity>> getBillsByRoom(int roomId);

    @Transaction
    @Query("SELECT * FROM bills WHERE roomId = :roomId ORDER BY id DESC")
    LiveData<List<BillWithRoomAndTenant>> getBillsWithDetailsByRoom(int roomId);

    @Transaction
    @Query("SELECT * FROM bills ORDER BY id DESC")
    LiveData<List<BillWithRoomAndTenant>> getAllBillsWithDetails();

    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    LiveData<BillWithRoomAndTenant> getBillWithDetailsById(int billId);
}
