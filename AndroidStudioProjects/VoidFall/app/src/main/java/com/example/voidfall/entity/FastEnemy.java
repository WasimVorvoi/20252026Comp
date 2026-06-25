package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.EchoShadow;

import java.util.List;

/**
 * FastEnemy — dashes toward the player at high speed.
 *
 * BEHAVIOUR:
 *   Patrols slowly until the player is within CHASE_RANGE pixels.
 *   Then charges directly at the player at DASH_SPEED.
 *   After closing to within MELEE_RANGE, deals 1 damage and briefly backs off.
 *
 * VISUAL:
 *   Slim forward-pointing triangle in electric cyan.
 *   Points in the direction of movement.
 *
 * ECHO ON DEATH:
 *   Spawns DashEcho — a ghost that replays the dash motion horizontally.
 */
public class FastEnemy extends Enemy {

    private static final float PATROL_SPEED = 80f;
    private static final float DASH_SPEED   = 320f;
    private static final float CHASE_RANGE  = 400f;
    private static final float MELEE_RANGE  = 55f;
    private static final float ATTACK_COOLDOWN = 1.2f;

    private boolean chasing      = false;
    private float   attackTimer  = 0f;
    // Patrol direction: +1 = right, -1 = left
    private float   patrolDir    = 1f;
    private float   patrolTimer  = 0f;

    // Path reused each frame for drawing the triangle body
    private final Path bodyPath = new Path();

    public FastEnemy(float x, float y, float screenW, float screenH) {
        super(x, y, 38f, 48f, 1, screenW, screenH);
        bodyPaint.setColor(Color.rgb(0, 229, 255)); // electric cyan
        patrolDir = (Math.random() > 0.5) ? 1f : -1f;
    }

    @Override
    protected void updateBehavior(float dt, Player player, ArenaManager arena,
                                  List<Projectile> projectiles) {
        attackTimer -= dt;

        float distToPlayer = Math.abs(player.getCenterX() - getCenterX());

        if (distToPlayer < CHASE_RANGE) {
            // Chase mode: accelerate toward player
            chasing = true;
            float dir = player.getCenterX() > getCenterX() ? 1f : -1f;
            velX += dir * DASH_SPEED * dt * 4f;
            // Cap speed
            velX = Math.max(-DASH_SPEED, Math.min(DASH_SPEED, velX));

            // Melee attack
            if (distToPlayer < MELEE_RANGE && attackTimer <= 0f) {
                player.takeDamage(1);
                attackTimer = ATTACK_COOLDOWN;
                // Bounce back slightly
                velX = -dir * 150f;
            }
        } else {
            // Patrol: move back and forth
            chasing = false;
            velX    = patrolDir * PATROL_SPEED;
            patrolTimer += dt;
            if (patrolTimer > 2.5f) {
                patrolDir   = -patrolDir;
                patrolTimer = 0f;
            }
            // Apply friction when not chasing
            velX *= 0.9f;
        }
    }

    @Override
    protected void drawBody(Canvas canvas) {
        // Draw a forward-pointing triangle
        float cx = x + width / 2f;
        float cy = y + height / 2f;
        float tipX = (velX >= 0) ? x + width : x; // tip points in direction of motion
        float baseX = (velX >= 0) ? x : x + width;

        bodyPath.reset();
        bodyPath.moveTo(tipX, cy);                    // tip
        bodyPath.lineTo(baseX, y);                     // top-back corner
        bodyPath.lineTo(baseX, y + height);            // bottom-back corner
        bodyPath.close();

        canvas.drawPath(bodyPath, bodyPaint);

        // Bright glow dot at tip
        bodyPaint.setAlpha(180);
        canvas.drawCircle(tipX, cy, 6f, bodyPaint);
        bodyPaint.setAlpha(255);
    }

    @Override
    public int getEchoType() { return EchoShadow.TYPE_DASH; }
}
