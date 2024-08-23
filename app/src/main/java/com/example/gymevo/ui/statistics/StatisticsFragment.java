package com.example.gymevo.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gymevo.databinding.FragmentStaticticsBinding;

public class StatisticsFragment extends Fragment {

    private FragmentStaticticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StaticticsViewModel galleryViewModel =
                new ViewModelProvider(this).get(StaticticsViewModel.class);

        binding = FragmentStaticticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textStatistics;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}