package com.example.project;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * HUD (Heads-Up Display) - draws the in-game overlay showing level info and controls.
 *
 * What the HUD displays:
 *   - Current level number and name (top-left)
 *   - A pause button (top-right corner)
 *   - A level hint text briefly at level start
 *
 * DESIGN NOTE:
 *   The HUD is drawn AFTER the level and player so it appears on top.
 *   All HUD elements use semi-transparent backgrounds so the game is visible behind them.
 */
public class HUD {

    // Semi-transparent panel background colour (dark navy, 70% opaque)
    private static final int COL_PANEL = Color.argb(178, 5, 14, 40);
    // Text colours
    private static final int COL_TEXT_WHITE = Color.rgb(220, 240, 255);
    private static final int COL_TEXT_CYAN  = Color.rgb(0, 210, 195);
    private static final int COL_PAUSE_BG   = Color.argb(180, 30, 80, 140);

    // Paint objects reused across frames
    private final Paint panelPaint  = new Paint();
    private final Paint textPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pausePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pauseBar    = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Pause button rectangle — stored so MainActivity can detect taps on it
    private final RectF pauseButton = new RectF();

    // Hint display timer — shows the hint for 3 seconds at level start
    private float hintTimer = 0f;
    private static final float HINT_DURATION = 3.0f;

    public HUD() {
        panelPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(COL_TEXT_WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        smallPaint.setColor(COL_TEXT_CYAN);

        hintPaint.setColor(Color.rgb(255, 220, 100));
        hintPaint.setTypeface(Typeface.DEFAULT_BOLD);
        hintPaint.setTextAlign(Paint.Align.CENTER);

        pausePaint.setStyle(Paint.Style.FILL);
        pausePaint.setColor(COL_PAUSE_BG);

        pauseBar.setStyle(Paint.Style.FILL);
        pauseBar.setColor(COL_TEXT_WHITE);
    }

    /**
     * Draw the HUD on top of the game scene.
     *
     * @param canvas       the Canvas to draw on
     * @param levelNum     current level number (1-based)
     * @param levelName    name of the current level
     * @param levelHint    hint text shown at level start
     * @param screenW      screen width in pixels
     * @param screenH      screen height in pixels
     * @param dt           delta time in seconds (to advance the hint timer)
     */
    public void draw(Canvas canvas, int levelNum, String levelName, String levelHint,
                     int screenW, int screenH, float dt) {

        float density = screenW / 360f; // approximate density for sizing

        float textSize      = 16f * density;
        float smallTextSize = 11f * density;
        float padding       = 8f * density;
        float panelH        = textSize + smallTextSize + padding * 2f;

        // ---- Top-left level panel ----
        panelPaint.setColor(COL_PANEL);
        float panelW = 180f * density;
        canvas.drawRect(0, 0, panelW, panelH, panelPaint);

        textPaint.setTextSize(textSize);
        textPaint.setColor(COL_TEXT_WHITE);
        canvas.drawText("LEVEL " + levelNum, padding, padding + textSize, textPaint);

        smallPaint.setTextSize(smallTextSize);
        canvas.drawText(levelName, padding, padding + textSize + smallTextSize, smallPaint);

        // ---- Pause button (top-right) ----
        float btnSize = 40f * density;
        float btnX    = screenW - btnSize - padding;
        float btnY    = padding;
        pauseButton.set(btnX, btnY, btnX + btnSize, btnY + btnSize);

        pausePaint.setColor(COL_PAUSE_BG);
        canvas.drawRoundRect(pauseButton, 6f, 6f, pausePaint);

        // Draw two vertical bars as the pause icon
        pauseBar.setColor(COL_TEXT_WHITE);
        float barW   = btnSize * 0.15f;
        float barH   = btnSize * 0.5f;
        float barY   = btnY + (btnSize - barH) / 2f;
        float bar1X  = btnX + btnSize * 0.28f;
        float bar2X  = btnX + btnSize * 0.57f;
        canvas.drawRect(bar1X, barY, bar1X + barW, barY + barH, pauseBar);
        canvas.drawRect(bar2X, barY, bar2X + barW, barY + barH, pauseBar);

        // ---- Hint text (shows for HINT_DURATION seconds, then fades) ----
        if (hintTimer < HINT_DURATION && levelHint != null && !levelHint.isEmpty()) {
            hintTimer += dt;

            // Fade out in the last second
            float alpha = 1f;
            if (hintTimer > HINT_DURATION - 1f) {
                alpha = (HINT_DURATION - hintTimer); // 0.0 to 1.0
            }
            int hintAlpha = (int)(alpha * 230);

            float hintTextSize = 14f * density;
            hintPaint.setTextSize(hintTextSize);
            hintPaint.setColor(Color.argb(hintAlpha, 255, 220, 100));
            hintPaint.setTextAlign(Paint.Align.CENTER);

            float hintY = panelH + hintTextSize * 2f;
            // Background pill for readability
            float hintW   = hintPaint.measureText(levelHint) + padding * 2f;
            float hintBoxY = hintY - hintTextSize - padding;
            RectF hintBox  = new RectF(screenW / 2f - hintW / 2f, hintBoxY,
                                        screenW / 2f + hintW / 2f, hintBoxY + hintTextSize + padding * 2f);
            panelPaint.setColor(Color.argb(hintAlpha / 2, 5, 14, 40));
            canvas.drawRoundRect(hintBox, 8f, 8f, panelPaint);

            canvas.drawText(levelHint, screenW / 2f, hintY, hintPaint);
        }
    }

    /** Returns the pause button bounding rectangle for touch detection. */
    public RectF getPauseButtonBounds() {
        return pauseButton;
    }

    /** Reset the hint timer (call when a new level loads). */
    public void resetHint() {
        hintTimer = 0f;
    }
}
