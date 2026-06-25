package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.EchoShadow;

import java.util.List;

/**
 * HeavyEnemy — slow, tanky, deals AOE damage with a shockwave slam.
 *
 * BEHAVIOUR:
 *   Moves slowly toward the player at all times (no patrol — always advancing).
 *   When within SLAM_RANGE, it charges an AOE slam (0.8s windup then slams).
 *   The slam damages the player if they are within AOE_RADIUS.
 *   After slamming, there is a SLAM_COOLDOWN recovery period.
 *
 * WINDUP VISUAL:
 *   During the 0.8s windup, the enemy flashes orange-red and pulses larger.
 *   This gives the player time to move away — telegraphing the attack.
 *
 * VISUAL:
 *   Large chunky rectangle with rounded corners, orange-red.
 *   A darker inner rectangle creates a "heavy armor" layered look.
 *
 * ECHO ON DEATH:
 *   Spawns BlockEcho — a stationary ghost block at death position.
 */
public class HeavyEnemy extends Enemy {

    private static final float MOVE_SPEED    = 55f;
    private static final float SLAM_RANGE    = 100f;
    private static final float AOE_RADIUS    = 130f;
    private static final float SLAM_WINDUP   = 0.8f;
    private static final float SLAM_COOLDOWN = 2.5f;

    private enum State { WALKING, WINDING_UP, SLAMMING, RECOVERING }
    private State state      = State.WALKING;
    private float stateTimer = 0f;

    private final Paint innerPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint aoeRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public HeavyEnemy(float x, float y, float screenW, float screenH) {
        super(x, y, 68f, 72f, 3, screenW, screenH);
        bodyPaint.setColor(Color.rgb(255, 107, 53));  // orange-red
        innerPaint.setColor(Color.rgb(180, 60, 20));
        aoeRingPaint.setStyle(Paint.Style.STROKE);
        aoeRingPaint.setStrokeWidth(3f);
        aoeRingPaint.setColor(Color.argb(120, 255, 150, 50));
    }

    @Override
    protected void updateBehavior(float dt, Player player, ArenaManager arena,
                                  List<Projectile> projectiles) {
        stateTimer += dt;

        float distToPlayer = distanceTo(player);

        switch (state) {
            case WALKING:
                // Slow steady advance
                float dir = player.getCenterX() > getCenterX() ? 1f : -1f;
                velX = dir * MOVE_SPEED;

                if (distToPlayer < SLAM_RANGE) {
                    state      = State.WINDING_UP;
                    stateTimer = 0f;
                    velX       = 0f;
                }
                break;

            case WINDING_UP:
                velX = 0f;
                if (stateTimer >= SLAM_WINDUP) {
                    state      = State.SLAMMING;
                    stateTimer = 0f;
                    // Check if player is in AOE
                    if (distToPlayer < AOE_RADIUS) {
                        player.takeDamage(2);
                    }
                }
                break;

            case SLAMMING:
                // Brief flash frame, then recover
                if (stateTimer > 0.1f) {
                    state      = State.RECOVERING;
                    stateTimer = 0f;
                }
                break;

            case RECOVERING:
                velX = 0f;
                if (stateTimer >= SLAM_COOLDOWN) {
                    state      = State.WALKING;
                    stateTimer = 0f;
                }
                break;
        }
    }

    @Override
    protected void drawBody(Canvas canvas) {
        // Pulse size during windup
        float scale = 1f;
        if (state == State.WINDING_UP) {
            scale = 1f + 0.15f * (float)Math.sin(stateTimer * Math.PI / SLAM_WINDUP * 4);
        }

        float drawW = width  * scale;
        float drawH = height * scale;
        float drawX = x + (width  - drawW) / 2f;
        float drawY = y + (height - drawH) / 2f;

        // Outer body
        canvas.drawRoundRect(new RectF(drawX, drawY, drawX + drawW, drawY + drawH),
                12f, 12f, bodyPaint);
        // Inner darker rect for armour look
        float margin = 8f;
        canvas.drawRoundRect(new RectF(drawX + margin, drawY + margin,
                        drawX + drawW - margin, drawY + drawH - margin),
                6f, 6f, innerPaint);

        // AOE ring visible during windup
        if (state == State.WINDING_UP || state == State.SLAMMING) {
            float progress = stateTimer / SLAM_WINDUP;
            aoeRingPaint.setAlpha((int)(100 + 100 * progress));
            canvas.drawCircle(getCenterX(), getCenterY(), AOE_RADIUS * progress, aoeRingPaint);
        }
    }

    private float distanceTo(Player p) {
        float dx = p.getCenterX() - getCenterX();
        float dy = p.getCenterY() - getCenterY();
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    // Expose state timer for HeavyEnemy-specific behaviours
    public float getStateTimer() { return stateTimer; }
    public boolean isWindingUp() { return state == State.WINDING_UP; }

    @Override
    public int getEchoType() { return EchoShadow.TYPE_BLOCK; }
}
