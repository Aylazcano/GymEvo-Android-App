package com.example.gymevo.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gymevo.R;
import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.databinding.FragmentSettingsBinding;
import com.example.gymevo.ui.common.ThemeUtils;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserPreferencesRepository userPreferencesRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private String currentThemeName = ThemeUtils.THEME_DEFAULT;
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
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }

    private void setupThemePicker() {
        binding.themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressChanges) {
                return;
            }
            String themeName = mapThemeName(checkedId);
            if (themeName == null || themeName.equalsIgnoreCase(currentThemeName)) {
                return;
            }
            currentThemeName = themeName;
            disposables.add(userPreferencesRepository.setThemeName(themeName)
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> {
                                if (!isAdded()) {
                                    return;
                                }
                                requireActivity().runOnUiThread(this::restartActivity);
                            },
                            throwable -> {
                            }));
        });
    }

    private void loadThemeSelection() {
        disposables.add(userPreferencesRepository.getThemeName()
                .subscribeOn(Schedulers.io())
                .subscribe(themeName -> {
                            if (!isAdded()) {
                                return;
                            }
                            requireActivity().runOnUiThread(() -> {
                                if (binding == null) {
                                    return;
                                }
                                currentThemeName = themeName;
                                selectTheme(themeName);
                            });
                        },
                        throwable -> {
                        }));
    }

    private void selectTheme(String themeName) {
        if (binding == null) {
            return;
        }
        suppressChanges = true;
        int checkedId = getThemeId(themeName);
        if (checkedId != View.NO_ID) {
            binding.themeRadioGroup.check(checkedId);
        }
        suppressChanges = false;
    }

    private int getThemeId(String themeName) {
        if (ThemeUtils.THEME_RED.equalsIgnoreCase(themeName)) {
            return R.id.theme_red;
        }
        if (ThemeUtils.THEME_GREEN.equalsIgnoreCase(themeName)) {
            return R.id.theme_green;
        }
        if (ThemeUtils.THEME_BLUE.equalsIgnoreCase(themeName)) {
            return R.id.theme_blue;
        }
        return R.id.theme_default;
    }

    private String mapThemeName(int checkedId) {
        if (checkedId == R.id.theme_red) {
            return ThemeUtils.THEME_RED;
        }
        if (checkedId == R.id.theme_green) {
            return ThemeUtils.THEME_GREEN;
        }
        if (checkedId == R.id.theme_blue) {
            return ThemeUtils.THEME_BLUE;
        }
        if (checkedId == R.id.theme_default) {
            return ThemeUtils.THEME_DEFAULT;
        }
        return null;
    }

    private void restartActivity() {
        if (!isAdded()) {
            return;
        }
        suppressChanges = true;
        Intent intent = requireActivity().getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        requireActivity().finish();
    }
}
