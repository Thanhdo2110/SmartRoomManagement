package com.example.smartroommanagement.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartroommanagement.data.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    UserEntity login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);
}
