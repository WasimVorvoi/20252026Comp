package com.example.voidfall.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.BlockEcho;
import com.example.voidfall.echo.DashEcho;
import com.example.voidfall.echo.EchoShadow;
import com.example.voidfall.echo.ProjectileEcho;
import com.example.voidfall.input.TiltInput;

import java.util.List;

/**
 * EchoTitanBoss — Level 9 final boss. Activates all echo systems simultaneously.
 *
 * DESIGN PHILOSOPHY:
 *   The final boss is a "greatest hits" fight.
 *   The player has encountered every echo type across levels 4-8.
 *   Echo Titan combines ALL of them at once while also being a massive direct threat.
 *   Surviving requires mastery of the whole game.
 *
 * PHASES:
 *   Phase 1 (HP > 33): Spawns DashEcho replays around itself. Heavy melee.
 *   Phase 2 (HP 33-16): Adds ProjectileEcho bursts. Fractures arena heavily.
 *   Phase 3 (HP <= 16): ALL echo types. Moves fastest. Arena nearly fully fractured.
 *
 * SPECIAL ABILITY — "ECHO BURST":
 *   Fires 8 projectiles in cardinal + diagonal directions simultaneously.
 *   In Phase 3: also triggers 2 fractures.
 *
 * VISUAL:
 *   Massive figure assembled from layered fragments (echoes made physical).
 *   Deep violet with translucent echo-layer outlines around it.
 *   In Phase 3: rapidly flickering between solid and ghost appearance.
 */
public class EchoTitanBoss extends Boss {

    private static final int   PHASE2_THRESHOLD = 33;
    private static final int   PHASE3_THRESHOLD = 16;
    private static final float MOVE_SPEED_P1    = 85f;
    private static final float MOVE_SPEED_P2    = 110f;
    private static final float MOVE_SPEED_P3    = 160f;
    private static final float MELEE_RANGE      = 95f;
    private static final float ECHO_SPAWN_INTERVAL = 4.0f;

    private float echoSpawnTimer = 0f;
    // Phase 3 flicker
    private float flickerTimer = 0f;
    // Echo shadow list (passed by GameView via update signature — we store a ref)
    private List<EchoShadow> echoShadows;

    private final Paint fragmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  bodyPath      = new Path();

    public EchoTitanBoss(float x, float y, float screenW, float screenH) {
        super(x, y, 115f, 125f, 50, screenW, screenH);
        bodyPaint.setColor(Color.rgb(124, 77, 255));    // deep violet
        fragmentPaint.setColor(Color.argb(80, 124, 77, 255));
        fragmentPaint.setStyle(Paint.Style.STROKE);
        fragmentPaint.setStrokeWidth(2f);
        glowPaint.setColor(Color.argb(60, 200, 100, 255));
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(5f);
        specialInterval = 4.5f;
    }

    /** Called from GameView so we can spawn echoes into the main list. */
    public void setEchoList(List<EchoShadow> echoes) {
        this.echoShadows = echoes;
    }

    @Override
    protected void updatePhase(float dt, Player player, ArenaManager arena,
                                TiltInput tiltInput, List<Projectile> projectiles) {
        echoSpawnTimer += dt;
        flickerTimer   += dt;

        float speed = (currentPhase == 3) ? MOVE_SPEED_P3
                    : (currentPhase == 2) ? MOVE_SPEED_P2
                    : MOVE_SPEED_P1;

        // Move toward player
        float dx = player.getCenterX() - getCenterX();
        velX     = (dx > 0 ? 1f : -1f) * speed;
        velX    *= 0.88f;

        // Melee
        if (Math.abs(dx) < MELEE_RANGE) {
            // CollisionManager handles actual damage
        }

        // Phase-based echo spawning
        if (echoShadows != null && echoSpawnTimer >= ECHO_SPAWN_INTERVAL) {
            echoSpawnTimer = 0f;
            spawnPhaseEchoes(player);
        }

        // Phase 2+: fracture arena periodically
        if (currentPhase >= 2 && specialTimer > specialInterval * 0.5f) {
            arena.triggerFracture(1);
        }
    }

