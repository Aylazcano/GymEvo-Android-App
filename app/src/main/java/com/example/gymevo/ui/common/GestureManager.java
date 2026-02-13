package com.example.gymevo.ui.common;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Small reusable manager for detecting horizontal fling gestures.
 * - Uses ViewConfiguration for touch/flick thresholds
 * - Calls a single listener with +1 (forward) or -1 (backward)
 * - Keeps activity/fragment code clean and testable
 */
public class GestureManager {
    public interface OnSwipeListener {
        void onSwipeDelta(int delta);
    }

    private final GestureDetector gestureDetector;
    private final int touchSlop;
    private final int minFlingVelocity;

    public GestureManager(Context context, OnSwipeListener listener) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                // only horizontal flings
                if (Math.abs(dx) <= Math.abs(dy)) return false;
                if (Math.abs(dx) < touchSlop || Math.abs(velocityX) < minFlingVelocity) return false;
                int delta = dx < 0 ? 1 : -1;
                listener.onSwipeDelta(delta);
                return true;
            }
        });
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev);
    }

    public int getTouchSlop() { return touchSlop; }
    public int getMinFlingVelocity() { return minFlingVelocity; }
}
