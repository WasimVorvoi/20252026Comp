package com.example.voidfall.level;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.arena.FracturePiece;
import com.example.voidfall.echo.EchoShadow;
import com.example.voidfall.entity.Boss;
import com.example.voidfall.entity.Enemy;
import com.example.voidfall.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * ObjectiveManager — tracks the current level's objective and determines completion.
 *
 * HOW IT WORKS:
 *   1. loadObjective() is called when a level loads. It reads the LevelConfig
 *      and creates the objective's game objects (shards, seals, core).
 *   2. update() is called every frame to check progress.
 *   3. isComplete() returns true when the objective condition is met.
 *   4. draw() renders objective items (shards, seals, core) on the arena.
 *
 * OBJECTIVE TYPES (one per level, no repetition):
 *
 *   DEFEAT_ALL:
 *     Complete when all enemies and the boss (if any) are dead.
 *     Simple, clean, tests basic combat.
 *
 *   COLLECT_SHARDS:
 *     N Void Shards are placed on top of FracturePiece surfaces.
 *     The shard moves WITH its parent piece (stays on top of it).
 *     Player collects by walking over it.
 *     Tests arena navigation.
 *
 *   PROTECT_CORE:
 *     A Core object is placed at the arena centre. It has 3 HP.
 *     HeavyEnemies and RangedEnemies target it when they can't reach the player.
 *     Complete when all enemies are dead. Fail if core HP drops to 0
 *     (treated as game-over-like state — player must restart the level).
 *
 *   ACTIVATE_SEALS:
 *     4 numbered seals placed on different arena pieces.
 *     Player must step on them in order 1→2→3→4.
 *     Stepping on the wrong number resets progress to 0.
 *     Tests multi-platform navigation and spatial memory.
 *
 *   SURVIVE_STORM (Level 8):
 *     3 Echo Generators are placed on the arena.
 *     Each generator spawns echoes periodically.
 *     Complete by destroying all 3 generators.
 *     Tests applying combat knowledge in a chaotic environment.
 */
public class ObjectiveManager {

    private LevelConfig config;
    private boolean     complete   = false;
    private int         progress   = 0; // enemies killed / shards collected / seals activated
    private float       stormTimer = 0f;

    // Objective game objects
    private final List<ShardItem>     shards     = new ArrayList<>();
    private final List<SealItem>      seals      = new ArrayList<>();
    private CoreItem                  core       = null;
    private final List<GeneratorItem> generators = new ArrayList<>();

    private int nextSealToActivate = 1; // for ACTIVATE_SEALS