    private void spawnPhaseEchoes(Player player) {
        float cx = getCenterX();
        float cy = getCenterY();

        // Phase 1: spawn dash echoes around self
        echoShadows.add(new DashEcho(cx - 80f, cy, 1f));
        echoShadows.add(new DashEcho(cx + 80f, cy, -1f));

        if (currentPhase >= 2) {
            // Phase 2+: projectile echoes
            echoShadows.add(new ProjectileEcho(cx, cy - 60f));
        }

        if (currentPhase == 3) {
            // Phase 3: block echoes in player's path
            echoShadows.add(new BlockEcho(player.getX(), player.getY(), 60f, 60f));
        }
    }

    @Override
    protected void triggerSpecialAbility(ArenaManager arena, Player player,
                                          List<Projectile> projectiles) {
        // ECHO BURST: 8-directional projectile volley
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI / 4) * i;
            float  dx    = (float)Math.cos(angle);
            float  dy    = (float)Math.sin(angle);
            projectiles.add(Projectile.directional(
                    getCenterX(), getCenterY(), dx, dy,
                    380f, Projectile.OWNER_ENEMY, 1, screenW, screenH
            ));
        }
        // Phase 3: also fracture
        if (currentPhase == 3) arena.triggerFracture(2);
    }

    @Override
    protected void checkPhaseTransition() {
        if (currentPhase == 1 && hp <= PHASE2_THRESHOLD) {
            currentPhase    = 2;
            specialInterval = 3.5f;
        } else if (currentPhase == 2 && hp <= PHASE3_THRESHOLD) {
            currentPhase    = 3;
            specialInterval = 2.5f;
        }
    }

    @Override
    protected void drawBody(Canvas canvas) {
        float cx = x + width / 2f;
        float cy = y + height / 2f;

        // Phase 3 flicker: rapidly change alpha
        int bodyAlpha = 255;
        if (currentPhase == 3) {
            bodyAlpha = (int)(150 + 105 * Math.sin(flickerTimer * 15));
        }
        bodyPaint.setAlpha(bodyAlpha);

        // Outer ghost layers (layered transparent rectangles offset slightly)
        for (int i = 1; i <= 3; i++) {
            float offset = i * 6f;
            float fade   = 255 / (i * 2f + 1);
            fragmentPaint.setAlpha((int)fade);
            canvas.drawRect(x - offset, y - offset,
                    x + width + offset, y + height + offset, fragmentPaint);
        }

        // Main body: irregular polygon to suggest a fractured form
        bodyPath.reset();
        bodyPath.moveTo(cx,         y);
        bodyPath.lineTo(x + width,  y + height * 0.3f);
        bodyPath.lineTo(x + width - 15f, y + height);
        bodyPath.lineTo(x + 15f,    y + height);
        bodyPath.lineTo(x,          y + height * 0.3f);
        bodyPath.close();
        canvas.drawPath(bodyPath, bodyPaint);

        // Pulsing glow ring
        float glow = 30f + 20f * (float)Math.sin(System.currentTimeMillis() * 0.003);
        glowPaint.setAlpha(currentPhase == 3 ? 120 : 60);
        canvas.drawCircle(cx, cy, glow + width / 2f, glowPaint);

        // Eyes
        bodyPaint.setAlpha(255);
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.rgb(230, 130, 255));
        canvas.drawCircle(cx - 25f, y + 40f, 10f, eyePaint);
        canvas.drawCircle(cx + 25f, y + 40f, 10f, eyePaint);
        eyePaint.setColor(Color.rgb(255, 200, 255));
        canvas.drawCircle(cx - 25f, y + 40f, 4f, eyePaint);
        canvas.drawCircle(cx + 25f, y + 40f, 4f, eyePaint);
    }

    @Override public String getBossName() { return "ECHO TITAN"; }
}
