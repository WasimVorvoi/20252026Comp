package com.frozenanimlib;

/**
 * PulseAnimation - smoothly oscillates a scale value between a minimum and maximum.
 *
 * HOW IT WORKS:
 *   We use a sine wave. The sine function produces values from -1 to +1 over time.
 *   We map that range to [minScale, maxScale] using the formula:
 *       scale = midpoint + amplitude * sin(2π * time / period)
 *
 *   where:
 *       midpoint  = (maxScale + minScale) / 2
 *       amplitude = (maxScale - minScale) / 2
 *
 * USED IN GAME FOR:
 *   - Goal portal glow (pulses between 0.9 and 1.1 scale)
 *   - Player spawn flash (quick pulse before gameplay starts)
 *   - Menu title text size pulsing
 */
public class PulseAnimation implements Animatable {

    // ---- configuration ----
    private final float minScale;   // smallest scale value (e.g. 0.85)
    private final float maxScale;   // largest  scale value (e.g. 1.15)
    private final float period;     // seconds for one full pulse cycle (e.g. 1.2)
    private final boolean looping;  // true = pulse forever, false = one shot

    // ---- internal state ----
    private float time;             // how many seconds have elapsed
    private boolean running;

    /**
     * Create a pulse animation.
     *
     * @param minScale  smallest scale the object reaches (e.g. 0.85f)
     * @param maxScale  largest  scale the object reaches (e.g. 1.15f)
     * @param period    duration of one full cycle in seconds (e.g. 1.0f)
     * @param looping   true = loops forever, false = stops after one cycle
     */
    public PulseAnimation(float minScale, float maxScale, float period, boolean looping) {
        this.minScale  = minScale;
        this.maxScale  = maxScale;
        this.period    = period;
        this.looping   = looping;
        this.time      = 0f;
        this.running   = true;
    }

    /** Advance time by deltaSeconds, wrapping around when looping. */
    @Override
    public void update(float deltaSeconds) {
        if (!running) return;

        time += deltaSeconds;

        if (!looping && time >= period) {
            time    = period;   // clamp at end of first cycle
            running = false;
        }
        // when looping, we let time grow; getScale() uses modulo via sin period
    }

    /**
     * Returns the current scale value in [minScale, maxScale].
     *
     * Formula breakdown:
     *   sin produces -1 to +1
     *   We multiply by amplitude to get -amplitude to +amplitude
     *   Then add midpoint to shift the range up to [min, max]
     */
    public float getScale() {
        float midpoint  = (maxScale + minScale) / 2f;
        float amplitude = (maxScale - minScale) / 2f;
        // 2π / period converts time to radians so one cycle = period seconds
        float angle = (float)(2.0 * Math.PI * time / period);
        return midpoint + amplitude * (float) Math.sin(angle);
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isLooping() { return looping; }

    @Override
    public void reset() {
        time    = 0f;
        running = true;
    }
}
