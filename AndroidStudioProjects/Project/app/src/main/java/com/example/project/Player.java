package com.example.project;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.frozenanimlib.AnimationManager;
import com.frozenanimlib.ShakeAnimation;

/**
 * Player - the snowball/ice orb that the player controls by tilting the phone.
 *
 * PHYSICS MODEL:
 *   The player has a position (x, y) in pixels and a velocity (vx, vy) in pixels/second.
 *   Each frame, tilt input adds to the velocity (like a force).
 *   Damping (ice friction) slowly reduces the velocity — this creates the slippery feel.
 *   Velocity is then clamped to a maximum speed so the ball doesn't fly off screen.
 *
 *   velocity update:
 *       vx += tiltX * ACCEL_STRENGTH * dt
 *       vy += tiltY * ACCEL_STRENGTH * dt
 *       vx *= pow(DAMPING, dt)          ← exponential decay, frame-rate independent
 *       vy *= pow(DAMPING, dt)
 *
 * COLLISION:
 *   Before moving, we check tiles in the next position.
 *   We test X and Y movement separately so the player slides along walls.
 *
 * RENDERING:
 *   The player is drawn as nested circles — a bright core and softer glow rings.
 *   On death a ShakeAnimation from the animation library creates a jitter effect.
 */
public class Player {

    // ---- physics constants ----
    // How strongly tilt input accelerates the ball (pixels/s² per unit of tilt)
    private static final float ACCEL_STRENGTH = 550f;
    // Damping factor: 0.0 = instant stop, 1.0 = no friction, ~0.85 = slippery ice
    private static final float DAMPING        = 0.82f;
    // Maximum speed in any direction (pixels/second)
    private static final float MAX_SPEED      = 600f;

    // ---- position and velocity ----
    public float x, y;           // centre of the player circle, in pixels
    public float vx, vy;         // velocity vector, pixels per second

    // ---- size (set in constructor based on tile size) ----
    public float radius;

    // ---- state ----
    public boolean alive;        // false after the player hits a hazard

    // ---- death shake animation ----
    private ShakeAnimation deathShake;

    // ---- paint objects (created once, reused every frame) ----
    private final Paint glowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Create a new player at pixel position (startX, startY).
     *
     * @param startX  pixel X of the player's starting centre
     * @param startY  pixel Y of the player's starting centre
     * @param radius  radius of the player circle in pixels
     */
    public Player(float startX, float startY, float radius) {
        this.x      = startX;
        this.y      = startY;
        this.radius = radius;
        this.vx     = 0f;
        this.vy     = 0f;
        this.alive  = true;
        // Death shake: 15px magnitude, 0.6s total, update every 0.04s
        this.deathShake = new ShakeAnimation(15f, 0.6f, 0.04f);
    }

    /**
     * Update the player's position for one game frame.
     *
     * @param dt       delta time in seconds (time since last frame)
     * @param tiltX    smoothed X-axis tilt from accelerometer (-1 to 1 range roughly)
     * @param tiltY    smoothed Y-axis tilt from accelerometer
     * @param level    the current Level, used for collision detection
     * @param animMgr  the AnimationManager (used to trigger death shake)
     */
    public void update(float dt, float tiltX, float tiltY, Level level, AnimationManager animMgr) {
        if (!alive) {
            // Still update shake animation even when dead so it plays out
            deathShake.update(dt);
            return;
        }

        // 1. Apply tilt acceleration to velocity
        vx += tiltX * ACCEL_STRENGTH * dt;
        vy += tiltY * ACCEL_STRENGTH * dt;

        // 2. Apply ice damping — uses Math.pow so damping is consistent at any frame rate
        //    (e.g. at 60fps dt≈0.016, DAMPING^0.016 ≈ 0.997 per frame)
        float dampFactor = (float) Math.pow(DAMPING, dt);
        vx *= dampFactor;
        vy *= dampFactor;

        // 3. Clamp speed so the ball never moves too fast
        float speed = (float) Math.sqrt(vx * vx + vy * vy);
        if (speed > MAX_SPEED) {
            float scale = MAX_SPEED / speed;
            vx *= scale;
            vy *= scale;
        }

        // 4. Attempt to move in X direction, then Y direction (separated for wall sliding)
        float newX = x + vx * dt;
        if (!level.collidesWithWalls(newX, y, radius)) {
            x = newX;
        } else {
            vx = 0f; // stop horizontal velocity on wall hit
        }

        float newY = y + vy * dt;
        if (!level.collidesWithWalls(x, newY, radius)) {
            y = newY;
        } else {
            vy = 0f; // stop vertical velocity on wall hit
        }

        // 5. Check if player is standing on a deadly tile
        if (level.isDeadlyAt(x, y)) {
            die(animMgr);
        }
    }

    /** Kill the player and start the death shake animation. */
    public void die(AnimationManager animMgr) {
        if (!alive) return;
        alive = false;
        deathShake.reset();
        // Register / re-register the shake in the manager so callers can query it
        animMgr.register("player_death_shake", deathShake);
    }

    /**
     * Draw the player on the given canvas.
     * Uses glow rings + bright core to look like a glowing ice orb.
     *
     * @param canvas   the Canvas to draw on
     * @param animMgr  used to read pulse scale for the spawn glow effect
     */
    public void draw(Canvas canvas, AnimationManager animMgr) {
        // If dead, apply shake offset from the death animation
        float drawX = x;
        float drawY = y;
        if (!alive) {
            drawX += deathShake.getOffsetX();
            drawY += deathShake.getOffsetY();
        }

        // Read pulse scale from animation manager (registered in GameView as "player_pulse")
        float scale = 1f;
        if (animMgr.getPulse("player_pulse") != null) {
            scale = animMgr.getPulse("player_pulse").getScale();
        }
        float r = radius * scale;

        // Outer glow (large, very transparent)
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(Color.argb(40, 150, 210, 255));
        canvas.drawCircle(drawX, drawY, r * 1.8f, glowPaint);

        // Mid glow
        glowPaint.setColor(Color.argb(70, 180, 225, 255));
        canvas.drawCircle(drawX, drawY, r * 1.35f, glowPaint);

        // Main body
        corePaint.setStyle(Paint.Style.FILL);
        if (alive) {
            corePaint.setColor(Color.rgb(200, 230, 255)); // icy white-blue
        } else {
            corePaint.setColor(Color.rgb(180, 100, 80)); // reddish on death
        }
        canvas.drawCircle(drawX, drawY, r, corePaint);

        // Inner highlight spot (top-left of circle to suggest a light source)
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(Color.argb(180, 255, 255, 255));
        canvas.drawCircle(drawX - r * 0.25f, drawY - r * 0.25f, r * 0.3f, innerPaint);
    }

    /** Reset player to a new position (called at level start/restart). */
    public void reset(float startX, float startY) {
        this.x  = startX;
        this.y  = startY;
        this.vx = 0f;
        this.vy = 0f;
        this.alive = true;
        deathShake.reset();
    }
}
