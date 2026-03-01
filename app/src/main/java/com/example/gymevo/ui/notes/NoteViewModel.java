package com.example.gymevo.ui.notes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gymevo.data.repository.NoteRepository;
import com.example.gymevo.model.Note;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        note.setUpdatedAt(System.currentTimeMillis());
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void togglePin(Note note) {
        note.setPinned(!note.isPinned());
        note.setUpdatedAt(System.currentTimeMillis());
        repository.update(note);
    }
}
