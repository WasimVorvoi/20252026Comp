package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.input.TiltInput;

import java.util.List;

/**
 * Boss — abstract base class for all boss enemies.
 *
 * DIFFERENCES FROM ENEMY:
 *   - Bosses have phases: behaviour changes at HP thresholds.
 *   - Bosses have a special ability triggered on a timer.
 *   - Bosses draw a large HP bar (rendered by HUDManager).
 *   - Bosses interact with ArenaManager directly (can force fractures).
 *   - Bosses receive the TiltInput so they can react to or manipulate tilt.
 *
 * PHASE SYSTEM:
 *   currentPhase starts at 1.
 *   Each subclass defines its own phase thresholds (e.g., Phase 2 at 50% HP).
 *   checkPhaseTransition() is called each frame; subclasses override it.
 *
 * HP BAR:
 *   Drawn by HUDManager at the top of the screen with the boss's name label.
 *
 * INVULNERABILITY WINDOW:
 *   Bosses have a brief invulnerability window (0.3s) after each hit
 *   so rapid auto-attacks don't one-shot them per second.
 */
public abstract class Boss {

    // Position and size
    protected float x, y;
    protected float width, height;
    // Velocity
    protected float velX, velY;
    // HP
    protected int hp;
    protected int maxHp;
    protected int currentPhase = 1;
    // State
    private boolean dead = false;
    // Hit flash
    private float hitFlashTimer = 0f;
    private static final float HIT_FLASH = 0.15f;
    // Invulnerability window per hit
    private float invulTimer = 0f;
    private static final float HIT_INVUL = 0.30f;
    // Special ability timer
    protected float specialTimer     = 0f;
    protected float specialInterval  = 5.0f; // subclass sets this
    // Gravity
    protected static final float GRAVITY = 700f;
    // Screen
    protected float screenW, screenH;
    // Name (shown on boss HP bar)
    protected String bossName = "BOSS";

    // Reusable paints
    protected final Paint bodyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected Boss(float x, float y, float width, float height,
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
    }

    // =========================================================================
    // Update
    // =========================================================================

    public void update(float dt, Player player, ArenaManager arena,
                       TiltInput tiltInput, List<Projectile> projectiles) {
        if (dead) return;

        if (hitFlashTimer > 0f) hitFlashTimer -= dt;
        if (invulTimer    > 0f) invulTimer    -= dt;

        specialTimer += dt;
        if (specialTimer >= specialInterval) {
            specialTimer = 0f;
            triggerSpecialAbility(arena, player, projectiles);
        }

        // Apply gravity
        velY += GRAVITY * dt;

        // Delegate to subclass phase AI
        updatePhase(dt, player, arena, tiltInput, projectiles);

        // Integrate position
        x += velX * dt;
        y += velY * dt;

        // Arena platform snapping
        float groundY = arena.getGroundY(x, x + width, y + height);
        if (groundY <= screenH + 50) {
            y    = groundY - height;
            velY = 0f;
        }

        // Screen edge clamping
        x = Math.max(20, Math.min(screenW - width - 20, x));

        // Phase check every frame
        checkPhaseTransition();
    }

    // =========================================================================
    // Abstract interface — subclasses implement these
    // =========================================================================

    protected abstract void updatePhase(float dt, Player player, ArenaManager arena,
                                        TiltInput tiltInput, List<Projectile> projectiles);

    protected abstract void triggerSpecialAbility(ArenaManager arena, Player player,
                                                   List<Projectile> projectiles);

    protected abstract void checkPhaseTransition();

    protected abstract void drawBody(Canvas canvas);

    public abstract String getBossName();

    // =========================================================================
    // Draw
    // =========================================================================

    public void draw(Canvas canvas) {
        if (dead) return;

        drawBody(canvas);

        // White flash on hit
        if (hitFlashTimer > 0f) {
            flashPaint.setAlpha((int)(200 * (hitFlashTimer / HIT_FLASH)));
            canvas.drawRect(x, y, x + width, y + height, flashPaint);
        }
    }

    // =========================================================================
    // Damage
    // =========================================================================

    /** Returns true if this hit killed the boss. */
    public boolean takeDamage(int amount) {
        if (dead || invulTimer > 0f) return false;
        hp -= amount;
        hitFlashTimer = HIT_FLASH;
        invulTimer    = HIT_INVUL;
        if (hp <= 0) {
            hp   = 0;
            dead = true;
            return true;
        }
        return false;
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public RectF getBounds()     { return new RectF(x, y, x + width, y + height); }
    public float getX()          { return x; }
    public float getY()          { return y; }
    public float getWidth()      { return width; }
    public float getHeight()     { return height; }
    public float getCenterX()    { return x + width / 2f; }
    public float getCenterY()    { return y + height / 2f; }
    public int   getHp()         { return hp; }
    public int   getMaxHp()      { return maxHp; }
    public int   getCurrentPhase(){ return currentPhase; }
    public boolean isDead()      { return dead; }
}
