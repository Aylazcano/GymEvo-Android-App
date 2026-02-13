package com.example.gymevo.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.example.gymevo.R;
import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.databinding.FragmentSettingsBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserPreferencesRepository userPreferencesRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private String currentThemeName = "default";
    private boolean suppressChanges;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        userPreferencesRepository = UserPreferencesRepository.getInstance(requireContext());
        currentThemeName = userPreferencesRepository.getThemeNameImmediate();
        selectTheme(currentThemeName);
        setupThemePicker();
        loadThemeSelection();
        updatePreview(currentThemeName);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }

    private void setupThemePicker() {
        binding.themeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressChanges) return;
            View checked = group.findViewById(checkedId);
            if (checked == null || checked.getTag() == null) return;
            String themeName = checked.getTag().toString();
            if (themeName.equalsIgnoreCase(currentThemeName)) return;

            // immediate visual feedback in the preview
            updatePreview(themeName);

            currentThemeName = themeName;
            disposables.add(userPreferencesRepository.setThemeName(themeName)
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> {
                            },
                            throwable -> {
                            }));
            if (isAdded()) {
                restartActivity();
            }
        });
    }

    private void loadThemeSelection() {
        disposables.add(userPreferencesRepository.getThemeName()
                .subscribeOn(Schedulers.io())
                .subscribe(themeName -> {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                if (binding == null) return;
                                currentThemeName = themeName;
                                selectTheme(themeName);
                                updatePreview(themeName);
                            });
                        },
                        throwable -> {
                        }));
    }

    private void selectTheme(String themeName) {
        if (binding == null) return;
        suppressChanges = true;
        Chip chip = findChipByTag(themeName);
        if (chip != null) {
            binding.themeChipGroup.check(chip.getId());
        }
        suppressChanges = false;
    }

    private Chip findChipByTag(String tag) {
        if (binding == null) return null;
        for (int i = 0; i < binding.themeChipGroup.getChildCount(); i++) {
            View child = binding.themeChipGroup.getChildAt(i);
            Object t = child.getTag();
            if (t != null && t.toString().equalsIgnoreCase(tag) && child instanceof Chip) {
                return (Chip) child;
            }
        }
        return null;
    }

    private void updatePreview(String themeName) {
        if (binding == null || themeName == null) return;

        int color = resolveThemePrimaryColor(themeName);

        // apply to preview swatch
        binding.themePreviewSample.setBackgroundColor(color);
        binding.themePreviewLabel.setText(getString(R.string.settings_theme_preview_format, resolveThemeLabel(themeName)));

        // choose readable text color (black or white) using luminance
        double luminance = ColorUtils.calculateLuminance(color);
        int textColor = luminance < 0.5d ? ContextCompat.getColor(requireContext(), android.R.color.white)
                : ContextCompat.getColor(requireContext(), android.R.color.black);
        binding.themePreviewLabel.setTextColor(textColor);
    }

    private int resolveThemePrimaryColor(@NonNull String themeName) {
        switch (themeName.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "red":
                return ContextCompat.getColor(requireContext(), R.color.gym_red_primary);
            case "green":
                return ContextCompat.getColor(requireContext(), R.color.gym_green_primary);
            case "blue":
                return ContextCompat.getColor(requireContext(), R.color.gym_blue_primary);
            case "gray":
                return ContextCompat.getColor(requireContext(), R.color.gym_gray_primary);
            case "default":
            default:
                return MaterialColors.getColor(binding.getRoot(), androidx.appcompat.R.attr.colorPrimary);
        }
    }

    private String resolveThemeLabel(@NonNull String themeName) {
        Chip chip = findChipByTag(themeName);
        if (chip != null && chip.getText() != null) {
            return chip.getText().toString();
        }
        return themeName;
    }

    private void restartActivity() {
        if (!isAdded()) return;
        requireActivity().recreate();
    }
}
