package com.example.smartroommanagement.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.dao.RoomDao;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.RoomWithTenants;

import java.util.List;

public class RoomRepository {
    private final RoomDao roomDao;
    private final LiveData<List<RoomEntity>> allRooms;

    public RoomRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        roomDao = db.roomDao();
        allRooms = roomDao.getAllRooms();
    }

    public LiveData<List<RoomEntity>> getAllRooms() {
        return allRooms;
    }

    public void insert(RoomEntity room) {
        AppDatabase.databaseWriteExecutor.execute(() -> roomDao.insert(room));
    }

    public void update(RoomEntity room) {
        AppDatabase.databaseWriteExecutor.execute(() -> roomDao.update(room));
    }

    public void delete(RoomEntity room) {
        AppDatabase.databaseWriteExecutor.execute(() -> roomDao.delete(room));
    }

    public LiveData<RoomEntity> getRoomById(int id) {
        return roomDao.getRoomById(id);
    }

    public LiveData<List<RoomWithTenants>> getRoomsWithTenants() {
        return roomDao.getRoomsWithTenants();
    }
}
