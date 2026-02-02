package com.example.gymevo.ui.common.calendar;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView dayTextView;
    private final View indicatorView;
    private final CalendarAdapter.OnItemListener onItemListener;
    private java.time.LocalDate cellDate;

    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener) {
        super(itemView);
        dayTextView = itemView.findViewById(R.id.dayCellText);
        indicatorView = itemView.findViewById(R.id.dayCellIndicator);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (cellDate == null || onItemListener == null) {
            return;
        }
        int position = getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            onItemListener.onItemClick(position, dayTextView.getText().toString(), cellDate);
        }
    }

    void setDayText(String dayText) {
        dayTextView.setText(dayText);
    }

    void setState(boolean selected, boolean today) {
        int bgColor = selected
                ? ContextCompat.getColor(itemView.getContext(), R.color.example_1_selection_color)
                : ContextCompat.getColor(itemView.getContext(), android.R.color.transparent);
        int textColor = selected
                ? ContextCompat.getColor(itemView.getContext(), R.color.black)
                : (today
                    ? ContextCompat.getColor(itemView.getContext(), R.color.example_1_selection_color)
                    : ContextCompat.getColor(itemView.getContext(), R.color.white));

        itemView.setBackgroundColor(bgColor);
        dayTextView.setTextColor(textColor);
        dayTextView.setTypeface(dayTextView.getTypeface(), today ? Typeface.BOLD : Typeface.NORMAL);
    }

    void setIndicatorVisible(boolean visible, boolean selected) {
        if (indicatorView == null) {
            return;
        }
        indicatorView.setVisibility(visible ? View.VISIBLE : View.GONE);
        int indicatorColor = ContextCompat.getColor(itemView.getContext(),
                selected ? R.color.black : R.color.example_1_selection_color);
        indicatorView.setBackgroundTintList(ColorStateList.valueOf(indicatorColor));
    }

    void setCellDate(java.time.LocalDate date) {
        this.cellDate = date;
    }
}
