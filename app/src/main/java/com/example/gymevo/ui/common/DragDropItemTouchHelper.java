package com.example.gymevo.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class DragDropItemTouchHelper extends ItemTouchHelper.Callback {

    public interface ItemMoveListener {
        boolean onItemMove(int fromPosition, int toPosition);
    }

    public interface DragStateListener {
        void onDragEnd();
    }

    private final ItemMoveListener moveListener;
    private final DragStateListener dragStateListener;

    public DragDropItemTouchHelper(@NonNull ItemMoveListener moveListener,
                                   DragStateListener dragStateListener) {
        this.moveListener = moveListener;
        this.dragStateListener = dragStateListener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return moveListener != null
                && moveListener.onItemMove(viewHolder.getBindingAdapterPosition(),
                target.getBindingAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // No swipe support
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (dragStateListener != null) {
            dragStateListener.onDragEnd();
        }
    }
}
