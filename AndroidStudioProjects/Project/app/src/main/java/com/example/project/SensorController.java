package com.example.project;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * SensorController - reads the phone's accelerometer and produces smooth tilt values.
 *
 * HOW THE ACCELEROMETER WORKS:
 *   The accelerometer measures the force of gravity on three axes (x, y, z).
 *   When the phone lies flat face-up: z ≈ +9.8, x ≈ 0, y ≈ 0
 *   When the phone is held upright (portrait): y ≈ -9.8, x ≈ 0, z ≈ 0
 *
 *   For our game (phone held in portrait):
 *     sensorX: tilt left (left edge down)  → positive → player goes RIGHT
 *     sensorY: the gravity baseline is -9.8 when upright.
 *              tilt "forward" (top toward you) → y increases → player goes DOWN
 *              tilt "backward" (top away from you) → y decreases → player goes UP
 *
 * DEAD ZONE:
 *   Small tilts near zero are ignored (DEAD_ZONE constant).
 *   This prevents the player from drifting when the phone is held still.
 *
 * LOW-PASS FILTER:
 *   Raw sensor values are noisy.  We smooth them using an exponential moving average:
 *       smooth = smooth + ALPHA * (raw - smooth)
 *   A small ALPHA (e.g. 0.15) = very smooth but sluggish.
 *   A larger ALPHA (e.g. 0.4)  = more responsive but noisier.
 *   We use 0.2 for a good balance.
 */
public class SensorController implements SensorEventListener {

    // Smoothing factor for low-pass filter (0.0 to 1.0)
    // Lower = smoother but less responsive; higher = snappier but jittery
    private static final float ALPHA     = 0.20f;

    // Dead zone: ignore tilts smaller than this (prevents idle drift)
    private static final float DEAD_ZONE = 0.4f;

    // Gravity baseline on Y axis when phone is held upright in portrait mode
    private static final float GRAVITY_Y_BASELINE = -9.8f;

    // The sensor manager (Android service that gives us sensor access)
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    // Smoothed output values (read by the game each frame)
    private float smoothX = 0f;
    private float smoothY = 0f;

    // Raw sensor values (updated by onSensorChanged on the sensor thread)
    private volatile float rawX = 0f;
    private volatile float rawY = 0f;

    // True if the sensor is currently registered and receiving events
    private boolean registered = false;

    /**
     * Create the controller. Pass the Activity context.
     * Does NOT start listening yet — call register() when the game starts.
     */
    public SensorController(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * Start listening for sensor updates.
     * Call from GameView.surfaceCreated() or Activity.onResume().
     *
     * SENSOR_DELAY_GAME: updates at ~50 Hz — good for games without wasting battery.
     */
    public void register() {
        if (!registered && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
            registered = true;
        }
    }

    /**
     * Stop listening for sensor updates.
     * Call from GameView.surfaceDestroyed() or Activity.onPause().
     * IMPORTANT: always unregister when the game is not visible to save battery.
     */
    public void unregister() {
        if (registered) {
            sensorManager.unregisterListener(this);
            registered = false;
        }
    }

    /**
     * Called by Android whenever the accelerometer has new data.
     * This runs on a background sensor thread — we just store the raw values.
     * The game thread reads them via getTiltX() / getTiltY().
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            rawX = event.values[0];  // lateral tilt
            rawY = event.values[1];  // forward/back tilt
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this game
    }

    /**
     * Update the low-pass filter — call once per game frame from GameView.update().
     * Reads the volatile rawX/rawY (set by sensor thread) and smooths them.
     */
    public void update() {
        // Smooth the raw values using the exponential moving average formula
        float filteredX = rawX;
        // For Y: subtract gravity baseline so 0 = "neutral upright" position
        float filteredY = rawY - GRAVITY_Y_BASELINE;

        // Apply low-pass filter
        smoothX = smoothX + ALPHA * (filteredX - smoothX);
        smoothY = smoothY + ALPHA * (filteredY - smoothY);
    }

    /**
     * Returns the smoothed X tilt value after applying the dead zone.
     * Positive = phone tilted right (right edge down) → player moves right.
     * Negative = phone tilted left.
     */
    public float getTiltX() {
        return Math.abs(smoothX) < DEAD_ZONE ? 0f : smoothX;
    }

    /**
     * Returns the smoothed Y tilt value after applying the dead zone.
     * Positive = phone tilted "forward" (bottom toward player) → player moves DOWN on screen.
     * Negative = phone tilted "backward" → player moves UP.
     */
    public float getTiltY() {
        // Negate smoothY so that tilting the top forward moves the player DOWN
        // (positive screen Y = downward, but "top toward you" increases rawY)
        float tilt = -smoothY;
        return Math.abs(tilt) < DEAD_ZONE ? 0f : tilt;
    }

    /** Returns true if the accelerometer hardware is available on this device. */
    public boolean isAvailable() {
        return accelerometer != null;
    }
}
