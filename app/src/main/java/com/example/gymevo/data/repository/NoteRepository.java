package com.example.gymevo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.local.NoteDao;
import com.example.gymevo.model.Note;

import java.util.List;

public class NoteRepository {

    private final NoteDao noteDao;

    public NoteRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        noteDao = db.noteDao();
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public void insert(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = noteDao.insert(note);
            note.setId(id);
        });
    }

    public void update(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> noteDao.delete(note));
    }
}
