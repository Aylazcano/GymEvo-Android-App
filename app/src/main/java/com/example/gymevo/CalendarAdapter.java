package com.example.gymevo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final Context context;  // Make sure context is defined as a class variable


    public CalendarAdapter(Context context, ArrayList<String> daysOfMonth, OnItemListener onItemListener) {
        this.context = context;  // Initialize context here
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_day_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.1666666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.dayTextView.setText(daysOfMonth.get(position));

        // La hauteur est fixée pour chaque item
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        params.height = (int) (40 * holder.itemView.getContext().getResources().getDisplayMetrics().density); // Convert dp to pixels
        holder.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }



    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}
