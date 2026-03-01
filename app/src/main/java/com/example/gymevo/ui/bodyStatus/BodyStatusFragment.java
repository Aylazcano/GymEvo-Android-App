package com.example.gymevo.ui.bodyStatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentBodyStatusBinding;

import java.util.Locale;

/**
 * Fragment that lets the user enter body metrics and displays calculated
 * BMI, BMR, TDEE, ideal weight range and recommended macros.
 */
public class BodyStatusFragment extends Fragment {

    private static final String PREFS_NAME = "body_status_prefs";
    private static final String KEY_GENDER  = "gender";   // "male" | "female"
    private static final String KEY_AGE     = "age";
    private static final String KEY_HEIGHT  = "height";   // cm
    private static final String KEY_WEIGHT  = "weight";   // kg
    private static final String KEY_ACTIVITY = "activity"; // chip id name

    private FragmentBodyStatusBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBodyStatusBinding.inflate(inflater, container, false);
        loadSavedData();
        binding.btnSave.setOnClickListener(v -> saveAndCalculate());
        return binding.getRoot();
    }

    /* ── persist / restore ─────────────────────────────────────────────── */

    private SharedPreferences prefs() {
        return requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void loadSavedData() {
        SharedPreferences p = prefs();
        String gender = p.getString(KEY_GENDER, "male");
        int age       = p.getInt(KEY_AGE, 0);
        float height  = p.getFloat(KEY_HEIGHT, 0f);
        float weight  = p.getFloat(KEY_WEIGHT, 0f);
        String activity = p.getString(KEY_ACTIVITY, "chip_moderate");

        if ("female".equals(gender)) {
            binding.toggleGender.check(R.id.btn_female);
        } else {
            binding.toggleGender.check(R.id.btn_male);
        }

        if (age > 0) binding.inputAge.setText(String.valueOf(age));
        if (height > 0) binding.inputHeight.setText(formatDecimal(height));
        if (weight > 0) binding.inputWeight.setText(formatDecimal(weight));

        checkActivityChip(activity);

        if (age > 0 && height > 0 && weight > 0) {
            calculate(gender, age, height, weight, activityFactor());
        }
    }

    private void saveAndCalculate() {
        String gender = binding.toggleGender.getCheckedButtonId() == R.id.btn_female
                ? "female" : "male";

        String ageStr = textOf(binding.inputAge);
        String heightStr = textOf(binding.inputHeight);
        String weightStr = textOf(binding.inputWeight);

        if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.body_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        float height, weight;
        try {
            age = Integer.parseInt(ageStr);
            height = Float.parseFloat(heightStr);
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.body_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }

        if (age <= 0 || height <= 0 || weight <= 0) {
            Toast.makeText(requireContext(), R.string.body_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }

        prefs().edit()
                .putString(KEY_GENDER, gender)
                .putInt(KEY_AGE, age)
                .putFloat(KEY_HEIGHT, height)
                .putFloat(KEY_WEIGHT, weight)
                .putString(KEY_ACTIVITY, activityChipName())
                .apply();

        calculate(gender, age, height, weight, activityFactor());
        Toast.makeText(requireContext(), R.string.body_saved, Toast.LENGTH_SHORT).show();
    }

    /* ── calculations ──────────────────────────────────────────────────── */

    private void calculate(String gender, int age, float heightCm, float weightKg,
                           float activityMultiplier) {
        // BMI
        float heightM = heightCm / 100f;
        float bmi = weightKg / (heightM * heightM);
        binding.valueBmi.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        binding.labelBmiCategory.setText(bmiCategory(bmi));

        // BMR (Mifflin-St Jeor)
        float bmr;
        if ("female".equals(gender)) {
            bmr = 10f * weightKg + 6.25f * heightCm - 5f * age - 161f;
        } else {
            bmr = 10f * weightKg + 6.25f * heightCm - 5f * age + 5f;
        }
        binding.valueBmr.setText(String.format(Locale.getDefault(), "%.0f", bmr));

        // TDEE
        float tdee = bmr * activityMultiplier;
        binding.valueTdee.setText(String.format(Locale.getDefault(), "%.0f", tdee));

        // Ideal weight (Devine formula)
        float idealLow, idealHigh;
        if ("female".equals(gender)) {
            idealLow  = 45.5f + 0.9f * (heightCm - 152.4f);
            idealHigh = idealLow * 1.10f;
        } else {
            idealLow  = 50f + 0.9f * (heightCm - 152.4f);
            idealHigh = idealLow * 1.10f;
        }
        if (idealLow < 30) idealLow = 30;
        if (idealHigh < idealLow) idealHigh = idealLow;
        binding.valueIdealWeight.setText(String.format(Locale.getDefault(),
                "%.0f–%.0f kg", idealLow, idealHigh));

        // Macros (balanced split: 30% protein, 40% carbs, 30% fat)
        float proteinCal = tdee * 0.30f;
        float carbsCal   = tdee * 0.40f;
        float fatCal     = tdee * 0.30f;
        int proteinG = Math.round(proteinCal / 4f);
        int carbsG   = Math.round(carbsCal / 4f);
        int fatG     = Math.round(fatCal / 9f);
        binding.valueProtein.setText(getString(R.string.body_grams_format, proteinG));
        binding.valueCarbs.setText(getString(R.string.body_grams_format, carbsG));
        binding.valueFat.setText(getString(R.string.body_grams_format, fatG));
    }

    private String bmiCategory(float bmi) {
        if (bmi < 18.5f) return getString(R.string.body_bmi_underweight);
        if (bmi < 25f)   return getString(R.string.body_bmi_normal);
        if (bmi < 30f)   return getString(R.string.body_bmi_overweight);
        return getString(R.string.body_bmi_obese);
    }

    /* ── activity helpers ──────────────────────────────────────────────── */

    private float activityFactor() {
        int checkedId = binding.chipGroupActivity.getCheckedChipId();
        if (checkedId == R.id.chip_sedentary) return 1.2f;
        if (checkedId == R.id.chip_light)     return 1.375f;
        if (checkedId == R.id.chip_moderate)  return 1.55f;
        if (checkedId == R.id.chip_very)      return 1.725f;
        if (checkedId == R.id.chip_extra)     return 1.9f;
        return 1.55f; // default moderate
    }

    private String activityChipName() {
        int checkedId = binding.chipGroupActivity.getCheckedChipId();
        if (checkedId == R.id.chip_sedentary) return "chip_sedentary";
        if (checkedId == R.id.chip_light)     return "chip_light";
        if (checkedId == R.id.chip_moderate)  return "chip_moderate";
        if (checkedId == R.id.chip_very)      return "chip_very";
        if (checkedId == R.id.chip_extra)     return "chip_extra";
        return "chip_moderate";
    }

    private void checkActivityChip(String name) {
        if (name == null) name = "chip_moderate";
        switch (name) {
            case "chip_sedentary": binding.chipGroupActivity.check(R.id.chip_sedentary); break;
            case "chip_light":    binding.chipGroupActivity.check(R.id.chip_light);    break;
            case "chip_very":     binding.chipGroupActivity.check(R.id.chip_very);     break;
            case "chip_extra":    binding.chipGroupActivity.check(R.id.chip_extra);    break;
            default:              binding.chipGroupActivity.check(R.id.chip_moderate); break;
        }
    }

    /* ── util ──────────────────────────────────────────────────────────── */

    private static String textOf(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private static String formatDecimal(float value) {
        if (value == (int) value) return String.valueOf((int) value);
        return String.format(Locale.US, "%.1f", value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
