package com.example.gymevo.ui.common;

import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public final class FastScrollDragHelper {

    public interface SectionTextProvider {
        @NonNull
        String getSectionText(int position);
    }

    private static final long POPUP_HIDE_DELAY_MS = 1500L;
    private static final long POPUP_HIDE_AFTER_DRAG_MS = 700L;
    private static final float TOUCH_AREA_WIDTH_DP = 32f;

    private final RecyclerView recyclerView;
    private final TextView popupView;
    private final SectionTextProvider sectionTextProvider;
    private final Handler popupHandler = new Handler(Looper.getMainLooper());
    private final Runnable hidePopup;

    private boolean isFastScrolling;
    private RecyclerView.OnScrollListener onScrollListener;
    private RecyclerView.OnItemTouchListener onItemTouchListener;

    public FastScrollDragHelper(@NonNull RecyclerView recyclerView,
                                @NonNull TextView popupView,
                                @NonNull SectionTextProvider sectionTextProvider) {
        this.recyclerView = recyclerView;
        this.popupView = popupView;
        this.sectionTextProvider = sectionTextProvider;
        this.hidePopup = () -> this.popupView.setVisibility(View.GONE);
    }

    public void attach() {
        detach();

        onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!isFastScrolling) {
                    return;
                }
                RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
                if (!(layoutManager instanceof LinearLayoutManager)) {
                    return;
                }
                int position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                showPopupAt(position, false);
            }
        };
        recyclerView.addOnScrollListener(onScrollListener);

        onItemTouchListener = new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isTouchOnFastScrollArea(event) || rv.getAdapter() == null
                                || rv.getAdapter().getItemCount() == 0
                                || isTouchOnInteractiveChild(rv, event)) {
                            return false;
                        }
                        isFastScrolling = true;
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        scrollToTouchPosition(event.getY());
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        return isFastScrolling;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        return isFastScrolling;

                    default:
                        return false;
                }
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        if (isFastScrolling) {
                            scrollToTouchPosition(event.getY());
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!isFastScrolling) {
                            return;
                        }
                        isFastScrolling = false;
                        popupHandler.removeCallbacks(hidePopup);
                        popupHandler.postDelayed(hidePopup, POPUP_HIDE_AFTER_DRAG_MS);
                        rv.getParent().requestDisallowInterceptTouchEvent(false);
                        break;

                    default:
                        break;
                }
            }
        };
        recyclerView.addOnItemTouchListener(onItemTouchListener);
    }

    public void detach() {
        if (onScrollListener != null) {
            recyclerView.removeOnScrollListener(onScrollListener);
            onScrollListener = null;
        }
        if (onItemTouchListener != null) {
            recyclerView.removeOnItemTouchListener(onItemTouchListener);
            onItemTouchListener = null;
        }
        popupHandler.removeCallbacks(hidePopup);
        popupView.setVisibility(View.GONE);
        isFastScrolling = false;
    }

    private void scrollToTouchPosition(float touchY) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (!(layoutManager instanceof LinearLayoutManager) || adapter == null) {
            return;
        }

        int itemCount = adapter.getItemCount();
        if (itemCount <= 0) {
            return;
        }

        int top = recyclerView.getPaddingTop();
        int bottom = recyclerView.getHeight() - recyclerView.getPaddingBottom();
        int availableHeight = Math.max(1, bottom - top);
        float clampedY = Math.max(top, Math.min(touchY, bottom));
        float proportion = (clampedY - top) / availableHeight;
        int targetPosition = Math.min(itemCount - 1,
                Math.max(0, Math.round(proportion * (itemCount - 1))));

        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(targetPosition, 0);
        showPopupAt(targetPosition, false);
    }

    private boolean isTouchOnFastScrollArea(@NonNull MotionEvent event) {
        int scrollRange = recyclerView.computeVerticalScrollRange();
        int scrollExtent = recyclerView.computeVerticalScrollExtent();
        if (scrollRange <= scrollExtent) {
            return false;
        }

        float touchAreaWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                TOUCH_AREA_WIDTH_DP,
                recyclerView.getResources().getDisplayMetrics());
        float minX = recyclerView.getWidth() - recyclerView.getPaddingRight() - touchAreaWidth;
        return event.getX() >= minX;
    }

    private boolean isTouchOnInteractiveChild(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
        View row = rv.findChildViewUnder(event.getX(), event.getY());
        if (row == null) {
            return false;
        }
        float xInRow = event.getX() - row.getLeft();
        float yInRow = event.getY() - row.getTop();
        View touched = findTouchedView(row, xInRow, yInRow);
        if (touched == null || touched == row) {
            return false;
        }
        return touched.isClickable() || touched.isLongClickable() || touched.isFocusable();
    }

    private View findTouchedView(@NonNull View view, float x, float y) {
        if (x < 0 || y < 0 || x > view.getWidth() || y > view.getHeight()) {
            return null;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                float childX = x - child.getLeft();
                float childY = y - child.getTop();
                View touchedChild = findTouchedView(child, childX, childY);
                if (touchedChild != null) {
                    return touchedChild;
                }
            }
        }
        return view;
    }

    private void showPopupAt(int position, boolean delayedHide) {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null || adapter.getItemCount() == 0) {
            return;
        }
        int safePosition = Math.max(0, Math.min(position, adapter.getItemCount() - 1));
        String text = sectionTextProvider.getSectionText(safePosition);
        if (text == null || text.isEmpty()) {
            popupHandler.removeCallbacks(hidePopup);
            popupView.setVisibility(View.GONE);
            return;
        }

        popupView.setText(text);
        popupView.setVisibility(View.VISIBLE);
        popupHandler.removeCallbacks(hidePopup);
        if (delayedHide) {
            popupHandler.postDelayed(hidePopup, POPUP_HIDE_DELAY_MS);
        }
    }
}
