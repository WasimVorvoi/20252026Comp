package com.example.voidfall.input;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * TiltInput — wraps Android accelerometer sensor data into normalized [-1, +1] values.
 *
 * WHY ACCELEROMETER:
 *   The accelerometer measures forces in m/s² along 3 axes.
 *   At rest on a flat surface: values[0]=0, values[1]=0, values[2]≈9.8 (gravity).
 *   Tilting the phone shifts gravity's contribution between axes.
 *   We only use X (left/right) and Y (forward/back) axes.
 *
 * CALIBRATION:
 *   The player holds the phone at whatever angle is comfortable.
 *   We average the first 30 readings to find the "resting" baseline.
 *   All subsequent readings subtract this baseline.
 *   This means the game responds to CHANGES in tilt, not absolute angle.
 *
 * VOLATILE KEYWORD:
 *   tiltX and tiltY are written by the sensor callback (main thread)
 *   and read by the game loop (background thread).
 *   volatile guarantees that each thread sees the most recently written value.
 *   Without volatile, the JVM might cache the value in a CPU register and
 *   the game thread could read stale data.
 *
 * DEAD ZONE:
 *   Small tilt values below DEAD_ZONE are treated as zero.
 *   This prevents the player from drifting when the phone is held still.
 */
public class TiltInput implements SensorEventListener {

    // Sensor input clamped to this range (m/s²) before normalising to [-1, +1].
    // A comfortable gaming tilt reaches about 4-5 m/s² on the relevant axis.
    private static final float MAX_TILT = 4.5f;

    // Values below this magnitude (after baseline subtraction) are treated as 0.
    private static final float DEAD_ZONE = 0.3f;

    // How many sensor readings to average for the calibration baseline.
    private static final int CALIBRATION_SAMPLES = 30;

    // Calibration state
    private final float[] baselineSum = new float[3];
    private final float[] baseline    = new float[3];
    private int  calibrationCount = 0;
    private boolean calibrated    = false;

    // Normalized tilt values consumed by the game thread.
    // volatile: written by sensor thread, read by game thread.
    private volatile float tiltX = 0f;   // left(-1) to right(+1)
    private volatile float tiltY = 0f;   // forward(+1) to backward(-1)

    // Mirror mode: when active (Mirror Beast boss), tilt axes are inverted.
    private volatile boolean mirrorX = false;
    private volatile boolean mirrorY = false;

    private final SensorManager sensorManager;
    private Sensor accelerometer;

    public TiltInput(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    /** Call from Activity.onResume() to start listening for tilt. */
    public void register() {
        if (accelerometer != null) {
            // SENSOR_DELAY_GAME provides ~50Hz update rate — enough for smooth gameplay.
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /** Call from Activity.onPause() to stop sensor updates and save battery. */
    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    /** Re-calibrate: next 30 readings will reset the baseline. */
    public void recalibrate() {
        calibrated         = false;
        calibrationCount   = 0;
        baselineSum[0]     = 0f;
        baselineSum[1]     = 0f;
        baselineSum[2]     = 0f;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float rawX = event.values[0];
        float rawY = event.values[1];

        // --- Calibration phase: accumulate readings ---
        if (!calibrated) {
            baselineSum[0] += rawX;
            baselineSum[1] += rawY;
            calibrationCount++;
            if (calibrationCount >= CALIBRATION_SAMPLES) {
                baseline[0] = baselineSum[0] / CALIBRATION_SAMPLES;
                baseline[1] = baselineSum[1] / CALIBRATION_SAMPLES;
                calibrated  = true;
            }
            return; // don't produce tilt values until calibrated
        }

        // --- Normal operation: subtract baseline ---
        float adjustedX = rawX - baseline[0];
        float adjustedY = rawY - baseline[1];

        // Apply dead zone: small noise becomes exactly 0
        if (Math.abs(adjustedX) < DEAD_ZONE) adjustedX = 0f;
        if (Math.abs(adjustedY) < DEAD_ZONE) adjustedY = 0f;

        // Clamp to [-MAX_TILT, +MAX_TILT] and normalise to [-1, +1]
        float normX = Math.max(-1f, Math.min(1f, adjustedX / MAX_TILT));
        float normY = Math.max(-1f, Math.min(1f, adjustedY / MAX_TILT));

        // Negate X: physically tilting right produces a negative accelerometer X value,
        // but we want tiltX > 0 to mean "right". Negating fixes this.
        normX = -normX;

        // Mirror mode: invert both axes (used by Mirror Beast boss zones).
        if (mirrorX) normX = -normX;
        if (mirrorY) normY = -normY;

        tiltX = normX;
        tiltY = normY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used — accuracy changes don't affect our simple tilt mapping.
    }

    // --- Accessors (called from game thread) ---

    /** Normalised left/right tilt in [-1, +1]. Negative = left, positive = right. */
    public float getTiltX() { return tiltX; }

    /** Normalised forward/back tilt in [-1, +1]. Positive = forward, negative = back. */
    public float getTiltY() { return tiltY; }

    /** True once the calibration baseline has been established. */
    public boolean isCalibrated() { return calibrated; }

    /** Enable or disable mirror mode on the X axis (Mirror Beast mechanic). */
    public void setMirrorX(boolean mirror) { this.mirrorX = mirror; }

    /** Enable or disable mirror mode on the Y axis. */
    public void setMirrorY(boolean mirror) { this.mirrorY = mirror; }
}
