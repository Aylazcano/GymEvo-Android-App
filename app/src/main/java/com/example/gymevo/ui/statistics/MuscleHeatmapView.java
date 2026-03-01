package com.example.gymevo.ui.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gymevo.R;
import com.google.android.material.color.MaterialColors;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom view that renders a front and back body silhouette with muscle zones
 * coloured by workout volume (heatmap). Each zone maps to one or more
 * {@link com.example.gymevo.model.MuscleGroup} labels.
 */
public class MuscleHeatmapView extends View {

    /* ── virtual canvas units ──────────────────────────────────────────── */
    private static final float CW = 220f;
    private static final float CH = 250f;
    private static final float BODY_OY = 26f;
    private static final float FRONT_OX = 12f;
    private static final float BACK_OX = 120f;

    /* ── heat-map colour stops ─────────────────────────────────────────── */
    private static final int COLOR_INACTIVE_FALLBACK = 0xFFE0E0E0;
    private static final int[] HEAT = {
            0xFFBBDEFB,   // light blue   (low)
            0xFF4CAF50,   // green        (moderate)
            0xFFFF9800,   // orange       (high)
            0xFFF44336    // red          (max)
    };

    /* ── zone definition ───────────────────────────────────────────────── */
    private static final class Z {
        final float l, t, r, b, rx, ry;
        final boolean oval;
        final String[] muscles;

        Z(float l, float t, float r, float b,
          float rx, float ry, boolean oval, String... muscles) {
            this.l = l; this.t = t; this.r = r; this.b = b;
            this.rx = rx; this.ry = ry; this.oval = oval;
            this.muscles = muscles;
        }
    }

    /* Coordinates relative to body origin (0,0)-(80,200) */
    private static final Z[] FRONT = {
            // head (no muscle mapping – always neutral)
            new Z(28, 0, 52, 26, 12, 13, true),
            // neck
            new Z(34, 25, 46, 36, 2, 2, false, "Neck"),
            // left shoulder
            new Z(6, 35, 22, 50, 4, 4, false,
                    "Shoulders", "Front delts", "Lateral delts"),
            // right shoulder
            new Z(58, 35, 74, 50, 4, 4, false,
                    "Shoulders", "Front delts", "Lateral delts"),
            // chest
            new Z(22, 35, 58, 66, 3, 3, false,
                    "Chest", "Upper chest", "Lower chest"),
            // left bicep
            new Z(2, 49, 19, 84, 4, 4, false, "Arms", "Biceps"),
            // right bicep
            new Z(61, 49, 78, 84, 4, 4, false, "Arms", "Biceps"),
            // left forearm
            new Z(-1, 83, 16, 118, 3, 3, false, "Forearms"),
            // right forearm
            new Z(64, 83, 81, 118, 3, 3, false, "Forearms"),
            // core / abs
            new Z(24, 65, 56, 96, 3, 3, false, "Abs", "Obliques"),
            // hip flexors
            new Z(22, 95, 58, 112, 4, 4, false, "Hip flexors"),
            // left quadricep
            new Z(22, 111, 38, 160, 4, 4, false,
                    "Legs", "Quadriceps", "Adductors"),
            // right quadricep
            new Z(42, 111, 58, 160, 4, 4, false,
                    "Legs", "Quadriceps", "Adductors"),
            // left calf
            new Z(24, 159, 36, 196, 3, 3, false, "Calves"),
            // right calf
            new Z(44, 159, 56, 196, 3, 3, false, "Calves"),
    };

    private static final Z[] BACK = {
            // head
            new Z(28, 0, 52, 26, 12, 13, true),
            // traps
            new Z(34, 25, 46, 36, 2, 2, false, "Traps"),
            // left rear delt
            new Z(6, 35, 22, 50, 4, 4, false, "Rear delts"),
            // right rear delt
            new Z(58, 35, 74, 50, 4, 4, false, "Rear delts"),
            // upper back
            new Z(22, 35, 58, 52, 3, 3, false,
                    "Back", "Upper back", "Rhomboids"),
            // lats / middle back
            new Z(22, 51, 58, 66, 3, 3, false, "Lats", "Middle back"),
            // left tricep
            new Z(2, 49, 19, 84, 4, 4, false, "Triceps"),
            // right tricep
            new Z(61, 49, 78, 84, 4, 4, false, "Triceps"),
            // left forearm
            new Z(-1, 83, 16, 118, 3, 3, false, "Forearms"),
            // right forearm
            new Z(64, 83, 81, 118, 3, 3, false, "Forearms"),
            // lower back
            new Z(24, 65, 56, 96, 3, 3, false, "Lower back"),
            // glutes
            new Z(22, 95, 58, 112, 4, 4, false, "Glutes"),
            // left hamstring
            new Z(22, 111, 38, 160, 4, 4, false,
                    "Hamstrings", "Abductors"),
            // right hamstring
            new Z(42, 111, 58, 160, 4, 4, false,
                    "Hamstrings", "Abductors"),
            // left calf
            new Z(24, 159, 36, 196, 3, 3, false, "Calves"),
            // right calf
            new Z(44, 159, 56, 196, 3, 3, false, "Calves"),
    };

    /* ── paints ────────────────────────────────────────────────────────── */
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tmp = new RectF();

    /* ── data ──────────────────────────────────────────────────────────── */
    private final Map<String, Float> intensities = new HashMap<>();

    /* ── strings ───────────────────────────────────────────────────────── */
    private String txtFront = "FRONT";
    private String txtBack = "BACK";
    private String txtLow = "Low";
    private String txtHigh = "High";

