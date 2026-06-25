package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.EchoShadow;

import java.util.List;

/**
 * RangedEnemy — maintains distance, fires projectiles at the player.
 *
 * BEHAVIOUR:
 *   Tries to stay at PREFERRED_DIST pixels away from the player.
 *   If too close → backs away. If too far → advances.
 *   Every FIRE_INTERVAL seconds, fires one Projectile toward the player.
 *
 * VISUAL:
 *   Medium purple diamond/hexagon with a small "cannon" protrusion
 *   pointing toward the player. Flashes when firing.
 *
 * ECHO ON DEATH:
 *   Spawns ProjectileEcho — fires delayed shots at the player.
 */
public class RangedEnemy extends Enemy {

    private static final float MOVE_SPEED     = 90f;
    private static final float PREFERRED_DIST = 280f;
    private static final float TOLERANCE      = 50f;   // stop adjusting within this band
    private static final float FIRE_INTERVAL  = 2.2f;
    private static final float PROJ_SPEED     = 350f;

    private float fireTimer = 1.0f; // initial delay before first shot

    private final Paint cannonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float cannonAngle = 0f; // angle toward player, for drawing

    public RangedEnemy(float x, float y, float screenW, float screenH) {
        super(x, y, 46f, 46f, 2, screenW, screenH);
        bodyPaint.setColor(Color.rgb(187, 134, 252)); // purple
        cannonPaint.setColor(Color.rgb(140, 80, 220));
        cannonPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void updateBehavior(float dt, Player player, ArenaManager arena,
                                  List<Projectile> projectiles) {
        float dx   = player.getCenterX() - getCenterX();
        float dy   = player.getCenterY() - getCenterY();
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        // Update cannon angle toward player for drawing
        cannonAngle = (float)Math.toDegrees(Math.atan2(dy, dx));

        // Distance-keeping movement
        if (dist > PREFERRED_DIST + TOLERANCE) {
            // Too far — move toward player
            velX = (dx / dist) * MOVE_SPEED;
        } else if (dist < PREFERRED_DIST - TOLERANCE) {
            // Too close — back away
            velX = -(dx / dist) * MOVE_SPEED;
        } else {
            // In preferred range — stop horizontal movement
            velX *= 0.8f;
        }

        // Fire timer
        fireTimer -= dt;
        if (fireTimer <= 0f && dist < 600f) {
            // Fire projectile toward player's current position
            Projectile p = new Projectile(
                    getCenterX(), getCenterY(),
                    player.getCenterX(), player.getCenterY(),
                    PROJ_SPEED, Projectile.OWNER_ENEMY, 1,
                    screenW, screenH
            );
            projectiles.add(p);
            fireTimer = FIRE_INTERVAL;
        }
    }

    @Override
    protected void drawBody(Canvas canvas) {
        float cx = x + width / 2f;
        float cy = y + height / 2f;
        float r  = width / 2f;

        // Diamond body (rotated square)
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(45f);
        canvas.drawRect(-r, -r, r, r, bodyPaint);
        canvas.restore();

        // Cannon barrel pointing toward player
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(cannonAngle);
        canvas.drawRect(0, -6f, r + 12f, 6f, cannonPaint);
        canvas.restore();
    }

    @Override
    public int getEchoType() { return EchoShadow.TYPE_PROJECTILE; }
}
