package com.example.smartroommanagement.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartroommanagement.data.dao.BillDao;
import com.example.smartroommanagement.data.dao.RoomDao;
import com.example.smartroommanagement.data.dao.TenantDao;
import com.example.smartroommanagement.data.dao.UserDao;
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.data.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TĂNG VERSION LÊN 8 VÌ ĐÃ THÊM TRƯỜNG contractTerm VÀO TenantEntity
@Database(entities = {RoomEntity.class, TenantEntity.class, BillEntity.class, UserEntity.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RoomDao roomDao();
    public abstract TenantDao tenantDao();
    public abstract BillDao billDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_room_db")
                            .fallbackToDestructiveMigration() // Xóa dữ liệu cũ và tạo lại bảng mới khi đổi cấu trúc
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