    /* ── constructors ──────────────────────────────────────────────────── */

    public MuscleHeatmapView(Context context) {
        super(context);
        init();
    }

    public MuscleHeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MuscleHeatmapView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(0.6f);

        labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        try {
            txtFront = getContext().getString(R.string.heatmap_front);
            txtBack  = getContext().getString(R.string.heatmap_back);
            txtLow   = getContext().getString(R.string.heatmap_low);
            txtHigh  = getContext().getString(R.string.heatmap_high);
        } catch (Exception ignored) { /* keep defaults */ }
    }

    /* ── public API ────────────────────────────────────────────────────── */

    /**
     * Feed volume data keyed by {@link com.example.gymevo.model.MuscleGroup#getLabel()}.
     * Internally normalises to 0–1.
     */
    public void setMuscleVolumes(@Nullable Map<String, Integer> volumes) {
        intensities.clear();
        if (volumes != null && !volumes.isEmpty()) {
            int max = 0;
            for (int v : volumes.values()) {
                if (v > max) max = v;
            }
            if (max > 0) {
                for (Map.Entry<String, Integer> e : volumes.entrySet()) {
                    intensities.put(e.getKey(), (float) e.getValue() / max);
                }
            }
        }
        invalidate();
    }

    /* ── measure ───────────────────────────────────────────────────────── */

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        int h = Math.round(w * CH / CW);
        if (MeasureSpec.getMode(hSpec) == MeasureSpec.AT_MOST) {
            h = Math.min(h, MeasureSpec.getSize(hSpec));
        }
        setMeasuredDimension(w, h);
    }

    /* ── draw ──────────────────────────────────────────────────────────── */

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float scale = Math.min(getWidth() / CW, getHeight() / CH);
        float offX = (getWidth() - CW * scale) / 2f;
        float offY = (getHeight() - CH * scale) / 2f;

        canvas.save();
        canvas.translate(offX, offY);
        canvas.scale(scale, scale);

        int onSurface = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
        stroke.setColor(alphaColor(onSurface, 60));

        // section labels
        labelPaint.setColor(onSurface);
        labelPaint.setTextSize(10f);
        canvas.drawText(txtFront, FRONT_OX + 40f, 16f, labelPaint);
        canvas.drawText(txtBack, BACK_OX + 40f, 16f, labelPaint);

        // bodies
        int inactive = inactiveColor();
        drawBody(canvas, FRONT_OX, BODY_OY, FRONT, inactive);
        drawBody(canvas, BACK_OX, BODY_OY, BACK, inactive);

        // legend bar
        drawLegend(canvas, onSurface);

        canvas.restore();
    }

    private void drawBody(Canvas canvas, float ox, float oy, Z[] zones, int inactive) {
        for (Z z : zones) {
            boolean noMuscle = z.muscles == null || z.muscles.length == 0;
            float intensity = noMuscle ? 0f : zoneIntensity(z.muscles);
            int color = (noMuscle || intensity <= 0f) ? inactive : heatColor(intensity);

            fill.setColor(color);
            fill.setStyle(Paint.Style.FILL);
            tmp.set(ox + z.l, oy + z.t, ox + z.r, oy + z.b);

            if (z.oval) {
                canvas.drawOval(tmp, fill);
                canvas.drawOval(tmp, stroke);
            } else {
                canvas.drawRoundRect(tmp, z.rx, z.ry, fill);
                canvas.drawRoundRect(tmp, z.rx, z.ry, stroke);
            }
        }
    }

    private void drawLegend(Canvas canvas, int textColor) {
        float lx = 50f, rx = 170f, y = 234f, h = 10f;
        tmp.set(lx, y, rx, y + h);

        Paint gp = new Paint(Paint.ANTI_ALIAS_FLAG);
        gp.setShader(new LinearGradient(lx, y, rx, y,
                HEAT, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(tmp, 4f, 4f, gp);
        canvas.drawRoundRect(tmp, 4f, 4f, stroke);

        labelPaint.setColor(textColor);
        labelPaint.setTextSize(7f);

        labelPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(txtLow, lx - 3f, y + h - 1f, labelPaint);

        labelPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(txtHigh, rx + 3f, y + h - 1f, labelPaint);

        // reset alignment
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    /* ── helpers ────────────────────────────────────────────────────────── */

    private float zoneIntensity(@Nullable String[] muscles) {
        if (muscles == null || muscles.length == 0) return 0f;
        float max = 0f;
        for (String m : muscles) {
            Float v = intensities.get(m);
            if (v != null && v > max) max = v;
        }
        return max;
    }

    private int inactiveColor() {
        return MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorSurfaceVariant,
                COLOR_INACTIVE_FALLBACK);
    }

    private static int heatColor(float t) {
        t = Math.max(0.01f, Math.min(1f, t));
        float idx = t * (HEAT.length - 1);
        int lo = Math.min((int) idx, HEAT.length - 2);
        float frac = idx - lo;
        return blendRgb(HEAT[lo], HEAT[lo + 1], frac);
    }

    private static int blendRgb(int c1, int c2, float ratio) {
        float inv = 1f - ratio;
        int r = Math.round(Color.red(c1) * inv + Color.red(c2) * ratio);
        int g = Math.round(Color.green(c1) * inv + Color.green(c2) * ratio);
        int b = Math.round(Color.blue(c1) * inv + Color.blue(c2) * ratio);
        return Color.rgb(r, g, b);
    }

    private static int alphaColor(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
