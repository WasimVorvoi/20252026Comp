package com.frozenanimlib;

/**
 * BounceAnimation - produces a vertical bounce offset using |sin(t)|.
 *
 * HOW IT WORKS:
 *   Regular sin() goes -1 → 0 → +1 → 0 → -1, so half the wave is negative.
 *   By taking the absolute value |sin(t)| we get a wave that only goes 0 → 1 → 0 → 1 ...
 *   This mimics a ball bouncing: it rises to a peak and falls back to 0 repeatedly.
 *
 *   offset = -maxHeight * |sin(2π * time / period)|
 *   (Negative because screen Y-axis is inverted: 0 = top, positive = downward)
 *
 * USED IN GAME FOR:
 *   - Player spawn "bounce in" effect
 *   - Level-complete coin or star bouncing
 *   - Menu item hover bounce
 */
public class BounceAnimation implements Animatable {

    private final float maxHeight;  // peak bounce height in pixels (positive = upward visually)
    private final float period;     // seconds per bounce cycle
    private final int   bounces;    // how many bounces before stopping (-1 = loop forever)
    private final boolean looping;

    private float time;
    private int   bounceCount;
    private boolean running;

    /**
     * @param maxHeight  maximum bounce height in pixels (e.g. 20f)
     * @param period     seconds per full bounce (e.g. 0.5f)
     * @param bounces    number of bounces, or -1 for infinite loop
     */
    public BounceAnimation(float maxHeight, float period, int bounces) {
        this.maxHeight   = maxHeight;
        this.period      = period;
        this.bounces     = bounces;
        this.looping     = (bounces < 0);
        this.time        = 0f;
        this.bounceCount = 0;
        this.running     = true;
    }

    @Override
    public void update(float deltaSeconds) {
        if (!running) return;

        time += deltaSeconds;

        // Count how many half-periods have passed (each half = one "bounce")
        if (!looping) {
            int halfCycles = (int)(time / (period / 2f));
            bounceCount = halfCycles;
            if (bounceCount >= bounces * 2) {
                running = false;
                time    = bounces * period; // clamp
            }
        }
    }

    /**
     * Returns a Y offset (negative = upward) representing the current bounce height.
     * Caller adds this to the object's normal Y position before drawing.
     */
    public float getOffsetY() {
        if (!running && bounceCount >= bounces * 2) return 0f;
        float angle = (float)(2.0 * Math.PI * time / period);
        // |sin| gives 0→1→0→1 wave; negate so visually it goes UP on screen
        return -maxHeight * Math.abs((float) Math.sin(angle));
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isLooping() { return looping; }

    @Override
    public void reset() {
        time        = 0f;
        bounceCount = 0;
        running     = true;
    }
}
