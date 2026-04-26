package com.example.smartroommanagement.data.repository;

import android.app.Application;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.dao.UserDao;
import com.example.smartroommanagement.data.entity.UserEntity;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    public void register(UserEntity user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
    }

    public UserEntity login(String username, String password) {
        Future<UserEntity> future = AppDatabase.databaseWriteExecutor.submit(() -> userDao.login(username, password));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    public boolean isUsernameTaken(String username) {
        Future<UserEntity> future = AppDatabase.databaseWriteExecutor.submit(() -> userDao.getUserByUsername(username));
        try {
            return future.get() != null;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
}
