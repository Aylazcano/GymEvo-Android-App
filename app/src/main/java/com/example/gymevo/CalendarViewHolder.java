package com.example.gymevo;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;

    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener) {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.dayCellText);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }

//    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        private static final int SWIPE_THRESHOLD = 100;
//        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
//        private final Context context;
//
//        public GestureListener(Context context) {
//            this.context = context;  // Initialize context here
//        }
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            float diffY = e2.getY() - e1.getY();
//            float diffX = e2.getX() - e1.getX();
//            if (Math.abs(diffX) > Math.abs(diffY)) {
//                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                    if (diffX > 0) {
//                        onSwipeRight();
//                    } else {
//                        onSwipeLeft();
//                    }
//                    return true;
//                }
//            } else {
//                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                    if (diffY > 0) {
//                        onSwipeDown();
//                    } else {
//                        onSwipeUp();
//                    }
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        private void onSwipeRight() {
//            Toast.makeText(context, "Swipe Right", Toast.LENGTH_SHORT).show();
//        }
//
//        private void onSwipeLeft() {
//            Toast.makeText(context, "Swipe Left", Toast.LENGTH_SHORT).show();
//        }
//
//        private void onSwipeUp() {
//            Toast.makeText(context, "Swipe Up", Toast.LENGTH_SHORT).show();
//        }
//
//        private void onSwipeDown() {
//            Toast.makeText(context, "Swipe Down", Toast.LENGTH_SHORT).show();
//        }
//    }
}
