package com.example.smartroommanagement.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.RoomWithTenants;

import java.util.List;

@Dao
public interface RoomDao {
    @Insert
    void insert(RoomEntity room);

    @Update
    void update(RoomEntity room);

    @Delete
    void delete(RoomEntity room);

    @Query("SELECT * FROM rooms ORDER BY name ASC")
    LiveData<List<RoomEntity>> getAllRooms();

    @Query("SELECT * FROM rooms WHERE id = :id")
    LiveData<RoomEntity> getRoomById(int id);

    @Transaction
    @Query("SELECT * FROM rooms ORDER BY name ASC")
    LiveData<List<RoomWithTenants>> getRoomsWithTenants();
}
