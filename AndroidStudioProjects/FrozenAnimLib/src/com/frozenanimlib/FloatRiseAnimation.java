package com.frozenanimlib;

/**
 * FloatRiseAnimation - moves an object upward while fading it out.
 *
 * HOW IT WORKS:
 *   Two values are computed simultaneously over [0, duration]:
 *
 *   1. offsetY  = -riseHeight * (time / duration)
 *      Starts at 0, ends at -riseHeight (negative = upward on screen).
 *      Linear rise so the motion feels constant and readable.
 *
 *   2. alpha = 1 - (time / duration)
 *      Starts fully opaque (1.0), fades to transparent (0.0) as it rises.
 *
 * USED IN GAME FOR:
 *   - Damage numbers floating up after a hit
 *   - Enemy defeat score popup
 *   - Shard collection effect
 *   - Boss phase transition text rising off the screen
 *   - "FRACTURE!" label rising from a crack
 *
 * CALLER RESPONSIBILITY:
 *   The caller reads getOffsetY() and adds it to the object's screen Y.
 *   The caller reads getAlpha() and sets Paint.setAlpha(getAlphaInt()).
 *   Drawing the actual text/sprite is NOT this class's job.
 */
public class FloatRiseAnimation implements Animatable {

    private final float riseHeight;   // total pixels to rise over the duration (e.g. 80f)
    private final float duration;     // total animation time in seconds (e.g. 1.2f)
    private final boolean looping;    // almost always false for this type

    private float time;
    private boolean running;

    /**
     * @param riseHeight  how many pixels upward the object moves (e.g. 80f)
     * @param duration    how long the rise+fade takes in seconds (e.g. 1.0f)
     * @param looping     true = repeats (unusual — useful for ambient particle effects)
     */
    public FloatRiseAnimation(float riseHeight, float duration, boolean looping) {
        this.riseHeight = riseHeight;
        this.duration   = duration;
        this.looping    = looping;
        reset();
    }

    /** Convenience constructor: non-looping, standard 1-second rise of 80 pixels. */
    public static FloatRiseAnimation standard() {
        return new FloatRiseAnimation(80f, 1.0f, false);
    }

    /** Convenience: slower, taller rise for boss-defeat effects. */
    public static FloatRiseAnimation dramatic() {
        return new FloatRiseAnimation(160f, 1.8f, false);
    }

    @Override
    public void update(float deltaSeconds) {
        if (!running) return;

        time += deltaSeconds;

        if (time >= duration) {
            if (looping) {
                time = 0f;
            } else {
                time    = duration;
                running = false;
            }
        }
    }

    /**
     * Returns the Y offset in pixels (negative = upward).
     * Caller: drawY = originalY + getOffsetY()
     */
    public float getOffsetY() {
        if (duration <= 0f) return -riseHeight;
        float progress = Math.min(time / duration, 1f);
        return -riseHeight * progress;
    }

    /**
     * Returns the current alpha in [0.0, 1.0].
     * Starts at 1.0 (opaque), ends at 0.0 (invisible).
     */
    public float getAlpha() {
        if (duration <= 0f) return 0f;
        float progress = Math.min(time / duration, 1f);
        return 1f - progress;
    }

    /**
     * Returns alpha as an integer [0, 255] for Android Paint.setAlpha().
     * Example: paint.setAlpha(anim.getAlphaInt());
     */
    public int getAlphaInt() {
        return (int)(getAlpha() * 255f);
    }

    /** True while the animation is still playing. */
    @Override public boolean isRunning() { return running; }

    /** True if this animation loops. */
    @Override public boolean isLooping() { return looping; }

    /** Reset to the beginning so this object can be re-used (e.g., object pool). */
    @Override
    public void reset() {
        time    = 0f;
        running = true;
    }
}
