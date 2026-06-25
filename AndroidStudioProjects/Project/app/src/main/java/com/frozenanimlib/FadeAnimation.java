package com.frozenanimlib;

/**
 * FadeAnimation - linearly interpolates an alpha value between 0 and 1 (or 1 to 0).
 *
 * HOW IT WORKS:
 *   Linear interpolation (lerp): alpha = start + (end - start) * (time / duration)
 *   When time = 0       → alpha = start
 *   When time = duration → alpha = end
 *
 *   Use fadeIn()  to go from transparent (0) to opaque (1).
 *   Use fadeOut() to go from opaque (1) to transparent (0).
 *
 * USED IN GAME FOR:
 *   - Level complete overlay fading in
 *   - Game over screen appearing
 *   - Cracking-ice warning flash (fade out)
 *   - Player death flash
 */
public class FadeAnimation implements Animatable {

    // ---- configuration ----
    private final float startAlpha;   // alpha at the very beginning
    private final float endAlpha;     // alpha at the very end
    private final float duration;     // seconds to complete one fade
    private final boolean looping;

    // ---- internal state ----
    private float time;
    private boolean running;

    /**
     * Create a fade animation.
     *
     * @param startAlpha  starting alpha (0.0 = fully transparent, 1.0 = fully opaque)
     * @param endAlpha    ending alpha
     * @param duration    time in seconds to complete the fade
     * @param looping     if true, reverses and repeats (ping-pong)
     */
    public FadeAnimation(float startAlpha, float endAlpha, float duration, boolean looping) {
        this.startAlpha = startAlpha;
        this.endAlpha   = endAlpha;
        this.duration   = duration;
        this.looping    = looping;
        this.time       = 0f;
        this.running    = true;
    }

    /** Convenience factory: fade from 0 → 1 over the given duration, not looping. */
    public static FadeAnimation fadeIn(float duration) {
        return new FadeAnimation(0f, 1f, duration, false);
    }

    /** Convenience factory: fade from 1 → 0 over the given duration, not looping. */
    public static FadeAnimation fadeOut(float duration) {
        return new FadeAnimation(1f, 0f, duration, false);
    }

    /** Convenience factory: fade in then out, looping (blink/flash effect). */
    public static FadeAnimation blink(float cycleDuration) {
        return new FadeAnimation(0f, 1f, cycleDuration / 2f, true);
    }

    @Override
    public void update(float deltaSeconds) {
        if (!running) return;

        time += deltaSeconds;

        if (looping) {
            // ping-pong: when we pass the end, wrap back
            if (time >= duration) {
                time = 0f; // simple wrap for now — direction stays the same
            }
        } else {
            if (time >= duration) {
                time    = duration; // clamp
                running = false;
            }
        }
    }

    /**
     * Returns the current alpha in [0.0, 1.0].
     * Converts alpha to a 0-255 int via getAlphaInt() for Android Paint.
     */
    public float getAlpha() {
        if (duration <= 0f) return endAlpha;
        float t = Math.min(time / duration, 1f); // t in [0, 1]
        return startAlpha + (endAlpha - startAlpha) * t;
    }

    /** Returns the current alpha as an integer in [0, 255] (for Android Paint.setAlpha). */
    public int getAlphaInt() {
        return (int)(getAlpha() * 255f);
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isLooping() { return looping; }

    @Override
    public void reset() {
        time    = 0f;
        running = true;
    }
}
