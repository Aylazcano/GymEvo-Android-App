package com.example.gymevo.ui.calories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentCalorieTrackerBinding;
import com.example.gymevo.model.CalorieEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalorieTrackerFragment extends Fragment {

    private FragmentCalorieTrackerBinding binding;
    private CalorieViewModel viewModel;
    private MealAdapter adapter;
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalorieTrackerBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CalorieViewModel.class);

        adapter = new MealAdapter(this::showEditMealDialog, this::confirmDeleteMeal);
        binding.recyclerMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMeals.setAdapter(adapter);

        binding.btnPrevDay.setOnClickListener(v -> shiftDay(-1));
        binding.btnNextDay.setOnClickListener(v -> shiftDay(1));
        binding.fabAddMeal.setOnClickListener(v -> showAddMealDialog());

        observeData();
        return binding.getRoot();
    }

    private void observeData() {
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date ->
                binding.textDate.setText(date.format(dateFormatter)));

        viewModel.entries.observe(getViewLifecycleOwner(), entries -> {
            adapter.submitList(entries);
            boolean empty = entries == null || entries.isEmpty();
            binding.recyclerMeals.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            updateTotals(entries);
        });
    }

    private void updateTotals(List<CalorieEntry> entries) {
        int cal = 0, pro = 0, carb = 0, fat = 0;
        if (entries != null) {
            for (CalorieEntry e : entries) {
                cal  += e.getCalories();
                pro  += e.getProtein();
                carb += e.getCarbs();
                fat  += e.getFat();
            }
        }
        binding.textTotalCal.setText(String.valueOf(cal));
        binding.textTotalProtein.setText(getString(R.string.body_grams_format, pro));
        binding.textTotalCarbs.setText(getString(R.string.body_grams_format, carb));
        binding.textTotalFat.setText(getString(R.string.body_grams_format, fat));
    }

    private void shiftDay(int delta) {
        LocalDate current = viewModel.getSelectedDate().getValue();
        if (current == null) current = LocalDate.now();
        viewModel.setSelectedDate(current.plusDays(delta));
    }

    /* ── Add / Edit meal dialog ────────────────────────────────────────── */

    private void showAddMealDialog() {
        showMealDialog(null);
    }

    private void showEditMealDialog(CalorieEntry entry) {
        showMealDialog(entry);
    }

    private void showMealDialog(CalorieEntry existing) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_meal, null);

        com.google.android.material.textfield.TextInputEditText nameInput =
                view.findViewById(R.id.input_meal_name);
        com.google.android.material.textfield.TextInputEditText calInput =
                view.findViewById(R.id.input_calories);
        com.google.android.material.textfield.TextInputEditText proInput =
                view.findViewById(R.id.input_protein);
        com.google.android.material.textfield.TextInputEditText carbInput =
                view.findViewById(R.id.input_carbs);
        com.google.android.material.textfield.TextInputEditText fatInput =
                view.findViewById(R.id.input_fat);

        if (existing != null) {
            nameInput.setText(existing.getMealName());
            calInput.setText(String.valueOf(existing.getCalories()));
            if (existing.getProtein() > 0) proInput.setText(String.valueOf(existing.getProtein()));
            if (existing.getCarbs() > 0)   carbInput.setText(String.valueOf(existing.getCarbs()));
            if (existing.getFat() > 0)     fatInput.setText(String.valueOf(existing.getFat()));
        }

        int titleRes = existing != null ? R.string.cal_edit_meal : R.string.cal_add_meal;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String name = textOf(nameInput);
                    int cal = intOf(calInput);
                    int pro = intOf(proInput);
                    int carb = intOf(carbInput);
                    int fat = intOf(fatInput);

                    if (name.isEmpty() || cal <= 0) return;

                    if (existing != null) {
                        existing.setMealName(name);
                        existing.setCalories(cal);
                        existing.setProtein(pro);
                        existing.setCarbs(carb);
                        existing.setFat(fat);
                        viewModel.update(existing);
                    } else {
                        LocalDate date = viewModel.getSelectedDate().getValue();
                        if (date == null) date = LocalDate.now();
                        viewModel.insert(new CalorieEntry(name, cal, pro, carb, fat,
                                date.toEpochDay()));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDeleteMeal(CalorieEntry entry) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cal_delete_title)
                .setMessage(R.string.cal_delete_confirm)
                .setPositiveButton(android.R.string.ok, (d, w) -> viewModel.delete(entry))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /* ── util ──────────────────────────────────────────────────────────── */

    private static String textOf(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private static int intOf(com.google.android.material.textfield.TextInputEditText et) {
        String s = textOf(et);
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /* ── Adapter ───────────────────────────────────────────────────────── */

    interface OnEntryClickListener {
        void onClick(CalorieEntry entry);
    }

    private static class MealAdapter extends ListAdapter<CalorieEntry, MealAdapter.VH> {

        private final OnEntryClickListener editListener;
        private final OnEntryClickListener deleteListener;

        MealAdapter(OnEntryClickListener editListener, OnEntryClickListener deleteListener) {
            super(DIFF);
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calorie_entry, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CalorieEntry entry = getItem(position);
            holder.name.setText(entry.getMealName());
            holder.calories.setText(entry.getCalories() + " kcal");
            holder.macros.setText(String.format(Locale.getDefault(),
                    "P %dg · C %dg · F %dg",
                    entry.getProtein(), entry.getCarbs(), entry.getFat()));

            holder.itemView.setOnClickListener(v -> editListener.onClick(entry));
            holder.itemView.setOnLongClickListener(v -> {
                deleteListener.onClick(entry);
                return true;
            });
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView name, calories, macros;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.meal_name);
                calories = itemView.findViewById(R.id.meal_calories);
                macros = itemView.findViewById(R.id.meal_macros);
            }
        }

        private static final DiffUtil.ItemCallback<CalorieEntry> DIFF =
                new DiffUtil.ItemCallback<CalorieEntry>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull CalorieEntry a, @NonNull CalorieEntry b) {
                        return a.getId() != null && a.getId().equals(b.getId());
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull CalorieEntry a, @NonNull CalorieEntry b) {
                        return a.getCalories() == b.getCalories()
                                && a.getProtein() == b.getProtein()
                                && a.getCarbs() == b.getCarbs()
                                && a.getFat() == b.getFat()
                                && (a.getMealName() != null ? a.getMealName().equals(b.getMealName()) : b.getMealName() == null);
                    }
                };
    }
}
