package com.example.voidfall.echo;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.example.voidfall.entity.Player;

/**
 * EchoShadow — abstract base for all echo shadows.
 *
 * WHAT IS AN ECHO SHADOW:
 *   When an enemy is defeated, it leaves behind a "ghost" at the death location.
 *   This ghost REPLAYS the enemy's defining behaviour for 15 seconds.
 *   It creates a danger zone that persists and accumulates as more enemies die.
 *
 *   This mechanic makes combat decisions meaningful:
 *   "If I kill this FastEnemy here, the dash echo will block that corridor."
 *   Players who think ahead can use echoes strategically.
 *
 * LIFESPAN:
 *   Each echo lasts LIFESPAN seconds, then fades out and is removed.
 *   The alpha decays in the final 3 seconds for a visual fade.
 *
 * TYPE CONSTANTS:
 *   Used by Enemy.getEchoType() to tell GameView which subclass to spawn.
 *
 * SUBCLASSES:
 *   DashEcho       — from FastEnemy  — horizontally dashing ghost
 *   ProjectileEcho — from RangedEnemy — fires delayed shots
 *   BlockEcho      — from HeavyEnemy  — stationary impassable zone
 */
public abstract class EchoShadow {

    public static final int TYPE_NONE       = 0;
    public static final int TYPE_DASH       = 1;
    public static final int TYPE_PROJECTILE = 2;
    public static final int TYPE_BLOCK      = 3;

    protected static final float LIFESPAN    = 15.0f;
    protected static final float FADE_START  = 12.0f;  // begin fading at 12s

    protected float x, y;
    protected float lifeTimer = 0f;
    protected boolean active  = true;

    // Current draw alpha [0-255] — fades in last 3 seconds
    protected int drawAlpha = 180;

    protected EchoShadow(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // =========================================================================
    // Update
    // =========================================================================

    public void update(float dt, Player player) {
        if (!active) return;

        lifeTimer += dt;

        // Fade out in the last (LIFESPAN - FADE_START) seconds
        if (lifeTimer > FADE_START) {
            float fadeProgress = (lifeTimer - FADE_START) / (LIFESPAN - FADE_START);
            drawAlpha = (int)(180f * (1f - fadeProgress));
        }

        if (lifeTimer >= LIFESPAN) {
            active = false;
            return;
        }

        updateBehavior(dt, player);
    }

    // =========================================================================
    // Abstract interface
    // =========================================================================

    protected abstract void updateBehavior(float dt, Player player);

    public abstract void draw(Canvas canvas);

    /** Returns the collision bounds used by CollisionManager. */
    public abstract RectF getBounds();

    // =========================================================================
    // Accessors
    // =========================================================================

    public boolean isActive() { return active; }
    public float   getX()     { return x; }
    public float   getY()     { return y; }
}
