package com.example.smartroommanagement.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.repository.RoomRepository;

import java.util.List;

public class RoomViewModel extends AndroidViewModel {
    private final RoomRepository repository;
    private final LiveData<List<RoomEntity>> allRooms;

    public RoomViewModel(@NonNull Application application) {
        super(application);
        repository = new RoomRepository(application);
        allRooms = repository.getAllRooms();
    }

    public LiveData<List<RoomEntity>> getAllRooms() {
        return allRooms;
    }

    public void insert(RoomEntity room) {
        repository.insert(room);
    }

    public void update(RoomEntity room) {
        repository.update(room);
    }

    public void delete(RoomEntity room) {
        repository.delete(room);
    }

    public LiveData<RoomEntity> getRoomById(int id) {
        return repository.getRoomById(id);
    }
}
