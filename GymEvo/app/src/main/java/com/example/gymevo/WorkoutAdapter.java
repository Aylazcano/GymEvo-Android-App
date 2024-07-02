package com.example.gymevo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.models.Workout;

import java.util.ArrayList;
import java.util.List;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder>{
    public List<Workout> workoutList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView exerciceNameTV;
        public TextView musclesTargetsTV;
        public TextView seriesQtyTV;
        public TextView repetitionsQtyTV;
        public TextView weightNumTV;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            exerciceNameTV = view.findViewById(R.id.ExerciceNameTV);
            musclesTargetsTV = view.findViewById(R.id.MusclesTargetsTV);
            seriesQtyTV = view.findViewById(R.id.SeriesQtyTV);
            repetitionsQtyTV = view.findViewById(R.id.RepetitionsQtyTV);
            weightNumTV = view.findViewById(R.id.WeightNumTV);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WorkoutAdapter() {
        workoutList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.exercice_recycler_view, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Workout currentWorkout = workoutList.get(position);
        viewHolder.exerciceNameTV.setText(currentWorkout.Exercice.ExerciceName);
        viewHolder.musclesTargetsTV.setText(currentWorkout.Exercice.MusclesTargets);
        viewHolder.seriesQtyTV.setText(currentWorkout.Series);
        viewHolder.repetitionsQtyTV.setText(currentWorkout.Repetitions);
        viewHolder.weightNumTV.setText(currentWorkout.Weight);

//        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int SelectedExercicePosition = viewHolder.getLayoutPosition();
//                Exercice selectedTask = exerciceList.get(SelectedExercicePosition);
//                Intent intent = new Intent(view.getContext(),ExerciceConsultationActivity.class);
//                intent.putExtra("selectedTaskId", selectedTask.id);
//                view.getContext().startActivity(intent);
//            }
//        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return workoutList.size();
    }
}
