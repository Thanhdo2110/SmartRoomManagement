package com.example.smartroommanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.RoomWithTenants;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.TenantWithRoom;
import com.example.smartroommanagement.data.entity.UserEntity;
import com.example.smartroommanagement.data.repository.BillRepository;
import com.example.smartroommanagement.data.repository.RoomRepository;
import com.example.smartroommanagement.data.repository.TenantRepository;
import java.util.List;

public class RoomDetailViewModel extends AndroidViewModel {
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final BillRepository billRepository;
    private final AppDatabase db;

    public RoomDetailViewModel(@NonNull Application application) {
        super(application);
        roomRepository = new RoomRepository(application);
        tenantRepository = new TenantRepository(application);
        billRepository = new BillRepository(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<UserEntity> getUserById(int userId) {
        MutableLiveData<UserEntity> userLiveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = db.userDao().getUserById(userId);
            userLiveData.postValue(user);
        });
        return userLiveData;
    }

    public void updateUser(UserEntity user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.userDao().update(user);
        });
    }

    public LiveData<RoomEntity> getRoomById(int id) {
        return roomRepository.getRoomById(id);
    }

    public void updateRoom(RoomEntity room) {
        roomRepository.update(room);
    }

    public LiveData<List<TenantEntity>> getTenantsByRoom(int roomId) {
        return tenantRepository.getTenantsByRoom(roomId);
    }

    public LiveData<List<TenantEntity>> getAllTenants() {
        return tenantRepository.getAllTenants();
    }

    public LiveData<List<TenantWithRoom>> getAllTenantsWithRoom() {
        return tenantRepository.getAllTenantsWithRoom();
    }

    public void addTenant(TenantEntity tenant) {
        tenantRepository.insert(tenant);
    }

    public void updateTenant(TenantEntity tenant) {
        tenantRepository.update(tenant);
    }

    public void deleteTenant(TenantEntity tenant) {
        tenantRepository.delete(tenant);
    }

    public LiveData<Boolean> hasTenantsInRoom(int roomId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() ->
                result.postValue(tenantRepository.countTenantsInRoom(roomId) > 0));
        return result;
    }

    public LiveData<List<BillEntity>> getBillsByRoom(int roomId) {
        return billRepository.getBillsByRoom(roomId);
    }

    public LiveData<List<BillWithRoomAndTenant>> getBillsWithDetailsByRoom(int roomId) {
        return billRepository.getBillsWithDetailsByRoom(roomId);
    }

    public LiveData<List<BillWithRoomAndTenant>> getAllBillsWithDetails() {
        return billRepository.getAllBillsWithDetails();
    }

    public LiveData<BillWithRoomAndTenant> getBillWithDetailsById(int billId) {
        return billRepository.getBillWithDetailsById(billId);
    }

    public void addBill(BillEntity bill) {
        billRepository.insert(bill);
    }

    public void updateBill(BillEntity bill) {
        billRepository.update(bill);
    }

    public void deleteBill(BillEntity bill) {
        billRepository.delete(bill);
    }

    public LiveData<List<RoomWithTenants>> getRoomsWithTenants() {
        return roomRepository.getRoomsWithTenants();
    }
}
