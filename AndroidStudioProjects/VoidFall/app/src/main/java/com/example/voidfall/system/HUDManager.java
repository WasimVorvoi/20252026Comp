package com.example.voidfall.system;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.entity.Boss;
import com.example.voidfall.entity.Player;
import com.example.voidfall.input.TiltInput;
import com.example.voidfall.level.ObjectiveManager;

/**
 * HUDManager — draws all heads-up display elements on top of the game.
 *
 * ELEMENTS DRAWN:
 *   1. HP Diamonds — three diamond shapes at the top-left.
 *      Filled = alive HP, empty = lost HP. Uses teal/grey colours.
 *
 *   2. Level indicator — "LEVEL X" top-right corner in small monospace font.
 *
 *   3. Objective bar — under the level indicator.
 *      Shows current objective description + a progress bar.
 *
 *   4. Fracture charge bar — small bar at the bottom of the screen.
 *      Shows how close the player is to triggering a tilt-fracture.
 *      Fills as the player tilts backward.
 *
 *   5. Tilt visualiser — a small crosshair showing current tiltX / tiltY.
 *      Useful for debugging and for the player to understand the mechanic.
 *
 *   6. Boss HP bar — when a boss is present, drawn at the very top-centre.
 *      Wide bar with boss name label above it.
 *
 * WHY DRAWN LAST:
 *   The HUD is drawn after all world elements so it always appears on top.
 *   Semi-transparent overlays and text must not be occluded by enemies.
 */
public class HUDManager {

    private final float screenW, screenH;

    // HP display
    private static final float HP_DIAMOND_SIZE = 28f;
    private static final float HP_MARGIN       = 14f;
    private static final float HP_TOP          = 52f;

    // Fracture charge bar
    private static final float CHARGE_BAR_W    = 200f;
    private static final float CHARGE_BAR_H    = 12f;
    private static final float CHARGE_BAR_BOT  = 60f; // distance from screen bottom

    // Tilt visualiser
    private static final float TILT_VIS_RADIUS = 28f;
    private static final float TILT_DOT_R      = 7f;

    // Boss HP bar
    private static final float BOSS_BAR_W      = 0.6f; // fraction of screen width
    private static final float BOSS_BAR_H      = 18f;
    private static final float BOSS_BAR_TOP    = 22f;

    // Paints
    private final Paint hpFillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hpEmptyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint objBarBg      = new Paint();
    private final Paint objBarFill    = new Paint();
    private final Paint chargeBg      = new Paint();
    private final Paint chargeFill    = new Paint();
    private final Paint tiltRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tiltDotPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bossBarBg     = new Paint();
    private final Paint bossBarFill   = new Paint();
    private final Paint bossNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public HUDManager(Context context, float screenW, float screenH) {
        this.screenW = screenW;
        this.screenH = screenH;

        hpFillPaint.setColor(Color.rgb(0, 255, 200));
        hpEmptyPaint.setColor(Color.rgb(50, 50, 60));

        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);

        objBarBg.setColor(Color.argb(150, 20, 20, 30));
        objBarFill.setColor(Color.rgb(0, 212, 255));

        chargeBg.setColor(Color.argb(120, 30, 30, 40));
        chargeFill.setColor(Color.rgb(0, 180, 255));

        tiltRingPaint.setStyle(Paint.Style.STROKE);
        tiltRingPaint.setStrokeWidth(2f);
        tiltRingPaint.setColor(Color.argb(120, 0, 212, 255));
        tiltDotPaint.setColor(Color.rgb(0, 212, 255));

        bossBarBg.setColor(Color.argb(180, 40, 0, 0));
        bossBarFill.setColor(Color.rgb(255, 45, 85));

