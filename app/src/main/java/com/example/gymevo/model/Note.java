package com.example.gymevo.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for a personal note (Google-Keep-style).
 */
@Entity(tableName = "note")
public class Note {

    @PrimaryKey(autoGenerate = true)
    @Nullable
    private Long id;

    private String title;
    private String content;
    private String color;
    private boolean pinned;
    private long createdAt;
    private long updatedAt;

    public Note(@Nullable Long id, String title, String content, String color,
                boolean pinned, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.color = color;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Note() {
        this(null, "", "", null, false, System.currentTimeMillis(), System.currentTimeMillis());
    }

    @Nullable
    public Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
