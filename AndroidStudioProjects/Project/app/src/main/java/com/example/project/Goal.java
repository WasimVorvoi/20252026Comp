package com.example.project;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.frozenanimlib.AnimationManager;
import com.frozenanimlib.PulseAnimation;

/**
 * Goal - the exit portal the player must reach to complete a level.
 *
 * RENDERING:
 *   The goal is drawn as a glowing teal/cyan portal using nested circles.
 *   A PulseAnimation from our animation library makes it breathe in and out,
 *   making it easy to spot on screen.
 *
 * The PulseAnimation is registered in AnimationManager under the key "goal_pulse".
 * GameView registers it once when the level is loaded.
 */
public class Goal {

    // Centre position in pixels (set from the tile grid position)
    public final float cx, cy;
    // Base radius of the goal portal (sized to fit within one tile)
    public final float radius;

    // Paint objects (created once, reused every frame)
    private final Paint outerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint midGlowPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint symbolPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * @param cx     pixel X of the goal centre
     * @param cy     pixel Y of the goal centre
     * @param radius radius of the goal portal (usually half a tile's size)
     */
    public Goal(float cx, float cy, float radius) {
        this.cx     = cx;
        this.cy     = cy;
        this.radius = radius;
    }

    /**
     * Register this goal's pulse animation into the AnimationManager.
     * Call this once when the level loads (from LevelManager or GameView).
     *
     * Parameters: pulses between 0.85 and 1.15 scale, once per 1.4 seconds, looping.
     */
    public void registerAnimations(AnimationManager animMgr) {
        animMgr.registerPulse("goal_pulse", 0.85f, 1.15f, 1.4f, true);
    }

    /**
     * Draw the goal portal.
     *
     * @param canvas   the Canvas to draw on
     * @param animMgr  used to read the current pulse scale
     */
    public void draw(Canvas canvas, AnimationManager animMgr) {
        // Read pulse scale — defaults to 1.0 if animation not found
        float scale = 1f;
        PulseAnimation pulse = animMgr.getPulse("goal_pulse");
        if (pulse != null) {
            scale = pulse.getScale();
        }
        float r = radius * scale;

        // Outermost glow (large, very transparent)
        outerGlowPaint.setStyle(Paint.Style.FILL);
        outerGlowPaint.setColor(Color.argb(30, 0, 230, 210));
        canvas.drawCircle(cx, cy, r * 2.2f, outerGlowPaint);

        // Second glow ring
        midGlowPaint.setStyle(Paint.Style.FILL);
        midGlowPaint.setColor(Color.argb(60, 0, 200, 185));
        canvas.drawCircle(cx, cy, r * 1.6f, midGlowPaint);

        // Main portal disc
        corePaint.setStyle(Paint.Style.FILL);
        corePaint.setColor(Color.rgb(0, 200, 185));
        canvas.drawCircle(cx, cy, r, corePaint);

        // Inner brighter centre
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(Color.rgb(180, 255, 245));
        canvas.drawCircle(cx, cy, r * 0.45f, innerPaint);

        // Snowflake-style cross symbol in the centre
        symbolPaint.setStyle(Paint.Style.STROKE);
        symbolPaint.setStrokeWidth(r * 0.08f);
        symbolPaint.setColor(Color.argb(180, 0, 80, 75));
        float arm = r * 0.35f;
        canvas.drawLine(cx - arm, cy, cx + arm, cy, symbolPaint);
        canvas.drawLine(cx, cy - arm, cx, cy + arm, symbolPaint);
        // Diagonal arms at 45°
        float diag = arm * 0.7f;
        canvas.drawLine(cx - diag, cy - diag, cx + diag, cy + diag, symbolPaint);
        canvas.drawLine(cx + diag, cy - diag, cx - diag, cy + diag, symbolPaint);
    }

    /**
     * Returns true if the player (at px, py with given radius) has reached the goal.
     * Uses simple circle–circle distance check.
     */
    public boolean isReached(float px, float py, float playerRadius) {
        float dx = px - cx;
        float dy = py - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        // Player needs to overlap the inner goal area
        return dist < (radius * 0.8f + playerRadius * 0.5f);
    }
}