        bossNamePaint.setTypeface(Typeface.MONOSPACE);
        bossNamePaint.setColor(Color.rgb(255, 150, 150));
        bossNamePaint.setTextAlign(Paint.Align.CENTER);
        bossNamePaint.setAntiAlias(true);
    }

    public void draw(Canvas canvas, Player player, int levelNumber,
                      ObjectiveManager objective, Boss boss, TiltInput tilt) {
        drawHpDiamonds(canvas, player);
        drawLevelIndicator(canvas, levelNumber);
        drawObjectiveBar(canvas, objective);
        drawFractureChargeBar(canvas, tilt);
        drawTiltVisualiser(canvas, tilt);
        if (boss != null && !boss.isDead()) {
            drawBossHpBar(canvas, boss);
        }
    }

    // ---- HP Diamonds ----

    private void drawHpDiamonds(Canvas canvas, Player player) {
        int maxHp = player.getMaxHp();
        int curHp = player.getHp();
        for (int i = 0; i < maxHp; i++) {
            float cx = HP_MARGIN + HP_DIAMOND_SIZE / 2f + i * (HP_DIAMOND_SIZE + 8f);
            float cy = HP_TOP;
            Paint p  = (i < curHp) ? hpFillPaint : hpEmptyPaint;
            float r  = HP_DIAMOND_SIZE / 2f;

            android.graphics.Path dp = new android.graphics.Path();
            dp.moveTo(cx,     cy - r);
            dp.lineTo(cx + r, cy);
            dp.lineTo(cx,     cy + r);
            dp.lineTo(cx - r, cy);
            dp.close();
            canvas.drawPath(dp, p);
        }
    }

    // ---- Level indicator ----

    private void drawLevelIndicator(Canvas canvas, int level) {
        textPaint.setTextSize(screenH * 0.022f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setColor(Color.argb(200, 200, 200, 220));
        canvas.drawText("LVL " + level, screenW - HP_MARGIN, HP_TOP, textPaint);
    }

    // ---- Objective bar ----

    private void drawObjectiveBar(Canvas canvas, ObjectiveManager obj) {
        if (obj == null) return;

        float barX = HP_MARGIN;
        float barY = HP_TOP + HP_DIAMOND_SIZE + 12f;
        float barW = screenW - HP_MARGIN * 2;
        float barH = screenH * 0.026f;

        // Background
        canvas.drawRoundRect(new RectF(barX, barY, barX + barW, barY + barH),
                4f, 4f, objBarBg);

        // Fill based on progress ratio
        int target   = Math.max(1, obj.getTarget());
        float ratio  = Math.min(1f, (float)obj.getProgress() / target);
        canvas.drawRoundRect(new RectF(barX, barY, barX + barW * ratio, barY + barH),
                4f, 4f, objBarFill);

        // Objective text
        textPaint.setTextSize(screenH * 0.020f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(obj.getObjectiveText(), barX + 6f, barY - 4f, textPaint);
    }

    // ---- Fracture charge bar ----

    private void drawFractureChargeBar(Canvas canvas, TiltInput tilt) {
        // This bar shows how close the backward-tilt is to triggering a fracture
        // We derive the charge level from the Y tilt value
        float tiltY = tilt.getTiltY();
        float charge = Math.max(0f, (-tiltY - 0.4f) / 0.6f); // maps [-0.4,-1] → [0, 1]
        charge = Math.min(1f, charge);

        float barX  = (screenW - CHARGE_BAR_W) / 2f;
        float barY  = screenH - CHARGE_BAR_BOT - CHARGE_BAR_H;

        canvas.drawRoundRect(new RectF(barX, barY, barX + CHARGE_BAR_W, barY + CHARGE_BAR_H),
                3f, 3f, chargeBg);
        if (charge > 0) {
            chargeFill.setColor(charge > 0.8f
                    ? Color.rgb(255, 100, 0) : Color.rgb(0, 180, 255));
            canvas.drawRoundRect(new RectF(barX, barY,
                    barX + CHARGE_BAR_W * charge, barY + CHARGE_BAR_H),
                    3f, 3f, chargeFill);
        }

        // Label
        textPaint.setTextSize(screenH * 0.016f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.argb(160, 180, 220, 255));
        canvas.drawText("FRACTURE CHARGE", screenW / 2f,
                barY - 4f, textPaint);
    }

    // ---- Tilt visualiser (small crosshair) ----

    private void drawTiltVisualiser(Canvas canvas, TiltInput tilt) {
        float cx  = screenW - TILT_VIS_RADIUS - 18f;
        float cy  = screenH - TILT_VIS_RADIUS - CHARGE_BAR_BOT - 20f;
        float tx  = tilt.getTiltX() * TILT_VIS_RADIUS;
        float ty  = tilt.getTiltY() * TILT_VIS_RADIUS;

        // Ring background
        canvas.drawCircle(cx, cy, TILT_VIS_RADIUS, tiltRingPaint);
        // Crosshair lines
        canvas.drawLine(cx - TILT_VIS_RADIUS, cy, cx + TILT_VIS_RADIUS, cy, tiltRingPaint);
        canvas.drawLine(cx, cy - TILT_VIS_RADIUS, cx, cy + TILT_VIS_RADIUS, tiltRingPaint);
        // Dot position representing current tilt
        tiltDotPaint.setAlpha(200);
        canvas.drawCircle(cx + tx, cy + ty, TILT_DOT_R, tiltDotPaint);
    }

    // ---- Boss HP bar ----

    private void drawBossHpBar(Canvas canvas, Boss boss) {
        float barW  = screenW * BOSS_BAR_W;
        float barX  = (screenW - barW) / 2f;
        float barH  = BOSS_BAR_H;
        float barY  = BOSS_BAR_TOP;

        // Background
        canvas.drawRoundRect(new RectF(barX, barY, barX + barW, barY + barH),
                4f, 4f, bossBarBg);

        // Fill
        float ratio = (float) boss.getHp() / boss.getMaxHp();
        bossBarFill.setColor(ratio > 0.5f ? Color.rgb(255, 45, 85)
                           : ratio > 0.25f ? Color.rgb(255, 150, 0)
                           : Color.rgb(255, 80, 0));
        canvas.drawRoundRect(new RectF(barX, barY, barX + barW * ratio, barY + barH),
                4f, 4f, bossBarFill);

        // Boss name above bar
        bossNamePaint.setTextSize(screenH * 0.022f);
        canvas.drawText(boss.getBossName(), screenW / 2f, barY - 4f, bossNamePaint);

        // Phase indicator
        if (boss.getCurrentPhase() > 1) {
            bossNamePaint.setTextSize(screenH * 0.016f);
            bossNamePaint.setColor(Color.rgb(255, 200, 100));
            canvas.drawText("PHASE " + boss.getCurrentPhase(),
                    screenW / 2f, barY + barH + 16f, bossNamePaint);
            bossNamePaint.setColor(Color.rgb(255, 150, 150)); // reset for next frame
        }
    }
}
