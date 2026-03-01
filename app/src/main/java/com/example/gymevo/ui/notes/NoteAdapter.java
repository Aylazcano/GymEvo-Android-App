package com.example.gymevo.ui.notes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note);
    }

    private final OnNoteClickListener clickListener;
    private final OnNoteLongClickListener longClickListener;

    public NoteAdapter(OnNoteClickListener clickListener, OnNoteLongClickListener longClickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getUpdatedAt() == newItem.getUpdatedAt()
                    && oldItem.isPinned() == newItem.isPinned()
                    && TextUtils.equals(oldItem.getTitle(), newItem.getTitle())
                    && TextUtils.equals(oldItem.getContent(), newItem.getContent());
        }
    };

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = getItem(position);
        holder.bind(note);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onNoteClick(note);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onNoteLongClick(note);
            return true;
        });
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView contentText;
        private final TextView dateText;
        private final ImageView pinIcon;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_note_title);
            contentText = itemView.findViewById(R.id.text_note_content);
            dateText = itemView.findViewById(R.id.text_note_date);
            pinIcon = itemView.findViewById(R.id.icon_pin);
        }

        void bind(Note note) {
            String title = note.getTitle();
            if (TextUtils.isEmpty(title)) {
                titleText.setVisibility(View.GONE);
            } else {
                titleText.setVisibility(View.VISIBLE);
                titleText.setText(title);
            }

            String content = note.getContent();
            if (TextUtils.isEmpty(content)) {
                contentText.setVisibility(View.GONE);
            } else {
                contentText.setVisibility(View.VISIBLE);
                contentText.setText(content);
            }

            pinIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
            dateText.setText(dateFormat.format(new Date(note.getUpdatedAt())));
        }
    }
}
