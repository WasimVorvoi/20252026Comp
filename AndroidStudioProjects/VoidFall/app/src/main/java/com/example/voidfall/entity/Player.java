package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.frozenanimlib.AnimationManager;
import com.frozenanimlib.FadeAnimation;
import com.frozenanimlib.FloatRiseAnimation;
import com.frozenanimlib.FrameSequenceAnimation;
import com.frozenanimlib.ShakeAnimation;

import java.util.List;

/**
 * Player — the character the player controls via tilt.
 *
 * MOVEMENT:
 *   tiltX and tiltY map to horizontal and vertical velocity additions each frame.
 *   Damping (velX *= 0.82) simulates friction — without it the player would
 *   accelerate indefinitely.
 *   Gravity pulls the player down; CollisionManager snaps them to the arena surface.
 *
 * ATTACK:
 *   The player auto-attacks any enemy within ATTACK_RANGE pixels every ATTACK_COOLDOWN.
 *   No button press required — proximity triggers the attack.
 *   This keeps the player focused on tilt/movement, not tapping.
 *
 * DASH:
 *   Triggered by a strong tilt (|tiltX| > DASH_THRESHOLD).
 *   Gives a burst of velocity in the tilt direction.
 *   2-second cooldown.
 *
 * INVULNERABILITY FRAMES (i-frames):
 *   After taking damage, the player is invulnerable for INVUL_DURATION seconds.
 *   This prevents the player from being damaged multiple times by the same collision.
 *   Classic game design pattern — ensures damage feels fair.
 *   During i-frames the player flashes (FadeAnimation blink).
 *
 * ANIMATION (uses FrozenAnimLib JAR):
 *   hitFlash    — FadeAnimation blink on i-frames
 *   dashTrail   — FloatRiseAnimation for the dash afterimage
 *   attackAnim  — FrameSequenceAnimation for the 4-frame slash effect
 */
public class Player {

    // Movement constants
    private static final float MOVE_SPEED      = 550f;  // pixels/s per unit tilt
    private static final float DAMPING         = 0.80f;
    private static final float GRAVITY         = 900f;
    private static final float DASH_SPEED      = 800f;
    private static final float DASH_THRESHOLD  = 0.75f; // tilt magnitude to trigger dash
    private static final float DASH_COOLDOWN   = 2.0f;
    private static final float ATTACK_RANGE    = 90f;
    private static final float ATTACK_COOLDOWN = 0.55f;
    private static final float ATTACK_DAMAGE   = 1f;
    private static final float INVUL_DURATION  = 1.5f;
    private static final int   MAX_HP          = 3;

    // Size
    private static final float W = 46f;
    private static final float H = 58f;

    // Position and velocity
    private float x, y;
    private float velX, velY;

    // HP
    private int hp = MAX_HP;
    private boolean dead = false;

    // Timers
    private float attackTimer  = 0f;
    private float dashCooldown = 0f;
    private float invulTimer   = 0f;

    // Dash state (for afterimage position)
    private boolean dashing   = false;
    private float   dashTimer = 0f;
    private float   afterX, afterY; // afterimage position

    // Screen bounds
    private final float screenW, screenH;

    // ---- Animation (FrozenAnimLib) ----
    private final AnimationManager animManager = new AnimationManager();
    private final FadeAnimation       hitFlashAnim;
    private final FloatRiseAnimation  dashTrailAnim;
    private final FrameSequenceAnimation attackAnim;

    // Drawing
    private final Paint bodyPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flashPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint afterPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  bodyPath    = new Path();

    public Player(float startX, float startY, float screenW, float screenH) {
        this.x       = startX - W / 2f;
        this.y       = startY - H / 2f;
        this.screenW = screenW;
        this.screenH = screenH;

        bodyPaint.setColor(Color.rgb(0, 255, 200));
        corePaint.setColor(Color.rgb(0, 180, 140));
        flashPaint.setColor(Color.WHITE);
        afterPaint.setColor(Color.argb(80, 0, 212, 255));

        // Register animations with FrozenAnimLib AnimationManager
        hitFlashAnim  = animManager.registerFade("hit_flash",   1f, 0f, INVUL_DURATION, false);
        dashTrailAnim = animManager.registerFloatRise("dash_trail", 30f, 0.4f, false);
        attackAnim    = animManager.registerFrameSequence("attack", 4, 0.07f, false);
    }

    // =========================================================================
    // Update
    // =========================================================================

    public void update(float dt, float tiltX, float tiltY,
                       ArenaManager arena, List<Projectile> projectiles) {
        if (dead) return;

        // Tick timers
        attackTimer  -= dt;
        dashCooldown -= dt;
        if (invulTimer > 0f) invulTimer -= dt;
        if (dashTimer  > 0f) dashTimer  -= dt; else dashing = false;

        // Update animations
        animManager.updateAll(dt);

        // Dash detection: strong tilt triggers dash
        if (dashCooldown <= 0f && Math.abs(tiltX) > DASH_THRESHOLD) {
            dash(tiltX > 0 ? 1f : -1f);
        }

        // Horizontal movement from tilt
        if (!dashing) {
            velX += tiltX * MOVE_SPEED * dt * 3f;
            velX *= DAMPING;
        }

        // Vertical movement from tilt
        velY += tiltY * MOVE_SPEED * 0.5f * dt * 3f;
        velY += GRAVITY * dt;          // constant downward gravity
        velY *= DAMPING;

        // Integrate position
        x += velX * dt;
        y += velY * dt;

        // Arena platform snapping
        float groundY = arena.getGroundY(x, x + W, y + H);
        if (groundY <= screenH + 50) {
            y    = groundY - H;
            velY = 0f;
        }

        // Screen bounds clamping
        x = Math.max(0, Math.min(screenW - W, x));
        y = Math.max(0, Math.min(screenH - H, y));

        // Fall death
        if (y > screenH) {
            dead = true;
        }

        // Auto-attack
        // (actual hit detection is done in CollisionManager; here we just track cooldown)
        if (attackTimer <= 0f) {
            attackTimer = ATTACK_COOLDOWN;
            attackAnim.reset();  // restart slash animation
        }
    }

