package com.example.gymevo.ui.notes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentNotesBinding;
import com.example.gymevo.model.Note;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class NotesFragment extends Fragment {

    private FragmentNotesBinding binding;
    private NoteViewModel viewModel;
    private NoteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        binding = FragmentNotesBinding.inflate(inflater, container, false);

        adapter = new NoteAdapter(this::openNoteEditor, this::showNoteOptions);
        binding.recyclerNotes.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        binding.recyclerNotes.setAdapter(adapter);

        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.submitList(notes);
            boolean empty = notes == null || notes.isEmpty();
            binding.textNotesEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        binding.fabAddNote.setOnClickListener(v -> openNoteEditor(null));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openNoteEditor(@Nullable Note note) {
        boolean isNew = note == null;
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_note_edit, null);
        EditText titleEdit = dialogView.findViewById(R.id.edit_note_title);
        EditText contentEdit = dialogView.findViewById(R.id.edit_note_content);

        if (!isNew) {
            titleEdit.setText(note.getTitle());
            contentEdit.setText(note.getContent());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String title = titleEdit.getText().toString().trim();
                    String content = contentEdit.getText().toString().trim();
                    if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
                        return;
                    }
                    if (isNew) {
                        Note newNote = new Note();
                        newNote.setTitle(title);
                        newNote.setContent(content);
                        viewModel.insert(newNote);
                    } else {
                        note.setTitle(title);
                        note.setContent(content);
                        viewModel.update(note);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showNoteOptions(Note note) {
        String[] options = {
                getString(R.string.note_pin_toggle),
                getString(R.string.action_delete)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.togglePin(note);
                    } else if (which == 1) {
                        ConfirmDeleteDialog.show(
                                requireContext(),
                                R.string.note_delete_title,
                                R.string.note_delete_confirm,
                                () -> viewModel.delete(note)
                        );
                    }
                })
                .show();
    }
}