    // Paints
    private final Paint shardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sealPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint genPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ObjectiveManager() {
        shardPaint.setColor(Color.rgb(0, 255, 200));
        sealPaint.setColor(Color.rgb(255, 200, 0));
        corePaint.setColor(Color.rgb(100, 180, 255));
        genPaint.setColor(Color.rgb(255, 80, 80));
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(22f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setAntiAlias(true);
    }

    // =========================================================================
    // Load
    // =========================================================================

    public void loadObjective(LevelConfig cfg, ArenaManager arena,
                               float screenW, float screenH) {
        this.config   = cfg;
        this.complete = false;
        this.progress = 0;
        this.nextSealToActivate = 1;

        shards.clear();
        seals.clear();
        generators.clear();
        core = null;

        List<FracturePiece> pieces = arena.getActivePieces();
        if (pieces.isEmpty()) return;

        switch (cfg.objectiveType) {
            case LevelConfig.OBJ_COLLECT_SHARDS:
                placeShards(cfg.targetCount, pieces);
                break;

            case LevelConfig.OBJ_PROTECT_CORE:
                float cx = screenW / 2f;
                float cy = screenH * 0.55f;
                core = new CoreItem(cx - 25f, cy - 25f, 50f, 50f);
                break;

            case LevelConfig.OBJ_ACTIVATE_SEALS:
                placeSeals(cfg.targetCount, pieces);
                break;

            case LevelConfig.OBJ_SURVIVE_STORM:
                placeGenerators(cfg.targetCount, pieces, screenW);
                break;
        }
    }

    private void placeShards(int count, List<FracturePiece> pieces) {
        int placed = 0;
        for (int i = 0; i < pieces.size() && placed < count; i++) {
            // Skip every other piece so shards are spread out
            if (i % 2 != 0) continue;
            FracturePiece p = pieces.get(i);
            float sx = p.getCenterX() - 15f;
            float sy = p.getTop()     - 35f;
            shards.add(new ShardItem(sx, sy, p));
            placed++;
        }
    }

    private void placeSeals(int count, List<FracturePiece> pieces) {
        // Place seals on spread-out pieces to require movement
        int step = Math.max(1, pieces.size() / count);
        for (int i = 0; i < count && i * step < pieces.size(); i++) {
            FracturePiece p  = pieces.get(i * step);
            float sx = p.getCenterX() - 20f;
            float sy = p.getTop()     - 45f;
            seals.add(new SealItem(sx, sy, i + 1, p));
        }
    }

    private void placeGenerators(int count, List<FracturePiece> pieces, float screenW) {
        float spacing = screenW / (count + 1f);
        for (int i = 0; i < count; i++) {
            float gx = spacing * (i + 1) - 20f;
            // Find the topmost piece near this x position
            float gy = 400f;
            for (FracturePiece p : pieces) {
                if (Math.abs(p.getCenterX() - gx) < p.getWidth() / 2f) {
                    gy = p.getTop() - 45f;
                    break;
                }
            }
            generators.add(new GeneratorItem(gx, gy, 40f, 45f));
        }
    }

    // =========================================================================
    // Update
    // =========================================================================

    public void update(float dt, Player player, List<Enemy> enemies,
                        Boss boss, ArenaManager arena, List<EchoShadow> echoShadows) {
        if (complete || config == null) return;

        switch (config.objectiveType) {
            case LevelConfig.OBJ_DEFEAT_ALL:
                checkDefeatAll(enemies, boss);
                break;

            case LevelConfig.OBJ_COLLECT_SHARDS:
                updateShards(player);
                break;

            case LevelConfig.OBJ_PROTECT_CORE:
                updateCore(dt, player, enemies, arena);
                break;

            case LevelConfig.OBJ_ACTIVATE_SEALS:
                updateSeals(player, enemies);
                break;

            case LevelConfig.OBJ_SURVIVE_STORM:
                updateStorm(dt, player, echoShadows);
                break;
        }
    }

    private void checkDefeatAll(List<Enemy> enemies, Boss boss) {
        boolean allEnemiesDead = enemies.isEmpty();
        boolean bossDead       = (config.isBossLevel) ? (boss == null || boss.isDead()) : true;
        if (allEnemiesDead && bossDead) complete = true;
    }

    private void updateShards(Player player) {
        RectF pBounds = player.getBounds();
        for (ShardItem s : shards) {
            if (!s.collected) {
                // Move shard with its parent piece
                s.syncWithPiece();
                // Check player overlap
                if (RectF.intersects(pBounds, s.getBounds())) {
                    s.collected = true;
                    progress++;
                }
            }
        }
        if (progress >= config.targetCount) complete = true;
    }

    private void updateCore(float dt, Player player, List<Enemy> enemies, ArenaManager arena) {
        if (core == null) return;
        // Enemies target the core when close
        for (Enemy e : enemies) {
            if (!e.isDead()) {
                RectF coreBounds = core.getBounds();
                RectF eBounds    = e.getBounds();
                if (RectF.intersects(coreBounds, eBounds)) {
                    core.hp -= dt * 1.5f; // damage over time on contact
                }
            }
        }
        if (core.hp <= 0) complete = true; // core destroyed = level fail (handled in GameView too)
        if (enemies.isEmpty()) complete = true; // all enemies defeated = win
    }

    private void updateSeals(Player player, List<Enemy> enemies) {
        RectF pBounds = player.getBounds();
        for (SealItem seal : seals) {
            if (!seal.activated) {
                seal.syncWithPiece();
                if (RectF.intersects(pBounds, seal.getBounds())) {
                    if (seal.number == nextSealToActivate) {
                        seal.activated = true;
                        nextSealToActivate++;
                        progress++;
                    } else if (seal.number != nextSealToActivate) {
                        // Wrong order — reset
                        for (SealItem s : seals) s.activated = false;
                        nextSealToActivate = 1;
                        progress           = 0;
                    }
                }
            }
        }
        if (progress >= config.targetCount && enemies.isEmpty()) complete = true;
    }

    private void updateStorm(float dt, Player player, List<EchoShadow> echoShadows) {
        // Count generators still alive
        int alive = 0;
        for (GeneratorItem g : generators) {
            if (!g.destroyed) {
                alive++;
                // Check if player touches generator to destroy it
                if (RectF.intersects(player.getBounds(), g.getBounds())) {
                    g.destroyed = true;
                    progress++;
                }
            }
        }
        if (alive == 0) complete = true;
    }

    // =========================================================================
    // Draw
    // =========================================================================

    public void draw(Canvas canvas) {
        if (config == null) return;

        for (ShardItem s : shards) {
            if (!s.collected) s.draw(canvas, shardPaint, labelPaint);
        }
        for (SealItem seal : seals) {
            if (!seal.activated) seal.draw(canvas, sealPaint, labelPaint);
        }
        if (core != null && core.hp > 0) core.draw(canvas, corePaint, labelPaint);
        for (GeneratorItem g : generators) {
            if (!g.destroyed) g.draw(canvas, genPaint, labelPaint);
        }
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public boolean isComplete()    { return complete; }
    public int     getProgress()   { return progress; }
    public int     getTarget()     { return config != null ? config.targetCount : 1; }
    public String  getObjectiveText() {
        if (config == null) return "";
        switch (config.objectiveType) {
            case LevelConfig.OBJ_DEFEAT_ALL:
                return "Enemies remaining";
            case LevelConfig.OBJ_COLLECT_SHARDS:
                return "Shards: " + progress + "/" + config.targetCount;
            case LevelConfig.OBJ_PROTECT_CORE:
                return core != null ? "Core HP: " + (int)Math.ceil(core.hp) + "/3" : "";
            case LevelConfig.OBJ_ACTIVATE_SEALS:
                return "Seals: " + progress + "/" + config.targetCount + " — next: " + nextSealToActivate;
            case LevelConfig.OBJ_SURVIVE_STORM:
                return "Generators left: " + (config.targetCount - progress);
            default:
                return "";
        }
    }

    public CoreItem getCore() { return core; }

    // =========================================================================
    // Inner data classes
    // =========================================================================

    public static class ShardItem {
        float x, y;
        boolean collected = false;
        final FracturePiece parentPiece;
        float offsetOnPiece; // horizontal offset from piece center

        ShardItem(float x, float y, FracturePiece piece) {
            this.x = x; this.y = y;
            this.parentPiece = piece;
            this.offsetOnPiece = x - piece.getCenterX();
        }

        void syncWithPiece() {
            x = parentPiece.getCenterX() + offsetOnPiece - 15f;
            y = parentPiece.getTop() - 35f;
        }

        RectF getBounds() { return new RectF(x, y, x + 30f, y + 30f); }

        void draw(Canvas canvas, Paint paint, Paint label) {
            float pulse = (float)(0.8 + 0.2 * Math.sin(System.currentTimeMillis() * 0.004));
            paint.setAlpha(200);
            // Draw a small diamond
            float cx = x + 15f, cy = y + 15f, r = 12f * pulse;
            android.graphics.Path p = new android.graphics.Path();
            p.moveTo(cx, cy - r); p.lineTo(cx + r, cy);
            p.lineTo(cx, cy + r); p.lineTo(cx - r, cy); p.close();
            canvas.drawPath(p, paint);
            label.setTextSize(18f);
            canvas.drawText("SHARD", cx, cy - r - 6f, label);
        }
    }

    public static class SealItem {
        float x, y;
        boolean activated = false;
        int number;
        final FracturePiece parentPiece;
        float offsetOnPiece;

        SealItem(float x, float y, int number, FracturePiece piece) {
            this.x = x; this.y = y; this.number = number;
            this.parentPiece = piece;
            this.offsetOnPiece = x - piece.getCenterX();
        }

        void syncWithPiece() {
            x = parentPiece.getCenterX() + offsetOnPiece - 20f;
            y = parentPiece.getTop() - 45f;
        }

        RectF getBounds() { return new RectF(x, y, x + 40f, y + 40f); }

        void draw(Canvas canvas, Paint paint, Paint label) {
            paint.setAlpha(200);
            float cx = x + 20f, cy = y + 20f;
            canvas.drawCircle(cx, cy, 20f, paint);
            label.setTextSize(24f);
            label.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(number), cx, cy + 8f, label);
            label.setColor(Color.WHITE);
            label.setTextSize(16f);
            canvas.drawText("SEAL", cx, y - 8f, label);
        }
    }

    public static class CoreItem {
        float x, y, w, h;
        float hp = 3f;

        CoreItem(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        RectF getBounds() { return new RectF(x, y, x + w, y + h); }

        void draw(Canvas canvas, Paint paint, Paint label) {
            float pulse = (float)(0.9 + 0.1 * Math.sin(System.currentTimeMillis() * 0.003));
            paint.setAlpha(200);
            float pw = w * pulse, ph = h * pulse;
            float px = x + (w - pw) / 2f, py = y + (h - ph) / 2f;
            canvas.drawRoundRect(new RectF(px, py, px + pw, py + ph), 10f, 10f, paint);
            // HP pip dots below
            for (int i = 0; i < 3; i++) {
                Paint pip = new Paint(Paint.ANTI_ALIAS_FLAG);
                pip.setColor(i < (int)Math.ceil(hp) ? Color.rgb(100, 180, 255) : Color.DKGRAY);
                canvas.drawCircle(x + 10f + i * 16f, y + h + 10f, 6f, pip);
            }
            label.setTextSize(18f);
            label.setColor(Color.WHITE);
            label.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("CORE", x + w / 2f, y - 8f, label);
        }
    }

    public static class GeneratorItem {
        float x, y, w, h;
        boolean destroyed = false;

        GeneratorItem(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        RectF getBounds() { return new RectF(x, y, x + w, y + h); }

        void draw(Canvas canvas, Paint paint, Paint label) {
            paint.setAlpha(220);
            canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 6f, 6f, paint);
            // Pulsing inner glow
            Paint inner = new Paint(Paint.ANTI_ALIAS_FLAG);
            inner.setColor(Color.argb(100, 255, 140, 0));
            float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 0.006));
            canvas.drawCircle(x + w / 2f, y + h / 2f, w * 0.3f * (float)pulse + 5f, inner);
            label.setTextSize(16f);
            label.setColor(Color.WHITE);
            label.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("GEN", x + w / 2f, y - 6f, label);
        }
    }
}
