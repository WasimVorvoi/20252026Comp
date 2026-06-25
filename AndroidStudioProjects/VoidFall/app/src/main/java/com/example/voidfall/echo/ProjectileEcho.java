package com.example.voidfall.echo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.entity.Player;
import com.example.voidfall.entity.Projectile;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectileEcho — spawned when a RangedEnemy is defeated.
 *
 * BEHAVIOUR:
 *   Every FIRE_INTERVAL seconds, fires a Projectile toward the player's
 *   CURRENT position. Unlike the RangedEnemy which tracks the player,
 *   the echo fires at where the player WAS when the shot was queued —
 *   so it punishes staying still in one spot.
 *
 *   The projectile is added to a local list inside this echo.
 *   CollisionManager checks this echo's projectile list for player hits.
 *   (This keeps projectile ownership clear.)
 *
 * VISUAL:
 *   Purple ghost diamond (same shape as RangedEnemy).
 *   Pulses slightly when about to fire.
 */
public class ProjectileEcho extends EchoShadow {

    private static final float FIRE_INTERVAL = 3.0f;
    private static final float PROJ_SPEED    = 280f;
    private static final float W = 44f;
    private static final float H = 44f;

    private float fireTimer = 1.5f; // slight initial delay

    // Local projectiles owned by this echo
    private final List<Projectile> myProjectiles = new ArrayList<>();

    private final Paint bodyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Stored screen dimensions for projectile creation
    private float screenW = 1080f, screenH = 1920f;

    public ProjectileEcho(float spawnX, float spawnY) {
        super(spawnX, spawnY);
        bodyPaint.setColor(Color.rgb(187, 134, 252));
        glowPaint.setColor(Color.rgb(187, 134, 252));
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(4f);
    }

    public void setScreenDimensions(float sw, float sh) {
        this.screenW = sw;
        this.screenH = sh;
    }

    @Override
    protected void updateBehavior(float dt, Player player) {
        fireTimer -= dt;

        // Update and prune own projectiles
        myProjectiles.removeIf(p -> !p.isActive());
        for (Projectile p : myProjectiles) p.update(dt);

        if (fireTimer <= 0f) {
            // Fire toward player's current position
            myProjectiles.add(new Projectile(
                    x + W / 2f, y + H / 2f,
                    player.getCenterX(), player.getCenterY(),
                    PROJ_SPEED, Projectile.OWNER_ENEMY, 1,
                    screenW, screenH
            ));
            fireTimer = FIRE_INTERVAL;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!active) return;

        // Draw own projectiles
        for (Projectile p : myProjectiles) p.draw(canvas);

        float cx = x + W / 2f;
        float cy = y + H / 2f;
        float r  = W / 2f;

        // Pulsing glow when about to fire
        float timeToFire = fireTimer;
        if (timeToFire < 0.8f) {
            float pulse = 1f - timeToFire / 0.8f;
            glowPaint.setAlpha((int)(drawAlpha * 0.6f * pulse));
            canvas.drawCircle(cx, cy, r * 1.5f * pulse + r, glowPaint);
        }

        // Ghost diamond body
        bodyPaint.setAlpha(drawAlpha);
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(45f);
        canvas.drawRect(-r, -r, r, r, bodyPaint);
        canvas.restore();
    }

    @Override
    public RectF getBounds() {
        return new RectF(x, y, x + W, y + H);
    }

    /** Returns projectiles owned by this echo (for CollisionManager). */
    public List<Projectile> getProjectiles() {
        return myProjectiles;
    }
}
