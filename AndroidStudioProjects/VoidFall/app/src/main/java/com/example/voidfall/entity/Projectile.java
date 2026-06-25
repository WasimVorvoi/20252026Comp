package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Projectile — a moving object fired by enemies, bosses, or echo shadows.
 *
 * Projectiles travel in a straight line at constant velocity.
 * They deactivate (isActive = false) when they leave the screen
 * or when CollisionManager flags them as having hit a target.
 *
 * OWNER TYPES:
 *   OWNER_ENEMY  — damages the player on contact
 *   OWNER_PLAYER — (reserved for future use) damages enemies
 *
 * VISUAL:
 *   Small bright circle with a short glow trail.
 *   Colour depends on owner type.
 */
public class Projectile {

    public static final int OWNER_ENEMY  = 0;
    public static final int OWNER_PLAYER = 1;

    private float x, y;
    private final float velX, velY;
    private final int ownerType;
    private final int damage;
    private boolean active = true;

    private final float screenW, screenH;
    private static final float RADIUS = 12f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Trail: store last few positions for a motion-blur effect
    private static final int TRAIL_LEN = 5;
    private final float[] trailX = new float[TRAIL_LEN];
    private final float[] trailY = new float[TRAIL_LEN];
    private int trailHead = 0;

    public Projectile(float startX, float startY,
                      float targetX, float targetY,
                      float speed, int ownerType, int damage,
                      float screenW, float screenH) {
        this.x         = startX;
        this.y         = startY;
        this.ownerType = ownerType;
        this.damage    = damage;
        this.screenW   = screenW;
        this.screenH   = screenH;

        // Compute normalised direction toward target
        float dx   = targetX - startX;
        float dy   = targetY - startY;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        if (dist < 1f) dist = 1f; // avoid division by zero
        this.velX  = (dx / dist) * speed;
        this.velY  = (dy / dist) * speed;

        // Initialise trail to starting position
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }

        paint.setColor(ownerType == OWNER_ENEMY ? Color.rgb(255, 200, 0) : Color.rgb(0, 255, 180));
    }

    /** Constructor for projectiles fired in an explicit direction (not toward a target). */
    public static Projectile directional(float startX, float startY,
                                         float dirX, float dirY,
                                         float speed, int ownerType, int damage,
                                         float screenW, float screenH) {
        float dist = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        if (dist < 0.001f) dist = 1f;
        float tx = startX + (dirX / dist) * 100f;
        float ty = startY + (dirY / dist) * 100f;
        return new Projectile(startX, startY, tx, ty, speed, ownerType, damage, screenW, screenH);
    }

    public void update(float dt) {
        if (!active) return;

        // Update trail before moving
        trailX[trailHead] = x;
        trailY[trailHead] = y;
        trailHead = (trailHead + 1) % TRAIL_LEN;

        x += velX * dt;
        y += velY * dt;

        // Deactivate when off screen
        if (x < -50 || x > screenW + 50 || y < -50 || y > screenH + 50) {
            active = false;
        }
    }

    public void draw(Canvas canvas) {
        if (!active) return;

        // Draw trail (fading, smaller circles)
        for (int i = 0; i < TRAIL_LEN; i++) {
            int idx   = (trailHead + i) % TRAIL_LEN;
            float t   = (float)i / TRAIL_LEN;
            int alpha = (int)(30 + 80 * t);
            float r   = RADIUS * 0.4f * t;
            paint.setAlpha(alpha);
            canvas.drawCircle(trailX[idx], trailY[idx], r, paint);
        }

        // Draw main projectile
        paint.setAlpha(255);
        canvas.drawCircle(x, y, RADIUS, paint);
    }

    /** Call from CollisionManager when this projectile has hit something. */
    public void deactivate() { active = false; }

    public RectF getBounds() {
        return new RectF(x - RADIUS, y - RADIUS, x + RADIUS, y + RADIUS);
    }

    public float  getX()       { return x; }
    public float  getY()       { return y; }
    public int    getOwner()   { return ownerType; }
    public int    getDamage()  { return damage; }
    public boolean isActive()  { return active; }
}