    /** Burst of velocity in the given horizontal direction. */
    private void dash(float dir) {
        velX       = dir * DASH_SPEED;
        afterX     = x;
        afterY     = y;
        dashing    = true;
        dashTimer  = 0.25f;
        dashCooldown = DASH_COOLDOWN;
        dashTrailAnim.reset();
    }

    // =========================================================================
    // Damage / HP
    // =========================================================================

    /**
     * Called by CollisionManager when an enemy or projectile touches the player.
     * Invulnerability frames prevent taking damage again for INVUL_DURATION seconds.
     */
    public void takeDamage(int amount) {
        if (dead || invulTimer > 0f) return;
        hp -= amount;
        invulTimer = INVUL_DURATION;
        hitFlashAnim.reset();
        if (hp <= 0) {
            hp   = 0;
            dead = true;
        }
    }

    public void resetHP() { hp = MAX_HP; dead = false; }

    // =========================================================================
    // Draw
    // =========================================================================

    public void draw(Canvas canvas) {
        if (dead) return;

        // Dash afterimage (FloatRiseAnimation controls alpha)
        if (dashing && dashTrailAnim.isRunning()) {
            float offsetY = dashTrailAnim.getOffsetY();
            afterPaint.setAlpha(dashTrailAnim.getAlphaInt());
            drawPlayerShape(canvas, afterX, afterY + offsetY, afterPaint, afterPaint);
        }

        // I-frame blink: FadeAnimation controls whether player is visible this frame
        boolean showPlayer = true;
        if (invulTimer > 0f) {
            // Blink by toggling visibility based on hit flash animation alpha
            float alpha = hitFlashAnim.getAlpha();
            showPlayer = alpha > 0.5f || (((int)(invulTimer * 10)) % 2 == 0);
        }

        if (showPlayer) {
            // Attack frame overlay on the first 2 of 4 attack frames
            if (attackAnim.isRunning() && attackAnim.getCurrentFrameIndex() <= 1) {
                drawAttackSlash(canvas);
            }
            drawPlayerShape(canvas, x, y, bodyPaint, corePaint);
        }
    }

    /** Draws the player's hexagonal body shape at position (px, py). */
    private void drawPlayerShape(Canvas canvas, float px, float py,
                                  Paint outer, Paint inner) {
        float cx = px + W / 2f;
        float cy = py + H / 2f;
        float rx  = W / 2f;
        float ry  = H / 2f;

        // Hexagon: 6 points
        bodyPath.reset();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i - Math.PI / 6;
            float px2    = cx + rx * (float)Math.cos(angle);
            float py2    = cy + ry * (float)Math.sin(angle);
            if (i == 0) bodyPath.moveTo(px2, py2);
            else         bodyPath.lineTo(px2, py2);
        }
        bodyPath.close();

        canvas.drawPath(bodyPath, outer);

        // Inner diamond (core)
        float ir = Math.min(rx, ry) * 0.45f;
        bodyPath.reset();
        bodyPath.moveTo(cx,      cy - ir);
        bodyPath.lineTo(cx + ir, cy);
        bodyPath.lineTo(cx,      cy + ir);
        bodyPath.lineTo(cx - ir, cy);
        bodyPath.close();
        canvas.drawPath(bodyPath, inner);
    }

    /** Draws a short arc/slash effect to the side during the attack animation. */
    private void drawAttackSlash(Canvas canvas) {
        Paint slashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slashPaint.setColor(Color.argb(180, 0, 255, 200));
        slashPaint.setStyle(Paint.Style.STROKE);
        slashPaint.setStrokeWidth(4f);
        float cx = x + W / 2f;
        float cy = y + H / 2f;
        float dir = velX >= 0 ? 1f : -1f;
        canvas.drawArc(
                new RectF(cx + dir * 20 - 40, cy - 40, cx + dir * 20 + 40, cy + 40),
                dir > 0 ? -60 : 120,
                120,
                false,
                slashPaint
        );
    }

    // =========================================================================
    // Touch
    // =========================================================================

    /** Handle screen tap (reserved for future ability trigger). */
    public void onTap(float tapX, float tapY) {
        // Future: activate special ability on tap
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public RectF getBounds()      { return new RectF(x, y, x + W, y + H); }
    public float getX()           { return x; }
    public float getY()           { return y; }
    public float getWidth()       { return W; }
    public float getHeight()      { return H; }
    public float getCenterX()     { return x + W / 2f; }
    public float getCenterY()     { return y + H / 2f; }
    public float getVelX()        { return velX; }
    public float getVelY()        { return velY; }
    public int   getHp()          { return hp; }
    public int   getMaxHp()       { return MAX_HP; }
    public boolean isDead()       { return dead; }
    public boolean isInvulnerable(){ return invulTimer > 0f; }
    public float getAttackRange() { return ATTACK_RANGE; }
    public float getAttackDamage(){ return ATTACK_DAMAGE; }
    public boolean canAttack()    { return attackTimer <= 0f; }

    public void setPosition(float nx, float ny) { x = nx; y = ny; }
}
