package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.input.TiltInput;

import java.util.ArrayList;
import java.util.List;

/**
 * MirrorBeastBoss — Level 7 boss. Creates tilt-reversal zones.
 *
 * DESIGN PHILOSOPHY:
 *   The player has mastered tilt movement across 6 levels.
 *   Mirror Beast strips that mastery away by inverting tilt in defined regions.
 *   Learning where the mirror zones are and navigating around them
 *   IS the challenge — not just dealing damage.
 *
 * MIRROR ZONES:
 *   Rectangular colored zones on the arena.
 *   When the player is inside a zone, TiltInput.setMirrorX(true) is called.
 *   The player's controls are reversed. This forces them to think inversely.
 *   Phase 2 adds a second overlapping zone.
 *
 * PHASES:
 *   Phase 1 (HP > 17): 1 mirror zone, moves between zones, fires occasionally.
 *   Phase 2 (HP <= 17): 2 mirror zones, summons RangedEnemy clones.
 *
 * SPECIAL ABILITY — "MIRROR FLIP":
 *   ALL tilt globally inverted for 5 seconds.
 *   Visual: screen briefly flashes blue.
 *
 * VISUAL:
 *   Split/mirrored body design — left half and right half are slightly different.
 *   Reflective teal-white colour scheme.
 */
public class MirrorBeastBoss extends Boss {

    private static final int   PHASE2_THRESHOLD = 17;
    private static final float MOVE_SPEED       = 95f;
    private static final float MELEE_RANGE      = 80f;
    private static final float PROJ_INTERVAL    = 3.0f;
    private static final float MIRROR_FLIP_DURATION = 5.0f;

    // Mirror zones: list of RectF regions on screen
    private final List<RectF> mirrorZones = new ArrayList<>();
    private float projTimer   = 2.0f;
    // Global mirror flip state
    private float flipTimer   = 0f;
    private boolean flipActive = false;

    private final Paint zonePaint   = new Paint();
    private final Paint zoneBorder  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bodyLeft    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bodyRight   = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MirrorBeastBoss(float x, float y, float screenW, float screenH) {
        super(x, y, 95f, 105f, 35, screenW, screenH);
        specialInterval = 7.0f;
        bodyPaint.setColor(Color.rgb(100, 255, 218)); // mirror teal
        bodyLeft.setColor(Color.rgb(100, 255, 218));
        bodyRight.setColor(Color.rgb(200, 255, 240));

        zonePaint.setColor(Color.argb(50, 0, 100, 255));
        zonePaint.setStyle(Paint.Style.FILL);
        zoneBorder.setColor(Color.argb(120, 0, 180, 255));
        zoneBorder.setStyle(Paint.Style.STROKE);
        zoneBorder.setStrokeWidth(3f);
        zoneBorder.setPathEffect(new android.graphics.DashPathEffect(new float[]{12f, 8f}, 0));

        // Create initial mirror zone (left third of screen)
        mirrorZones.add(new RectF(0, screenH * 0.2f, screenW * 0.4f, screenH * 0.85f));
    }

    @Override
    protected void updatePhase(float dt, Player player, ArenaManager arena,
                                TiltInput tiltInput, List<Projectile> projectiles) {
        projTimer -= dt;

        // Check if player is inside any mirror zone → invert tilt X
        boolean inMirror = false;
        for (RectF zone : mirrorZones) {
            if (zone.contains(player.getCenterX(), player.getCenterY())) {
                inMirror = true;
                break;
            }
        }
        tiltInput.setMirrorX(inMirror || flipActive);

        // Tick global flip
        if (flipActive) {
            flipTimer -= dt;
            if (flipTimer <= 0f) {
                flipActive = false;
                tiltInput.setMirrorX(false);
            }
        }

        // Move toward player
        float dx  = player.getCenterX() - getCenterX();
        velX = (dx > 0 ? 1f : -1f) * MOVE_SPEED;
        velX *= 0.85f;

        // Melee
        if (Math.abs(dx) < MELEE_RANGE) {
            // Handled by CollisionManager
        }

        // Ranged projectile
        if (projTimer <= 0f) {
            projectiles.add(new Projectile(
                    getCenterX(), getCenterY(),
                    player.getCenterX(), player.getCenterY(),
                    300f, Projectile.OWNER_ENEMY, 1, screenW, screenH
            ));
            projTimer = PROJ_INTERVAL;
        }

        // Phase 2: add second mirror zone
        if (currentPhase == 2 && mirrorZones.size() < 2) {
            mirrorZones.add(new RectF(screenW * 0.6f, screenH * 0.2f,
                    screenW, screenH * 0.85f));
        }
    }

    @Override
    protected void triggerSpecialAbility(ArenaManager arena, Player player,
                                          List<Projectile> projectiles) {
        // MIRROR FLIP: globally invert tilt for MIRROR_FLIP_DURATION seconds
        flipActive = true;
        flipTimer  = MIRROR_FLIP_DURATION;
    }

    @Override
    protected void checkPhaseTransition() {
        if (currentPhase == 1 && hp <= PHASE2_THRESHOLD) {
            currentPhase    = 2;
            specialInterval = 5.0f;
        }
    }

    @Override
    protected void drawBody(Canvas canvas) {
        // Draw mirror zones first (behind the boss)
        for (RectF zone : mirrorZones) {
            canvas.drawRect(zone, zonePaint);
            canvas.drawRect(zone, zoneBorder);
            // Label
            Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
            label.setColor(Color.argb(140, 0, 180, 255));
            label.setTextSize(22f);
            label.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("MIRROR ZONE", zone.centerX(), zone.top + 28f, label);
        }

        // Global flip indicator
        if (flipActive) {
            Paint flipOverlay = new Paint();
            flipOverlay.setColor(Color.argb((int)(30 + 20 * Math.sin(flipTimer * 8)), 0, 120, 255));
            canvas.drawRect(0, 0, screenW, screenH, flipOverlay);
        }

        float cx = x + width / 2f;

        // Left half body (slightly different colour from right — mirror effect)
        canvas.drawRect(x, y, cx, y + height, bodyLeft);
        canvas.drawRect(cx, y, x + width, y + height, bodyRight);

        // Vertical seam line in the middle
        Paint seamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        seamPaint.setColor(Color.rgb(150, 255, 240));
        seamPaint.setStrokeWidth(3f);
        canvas.drawLine(cx, y, cx, y + height, seamPaint);

        // Eyes (mirrored pair)
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.rgb(0, 200, 255));
        canvas.drawCircle(cx - 22f, y + 38f, 9f, eyePaint);
        canvas.drawCircle(cx + 22f, y + 38f, 9f, eyePaint);
    }

    /** Call from GameView on level end to ensure mirror mode is always cleaned up. */
    public void cleanupMirror(TiltInput tiltInput) {
        tiltInput.setMirrorX(false);
        tiltInput.setMirrorY(false);
    }

    @Override public String getBossName() { return "MIRROR BEAST"; }
}
