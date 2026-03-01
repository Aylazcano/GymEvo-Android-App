package com.example.gymevo.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gymevo.model.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM note ORDER BY pinned DESC, updatedAt DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    Note getById(long id);
}
