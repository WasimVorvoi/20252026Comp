package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.EchoShadow;

import java.util.List;

/**
 * Enemy — abstract base class for all enemy types.
 *
 * SUBCLASSES:
 *   FastEnemy   — high speed, dashes at player, spawns DashEcho
 *   HeavyEnemy  — slow, AOE melee, spawns BlockEcho
 *   RangedEnemy — fires projectiles, spawns ProjectileEcho
 *
 * ABSTRACT METHODS:
 *   updateBehavior() — each subclass defines its own movement AI
 *   drawBody()       — each subclass draws its own shape/colour
 *   getEchoType()    — which type of echo to spawn on death
 *
 * HIT FLASH:
 *   When the player attacks this enemy, hitFlashTimer is set.
 *   While > 0, drawBody() is called with an override to flash white.
 *   This gives immediate visual feedback that the hit registered.
 *
 * GRAVITY:
 *   Enemies are pulled downward toward the arena surface.
 *   CollisionManager resolves them onto the nearest piece each frame.
 *   If an enemy is in a gap (no piece below), it falls and eventually dies.
 */
public abstract class Enemy {

    // Position and size
    protected float x, y;
    protected float width, height;
    // Velocity
    protected float velX, velY;
    // Last velocity X, saved for DashEcho direction
    protected float lastVelX = 0f;
    // HP
    protected int hp;
    protected int maxHp;
    // State
    private boolean dead = false;
    // Hit flash: white overlay for 0.2 seconds after taking damage
    private float hitFlashTimer = 0f;
    private static final float HIT_FLASH_DURATION = 0.20f;
    // Gravity constant applied every frame (pixels/second²)
    protected static final float GRAVITY = 800f;
    // Screen dimensions
    protected float screenW, screenH;

    // Reusable paints
    protected final Paint bodyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint hpPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected Enemy(float x, float y, float width, float height,
                    int hp, float screenW, float screenH) {
        this.x       = x;
        this.y       = y;
        this.width   = width;
        this.height  = height;
        this.hp      = hp;
        this.maxHp   = hp;
        this.screenW = screenW;
        this.screenH = screenH;

        flashPaint.setColor(Color.argb(200, 255, 255, 255));
        hpPaint.setColor(Color.RED);
        hpPaint.setStyle(Paint.Style.FILL);
    }

    // =========================================================================
    // Public update — common logic + delegated AI
    // =========================================================================

    public void update(float dt, Player player, ArenaManager arena, List<Projectile> projectiles) {
        if (dead) return;

        // Tick hit flash
        if (hitFlashTimer > 0f) hitFlashTimer -= dt;

        // Apply gravity — pulled downward each frame
        velY += GRAVITY * dt;

        // Delegate to subclass AI
        updateBehavior(dt, player, arena, projectiles);

        // Move
        x      += velX * dt;
        y      += velY * dt;
        lastVelX = velX;

        // Arena collision: snap onto the nearest piece top surface
        float groundY = arena.getGroundY(x, x + width, y + height);
        if (groundY <= screenH) {
            // There is a platform below — snap to it
            y    = groundY - height;
            velY = 0f;
        }

        // Fall to death if off the bottom of the screen
        if (y > screenH + 100) {
            dead = true;
        }

        // Clamp to screen width (enemies don't walk off the sides)
        x = Math.max(0, Math.min(screenW - width, x));
    }

    // =========================================================================
    // Abstract interface
    // =========================================================================

    /** Each enemy subclass defines its own movement + attack pattern here. */
    protected abstract void updateBehavior(float dt, Player player,
                                           ArenaManager arena, List<Projectile> projectiles);

    /** Each enemy subclass draws its own shape. */
    protected abstract void drawBody(Canvas canvas);

    /** Returns the echo type to spawn on death (EchoShadow.TYPE_*). */
    public abstract int getEchoType();

    // =========================================================================
    // Draw
    // =========================================================================

    public void draw(Canvas canvas) {
        if (dead) return;

        drawBody(canvas);

        // White flash overlay when recently hit
        if (hitFlashTimer > 0f) {
            flashPaint.setAlpha((int)(200 * (hitFlashTimer / HIT_FLASH_DURATION)));
            canvas.drawRect(x, y, x + width, y + height, flashPaint);
        }

        // Small HP bar above enemy
        drawHpBar(canvas);
    }

    private void drawHpBar(Canvas canvas) {
        if (hp >= maxHp) return; // don't show if at full HP
        float barW   = width * 0.8f;
        float barH   = 5f;
        float barX   = x + (width - barW) / 2f;
        float barY   = y - barH - 4f;

        // Background
        hpPaint.setColor(Color.argb(150, 60, 0, 0));
        canvas.drawRect(barX, barY, barX + barW, barY + barH, hpPaint);

        // Fill
        hpPaint.setColor(Color.rgb(255, 50, 50));
        float fill = barW * ((float)hp / maxHp);
        canvas.drawRect(barX, barY, barX + fill, barY + barH, hpPaint);
    }

    // =========================================================================
    // Damage
    // =========================================================================

    /**
     * Called by CollisionManager when player's attack lands.
     * Returns true if the hit killed this enemy.
     */
    public boolean takeDamage(int amount) {
        if (dead) return false;
        hp -= amount;
        hitFlashTimer = HIT_FLASH_DURATION;
        if (hp <= 0) {
            dead = true;
            return true;
        }
        return false;
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public float getX()          { return x; }
    public float getY()          { return y; }
    public float getWidth()      { return width; }
    public float getHeight()     { return height; }
    public float getCenterX()    { return x + width / 2f; }
    public float getCenterY()    { return y + height / 2f; }
    public float getLastVelocityX() { return lastVelX; }
    public boolean isDead()      { return dead; }
    public int getHp()           { return hp; }
}
