package com.frozenanimlib;

import java.util.Random;

/**
 * ShakeAnimation - rapidly jitters an object horizontally (and optionally vertically).
 *
 * HOW IT WORKS:
 *   Every "tick" (shakeInterval seconds) we pick a new random X offset within [-magnitude, magnitude].
 *   The magnitude decays linearly over the total duration so the shake fades to nothing.
 *
 *   decayFactor = 1 - (time / duration)  → starts at 1, reaches 0 at end
 *   offsetX = randomInRange(-magnitude, +magnitude) * decayFactor
 *
 * USED IN GAME FOR:
 *   - Player collision/death reaction
 *   - Obstacle warning (shake before a hazard moves)
 *   - Screen shake when player falls into a pit
 */
public class ShakeAnimation implements Animatable {

    private final float magnitude;       // max pixel displacement at start (e.g. 12f)
    private final float duration;        // total shake time in seconds (e.g. 0.5f)
    private final float shakeInterval;   // seconds between new random offsets (e.g. 0.05f)
    private final boolean looping;       // shake animations are rarely looping

    private float time;
    private float tickTimer;   // counts up to shakeInterval, then resets
    private float currentX;    // the current random X offset
    private float currentY;    // the current random Y offset (for vertical shake)
    private boolean running;

    private final Random random = new Random();

    /**
     * @param magnitude      peak displacement in pixels (e.g. 10f)
     * @param duration       total shake duration in seconds (e.g. 0.4f)
     * @param shakeInterval  how often the offset updates (e.g. 0.05f)
     */
    public ShakeAnimation(float magnitude, float duration, float shakeInterval) {
        this.magnitude     = magnitude;
        this.duration      = duration;
        this.shakeInterval = shakeInterval;
        this.looping       = false;
        reset();
    }

    @Override
    public void update(float deltaSeconds) {
        if (!running) return;

        time      += deltaSeconds;
        tickTimer += deltaSeconds;

        if (time >= duration) {
            running  = false;
            currentX = 0f;
            currentY = 0f;
            return;
        }

        // Update offset on every tick
        if (tickTimer >= shakeInterval) {
            tickTimer = 0f;
            // Magnitude decays to zero as time approaches duration
            float decayFactor = 1f - (time / duration);
            float range = magnitude * decayFactor;
            currentX = (random.nextFloat() * 2f - 1f) * range; // -range to +range
            currentY = (random.nextFloat() * 2f - 1f) * range * 0.5f; // half vertical
        }
    }

    /** Returns current horizontal pixel offset. Add to object's draw X position. */
    public float getOffsetX() { return currentX; }

    /** Returns current vertical pixel offset. Add to object's draw Y position. */
    public float getOffsetY() { return currentY; }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isLooping() { return looping; }

    @Override
    public void reset() {
        time      = 0f;
        tickTimer = 0f;
        currentX  = 0f;
        currentY  = 0f;
        running   = true;
    }
}
