package com.example.smartroommanagement.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartroommanagement.data.entity.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY id DESC")
    List<NoteEntity> getNotesByUserId(int userId);

    @Delete
    void delete(NoteEntity note);
}
