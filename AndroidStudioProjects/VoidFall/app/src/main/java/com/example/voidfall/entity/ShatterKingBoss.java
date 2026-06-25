package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.input.TiltInput;

import java.util.List;

/**
 * ShatterKingBoss — Level 3 boss. Forces arena fractures as its main weapon.
 *
 * DESIGN PHILOSOPHY:
 *   The Shatter King REWARDS knowledge of the fracture system.
 *   The player has learned that fractures create gaps.
 *   The boss exploits this by fracturing aggressively, making the arena unstable.
 *   The player must use what they learned in levels 1-2 to navigate and fight.
 *
 * PHASES:
 *   Phase 1 (HP > 15): Slow advance, fractures arena every 5s, weak attacks.
 *   Phase 2 (HP <= 15): Faster movement, fractures every 3s, spawns FastEnemies,
 *                        charges across the screen.
 *
 * SPECIAL ABILITY — "SHATTER PULSE":
 *   Triggers 3 fractures at once + AOE damage around self.
 *   Visual: expanding ring of cracks, screen shake (via arena.triggerFracture).
 *
 * VISUAL:
 *   Large angular figure with jagged crown-like spikes along the top.
 *   Deep red-magenta colour. Phase 2 adds a pulsing glow.
 */
public class ShatterKingBoss extends Boss {

    private static final int   PHASE2_HP_THRESHOLD = 15;
    private static final float MOVE_SPEED_P1   = 75f;
    private static final float MOVE_SPEED_P2   = 140f;
    private static final float MELEE_RANGE     = 90f;
    private static final float MELEE_DAMAGE    = 1;
    private static final float MELEE_COOLDOWN  = 1.8f;

    private float meleeTimer = 0f;
    // Phase 2 charge state
    private boolean charging   = false;
    private float   chargeTimer = 0f;
    private float   chargeDir   = 1f;

    // Crown spike path (reused each frame)
    private final Path crownPath = new Path();
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ShatterKingBoss(float x, float y, float screenW, float screenH) {
        super(x, y, 100f, 110f, 30, screenW, screenH);
        bodyPaint.setColor(Color.rgb(255, 45, 85));    // vivid red
        glowPaint.setColor(Color.argb(80, 255, 45, 85));
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(6f);
        specialInterval = 5.0f; // special ability every 5 seconds
    }

    @Override
    protected void updatePhase(float dt, Player player, ArenaManager arena,
                                TiltInput tiltInput, List<Projectile> projectiles) {
        meleeTimer -= dt;
        float speed = (currentPhase == 1) ? MOVE_SPEED_P1 : MOVE_SPEED_P2;
        float dx    = player.getCenterX() - getCenterX();

        if (currentPhase == 2 && charging) {
            // Phase 2 charge: dash across screen
            velX        = chargeDir * MOVE_SPEED_P2 * 2.5f;
            chargeTimer -= dt;
            if (chargeTimer <= 0f) {
                charging = false;
                velX     = 0f;
            }
        } else {
            // Normal advance toward player
            velX = (dx > 0 ? 1f : -1f) * speed;
            velX *= 0.9f; // some drag
        }

        // Melee contact damage
        float dist = Math.abs(dx);
        if (dist < MELEE_RANGE && meleeTimer <= 0f) {
            player.takeDamage(MELEE_DAMAGE);
            meleeTimer = MELEE_COOLDOWN;
        }

        // Phase 2: trigger a charge every 8 seconds
        if (currentPhase == 2 && !charging) {
            chargeDir   = dx > 0 ? 1f : -1f;
            charging    = true;
            chargeTimer = 0.5f;
        }
    }

    @Override
    protected void triggerSpecialAbility(ArenaManager arena, Player player,
                                          List<Projectile> projectiles) {
        // SHATTER PULSE: fracture 3 arena pieces at once
        arena.triggerFracture(3);
        // AOE damage if player is within 200 pixels
        float dist = (float)Math.sqrt(
                Math.pow(player.getCenterX() - getCenterX(), 2) +
                Math.pow(player.getCenterY() - getCenterY(), 2));
        if (dist < 200f) player.takeDamage(1);
    }

    @Override
    protected void checkPhaseTransition() {
        if (currentPhase == 1 && hp <= PHASE2_HP_THRESHOLD) {
            currentPhase    = 2;
            specialInterval = 3.0f; // fracture more often in phase 2
        }
    }

    @Override
    protected void drawBody(Canvas canvas) {
        float cx = x + width / 2f;

        // Phase 2 outer glow
        if (currentPhase == 2) {
            float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 0.005));
            glowPaint.setAlpha((int)(40 + 80 * pulse));
            canvas.drawRoundRect(x - 10, y - 10, x + width + 10, y + height + 10,
                    15f, 15f, glowPaint);
        }

        // Main body (rounded rect)
        bodyPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(x, y + 28f, x + width, y + height, 8f, 8f, bodyPaint);

        // Crown spikes along the top
        crownPath.reset();
        float spikeBase = y + 28f;
        int   numSpikes = 5;
        float spikeW    = width / (numSpikes * 2f);
        float spikeH    = 32f;

        crownPath.moveTo(x, spikeBase);
        for (int i = 0; i < numSpikes; i++) {
            float bx  = x + i * (width / numSpikes);
            float bx2 = bx + spikeW;
            crownPath.lineTo(bx, spikeBase);
            crownPath.lineTo(bx + spikeW / 2f, spikeBase - spikeH);
            crownPath.lineTo(bx2, spikeBase);
        }
        crownPath.lineTo(x + width, spikeBase);
        crownPath.lineTo(x + width, spikeBase);
        canvas.drawPath(crownPath, bodyPaint);

        // Eyes (two bright dots)
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.WHITE);
        canvas.drawCircle(cx - 20f, y + 55f, 7f, eyePaint);
        canvas.drawCircle(cx + 20f, y + 55f, 7f, eyePaint);
        eyePaint.setColor(Color.rgb(255, 45, 85));
        canvas.drawCircle(cx - 20f, y + 55f, 3.5f, eyePaint);
        canvas.drawCircle(cx + 20f, y + 55f, 3.5f, eyePaint);
    }

    @Override public String getBossName() { return "SHATTER KING"; }
}
