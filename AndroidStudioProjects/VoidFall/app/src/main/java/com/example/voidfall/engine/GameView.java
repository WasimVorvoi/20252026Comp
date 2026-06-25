package com.example.voidfall.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.BlockEcho;
import com.example.voidfall.echo.DashEcho;
import com.example.voidfall.echo.EchoShadow;
import com.example.voidfall.echo.ProjectileEcho;
import com.example.voidfall.entity.Boss;
import com.example.voidfall.entity.Enemy;
import com.example.voidfall.entity.EchoTitanBoss;
import com.example.voidfall.entity.MirrorBeastBoss;
import com.example.voidfall.entity.Player;
import com.example.voidfall.entity.Projectile;
import com.example.voidfall.entity.ShatterKingBoss;
import com.example.voidfall.input.TiltInput;
import com.example.voidfall.level.LevelConfig;
import com.example.voidfall.level.LevelManager;
import com.example.voidfall.level.ObjectiveManager;
import com.example.voidfall.system.CollisionManager;
import com.example.voidfall.system.HUDManager;
import com.example.voidfall.system.SoundManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GameView — the central SurfaceView that owns and coordinates every game system.
 *
 * WHY SURFACEVIEW:
 *   A regular View can only draw on the UI thread via onDraw().
 *   SurfaceView has its own dedicated drawing surface that any thread can lock.
 *   Our GameThread calls draw() from a background thread, which locks the
 *   SurfaceHolder canvas, paints everything, then unlocks and posts.
 *   This lets the game loop run independently of the UI thread.
 *
 * OWNERSHIP MODEL:
 *   GameView creates and holds every system: arena, player, enemies, bosses,
 *   echoes, level manager, objective manager, collision, sound, HUD.
 *   The GameThread only calls update(dt) and draw() — it knows nothing else.
 *
 * GAME STATES:
 *   MENU         → title screen
 *   LEVEL_INTRO  → shows level name + objective for 3 seconds
 *   PLAYING      → active gameplay
 *   LEVEL_COMPLETE → "LEVEL COMPLETE" pause, tap to continue
 *   BOSS_INTRO   → dramatic boss entry warning
 *   GAME_OVER    → player ran out of HP, tap to restart
 *   WIN          → final boss defeated
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    // ---- Game state constants ----
    public static final int STATE_MENU           = 0;
    public static final int STATE_LEVEL_INTRO    = 1;
    public static final int STATE_PLAYING        = 2;
    public static final int STATE_LEVEL_COMPLETE = 3;
    public static final int STATE_BOSS_INTRO     = 4;
    public static final int STATE_GAME_OVER      = 5;
    public static final int STATE_WIN            = 6;

    private int gameState = STATE_MENU;

    // ---- Engine ----
    private GameThread gameThread;
    private final TiltInput tiltInput;

    // ---- Game systems ----
    private ArenaManager     arenaManager;
    private Player           player;
    private List<Enemy>      enemies;
    private List<Projectile> projectiles;
    private List<EchoShadow> echoShadows;
    private Boss             currentBoss;
    private LevelManager     levelManager;
    private ObjectiveManager objectiveManager;
    private CollisionManager collisionManager;
    private SoundManager     soundManager;
    private HUDManager       hudManager;

    // ---- Screen dimensions (set in surfaceCreated) ----
    private int screenW = 0;
    private int screenH = 0;

    // ---- State timers ----
    private float stateTimer    = 0f;   // general-purpose timer for timed states
    private float introTimer    = 3.0f; // how long to show level intro
    private float bossIntroTimer= 3.5f;

    // ---- Reusable paint for overlays ----
    private final Paint overlayPaint  = new Paint();
    private final Paint textPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---- Screen shake ----
    private float shakeOffsetX = 0f;
    private float shakeOffsetY = 0f;
    private float shakeTimer   = 0f;
    private float shakeMagnitude = 0f;

    public GameView(Context context, TiltInput tiltInput, SoundManager soundManager) {
        super(context);
        this.tiltInput   = tiltInput;
        this.soundManager = soundManager;

        getHolder().addCallback(this);
        setFocusable(true); // so we can receive touch events

        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setColor(Color.WHITE);
    }

    // =========================================================================
    // SurfaceHolder.Callback — surface lifecycle
    // =========================================================================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = getWidth();
        screenH = getHeight();

        initSystems();

        // Start the game thread only if it is not already running.
        if (gameThread == null || !gameThread.isRunning()) {
            gameThread = new GameThread(this, holder);
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenW = width;
        screenH = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Ask the thread to stop, then wait for it to finish cleanly.
        // This prevents the surface from being destroyed while a draw is in progress.
        if (gameThread != null) {
            gameThread.stopLoop();
            boolean joined = false;
            while (!joined) {
                try {
                    gameThread.join();
                    joined = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    /** Create and wire up all game systems. Called once per surface creation. */
    private void initSystems() {
        arenaManager     = new ArenaManager(screenW, screenH);
        player           = new Player(screenW / 2f, screenH * 0.4f, screenW, screenH);
        enemies          = new ArrayList<>();
        projectiles      = new ArrayList<>();
        echoShadows      = new ArrayList<>();
        currentBoss      = null;
        levelManager     = new LevelManager();
        objectiveManager = new ObjectiveManager();
        collisionManager = new CollisionManager();
        hudManager       = new HUDManager(getContext(), screenW, screenH);
    }

    /**
     * Load a specific level: configure arena, spawn enemies, set objective.
     * Called when transitioning from LEVEL_INTRO → PLAYING, or from LEVEL_COMPLETE.
     */
    private void loadLevel(int levelNumber) {
        LevelConfig cfg = levelManager.getConfig(levelNumber);

        // Clear previous level's entities
        enemies.clear();
        projectiles.clear();
        echoShadows.clear();
        currentBoss = null;

        // Reset player position and HP (HP only resets at boss levels or game start)
        player.setPosition(screenW / 2f, screenH * 0.4f);
        if (levelNumber == 1) player.resetHP();

        // Reset arena and apply this level's fracture settings
        arenaManager.reset(screenW, screenH);
        arenaManager.setFractureLevel(cfg.initialFractureCount);

        // Spawn enemies per config
        levelManager.spawnEnemies(cfg, enemies, screenW, screenH);

        // Spawn boss if this is a boss level
        if (cfg.isBossLevel) {
            currentBoss = levelManager.createBoss(cfg.bossType, screenW, screenH);
            soundManager.playBossWarning();
        }

        // Configure objective
        objectiveManager.loadObjective(cfg, arenaManager, screenW, screenH);

        // Configure tilt mirror zones for Mirror Beast level
        tiltInput.setMirrorX(false);
        tiltInput.setMirrorY(false);
    }

    // =========================================================================
    // UPDATE — called by GameThread every frame
    // =========================================================================

    /** Called by GameThread. All game logic runs here. */
    public void update(float dt) {
        float tiltX = tiltInput.getTiltX();
        float tiltY = tiltInput.getTiltY();

        switch (gameState) {
            case STATE_MENU:
                // Nothing to update; waiting for tap.
                break;

            case STATE_LEVEL_INTRO:
                stateTimer += dt;
                if (stateTimer >= introTimer) {
                    stateTimer = 0f;
                    loadLevel(levelManager.getCurrentLevel());
                    gameState = STATE_PLAYING;
                    soundManager.playGameplayMusic();
                }
                break;

            case STATE_BOSS_INTRO:
                stateTimer += dt;
                if (stateTimer >= bossIntroTimer) {
                    stateTimer = 0f;
                    loadLevel(levelManager.getCurrentLevel());
                    gameState = STATE_PLAYING;
                    soundManager.playBossMusic();
                }
                break;

            case STATE_PLAYING:
                updatePlaying(dt, tiltX, tiltY);
                break;

            case STATE_LEVEL_COMPLETE:
                stateTimer += dt;
                // Auto-advance after 2 seconds; player can also tap.
                if (stateTimer >= 2.0f) {
                    advanceToNextLevel();
                }
                break;

            case STATE_GAME_OVER:
            case STATE_WIN:
                // Waiting for tap to restart / return to menu.
                break;
        }

        // Screen shake decays regardless of state
        updateScreenShake(dt);
    }

    /** Full gameplay update: arena, player, enemies, boss, echoes, collisions, objective. */
    private void updatePlaying(float dt, float tiltX, float tiltY) {
        // 1. Update arena — tilt stretches/fractures arena
        arenaManager.update(dt, tiltX, tiltY);

        // 2. Update player
        player.update(dt, tiltX, tiltY, arenaManager, projectiles);

        // 3. Update enemies
        for (Enemy e : enemies) {
            e.update(dt, player, arenaManager, projectiles);
        }

        // 4. Update boss
        if (currentBoss != null && !currentBoss.isDead()) {
            currentBoss.update(dt, player, arenaManager, tiltInput, projectiles);
        }

        // 5. Update projectiles
        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.update(dt);
            if (!p.isActive()) pit.remove();
        }

        // 6. Update echo shadows
        Iterator<EchoShadow> eit = echoShadows.iterator();
        while (eit.hasNext()) {
            EchoShadow es = eit.next();
            es.update(dt, player);
            if (!es.isActive()) eit.remove();
        }

        // 7. Resolve collisions
        collisionManager.resolveAll(player, enemies, currentBoss,
                projectiles, echoShadows, arenaManager, soundManager);

        // 8. Collect dead enemies → spawn echo shadows
        Iterator<Enemy> eIter = enemies.iterator();
        while (eIter.hasNext()) {
            Enemy e = eIter.next();
            if (e.isDead()) {
                soundManager.playEnemyDefeat();
                // Spawn an echo shadow at the enemy's death position
                EchoShadow echo = createEcho(e);
                if (echo != null) echoShadows.add(echo);
                eIter.remove();
            }
        }

        // 9. Check if boss is dead
        if (currentBoss != null && currentBoss.isDead()) {
            soundManager.playEnemyDefeat();
            currentBoss = null;
        }

        // 10. Check screen shake trigger from arena fractures
        if (arenaManager.consumeFractureEvent()) {
            triggerScreenShake(18f, 0.35f);
            soundManager.playFracture();
        }

        // 11. Check objective completion
        objectiveManager.update(dt, player, enemies, currentBoss, arenaManager, echoShadows);
        if (objectiveManager.isComplete()) {
            gameState  = STATE_LEVEL_COMPLETE;
            stateTimer = 0f;
            soundManager.stopMusic();
            soundManager.playLevelComplete();
        }

        // 12. Check player death
        if (player.isDead()) {
            gameState = STATE_GAME_OVER;
            soundManager.stopMusic();
        }
    }

    /** Transition to the next level or show victory screen. */
    private void advanceToNextLevel() {
        stateTimer = 0f;
        boolean hasNext = levelManager.advance();
        if (!hasNext) {
            gameState = STATE_WIN;
            return;
        }
        LevelConfig next = levelManager.getConfig(levelManager.getCurrentLevel());
        if (next.isBossLevel) {
            gameState = STATE_BOSS_INTRO;
        } else {
            gameState = STATE_LEVEL_INTRO;
        }
    }

    // =========================================================================
    // SCREEN SHAKE
    // =========================================================================

    public void triggerScreenShake(float magnitude, float duration) {
        shakeMagnitude = magnitude;
        shakeTimer     = duration;
    }

    private void updateScreenShake(float dt) {
        if (shakeTimer > 0f) {
            shakeTimer -= dt;
            float decay  = shakeTimer / 0.35f;
            float range  = shakeMagnitude * Math.max(0f, decay);
            shakeOffsetX = (float)(Math.random() * 2 - 1) * range;
            shakeOffsetY = (float)(Math.random() * 2 - 1) * range;
        } else {
            shakeOffsetX = 0f;
            shakeOffsetY = 0f;
        }
    }

    // =========================================================================
    // DRAW — called by GameThread every frame
    // =========================================================================

    /**
     * Called by GameThread. Locks the SurfaceHolder canvas, draws, then unlocks.
     * All drawing is done on the game thread, never the UI thread.
     */
    public void draw() {
        Canvas canvas = null;
        SurfaceHolder holder = getHolder();
        try {
            canvas = holder.lockCanvas();
            if (canvas == null) return;

            // Apply screen shake by translating the entire canvas.
            canvas.save();
            canvas.translate(shakeOffsetX, shakeOffsetY);

            drawFrame(canvas);

            canvas.restore();
        } finally {
            // CRITICAL: always unlock, even if an exception was thrown.
            // Failing to unlock causes the surface to freeze permanently.
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawFrame(Canvas canvas) {
        // Clear with the deep navy background
        canvas.drawColor(Color.rgb(10, 8, 20));

        switch (gameState) {
            case STATE_MENU:
                drawMenu(canvas);
                break;
            case STATE_LEVEL_INTRO:
                drawLevelIntro(canvas);
                break;
            case STATE_BOSS_INTRO:
                drawBossIntro(canvas);
                break;
            case STATE_PLAYING:
            case STATE_LEVEL_COMPLETE:
                drawPlaying(canvas);
                if (gameState == STATE_LEVEL_COMPLETE) drawLevelComplete(canvas);
                break;
            case STATE_GAME_OVER:
                drawPlaying(canvas);
                drawGameOver(canvas);
                break;
            case STATE_WIN:
                drawPlaying(canvas);
                drawVictory(canvas);
                break;
        }
    }

    /** Draw everything that appears during active gameplay. */
    private void drawPlaying(Canvas canvas) {
        // Back layer: arena tiles
        arenaManager.draw(canvas);

        // Objective items (shards, seals, core) drawn by ObjectiveManager
        objectiveManager.draw(canvas);

        // Echo shadows (behind enemies — they are ghosts)
        for (EchoShadow es : echoShadows) es.draw(canvas);

        // Enemies
        for (Enemy e : enemies) e.draw(canvas);

        // Projectiles
        for (Projectile p : projectiles) p.draw(canvas);

        // Boss (on top of everything except player and HUD)
        if (currentBoss != null) currentBoss.draw(canvas);

        // Player (always on top of enemies)
        player.draw(canvas);

        // HUD (always drawn last, always on top)
        hudManager.draw(canvas, player, levelManager.getCurrentLevel(),
                objectiveManager, currentBoss, tiltInput);
    }

    // ---- State-specific overlay draws ----

    private void drawMenu(Canvas canvas) {
        textPaint.setTextSize(screenH * 0.08f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(0, 255, 200));
        canvas.drawText("FRACTURE", screenW / 2f, screenH * 0.38f, textPaint);
        canvas.drawText("ARENA",    screenW / 2f, screenH * 0.48f, textPaint);

        textPaint.setTextSize(screenH * 0.035f);
        textPaint.setColor(Color.rgb(0, 180, 255));
        canvas.drawText("TAP TO START", screenW / 2f, screenH * 0.65f, textPaint);

        textPaint.setTextSize(screenH * 0.022f);
        textPaint.setColor(Color.rgb(100, 100, 120));
        canvas.drawText("Tilt your phone to move and fracture the arena",
                screenW / 2f, screenH * 0.72f, textPaint);
    }

    private void drawLevelIntro(Canvas canvas) {
        LevelConfig cfg = levelManager.getConfig(levelManager.getCurrentLevel());
        overlayPaint.setColor(Color.argb(200, 0, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        textPaint.setTextSize(screenH * 0.04f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(0, 212, 255));
        canvas.drawText("LEVEL " + cfg.levelNumber + " — " + cfg.name,
                screenW / 2f, screenH * 0.4f, textPaint);

        textPaint.setTextSize(screenH * 0.028f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("OBJECTIVE:", screenW / 2f, screenH * 0.52f, textPaint);

        textPaint.setTextSize(screenH * 0.025f);
        textPaint.setColor(Color.rgb(0, 255, 180));
        drawWrappedText(canvas, cfg.objectiveDescription, screenW / 2f, screenH * 0.60f,
                screenW * 0.85f, textPaint);

        textPaint.setTextSize(screenH * 0.022f);
        textPaint.setColor(Color.rgb(100, 100, 120));
        float timeLeft = Math.max(0f, introTimer - stateTimer);
        canvas.drawText("Starting in " + (int)Math.ceil(timeLeft) + "...",
                screenW / 2f, screenH * 0.80f, textPaint);
    }

    private void drawBossIntro(Canvas canvas) {
        LevelConfig cfg = levelManager.getConfig(levelManager.getCurrentLevel());

        // Dark pulsing background
        float pulse = (float)(0.5 + 0.5 * Math.sin(stateTimer * 4));
        int alpha   = (int)(150 + 50 * pulse);
        overlayPaint.setColor(Color.argb(alpha, 40, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(screenH * 0.035f);
        textPaint.setColor(Color.rgb(255, 60, 60));
        canvas.drawText("WARNING", screenW / 2f, screenH * 0.35f, textPaint);

        textPaint.setTextSize(screenH * 0.055f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(cfg.name, screenW / 2f, screenH * 0.48f, textPaint);

        textPaint.setTextSize(screenH * 0.025f);
        textPaint.setColor(Color.rgb(255, 60, 60));
        canvas.drawText("BOSS FIGHT", screenW / 2f, screenH * 0.60f, textPaint);
    }

    private void drawLevelComplete(Canvas canvas) {
        overlayPaint.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        textPaint.setTextSize(screenH * 0.06f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(0, 255, 200));
        canvas.drawText("LEVEL COMPLETE", screenW / 2f, screenH * 0.45f, textPaint);

        textPaint.setTextSize(screenH * 0.028f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Tap to continue", screenW / 2f, screenH * 0.58f, textPaint);
    }

    private void drawGameOver(Canvas canvas) {
        overlayPaint.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        textPaint.setTextSize(screenH * 0.07f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(255, 60, 60));
        canvas.drawText("GAME OVER", screenW / 2f, screenH * 0.42f, textPaint);

        textPaint.setTextSize(screenH * 0.028f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Tap to try again", screenW / 2f, screenH * 0.56f, textPaint);
    }

    private void drawVictory(Canvas canvas) {
        overlayPaint.setColor(Color.argb(190, 0, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        textPaint.setTextSize(screenH * 0.055f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(0, 255, 200));
        canvas.drawText("ARENA SHATTERED", screenW / 2f, screenH * 0.4f, textPaint);

        textPaint.setTextSize(screenH * 0.03f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("All bosses defeated.", screenW / 2f, screenH * 0.52f, textPaint);
        canvas.drawText("Tap to play again",    screenW / 2f, screenH * 0.60f, textPaint);
    }

    // =========================================================================
    // TOUCH INPUT
    // =========================================================================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        switch (gameState) {
            case STATE_MENU:
                gameState  = STATE_LEVEL_INTRO;
                stateTimer = 0f;
                levelManager.reset();
                break;

            case STATE_LEVEL_COMPLETE:
                stateTimer = 2.1f; // force immediate advance on tap
                break;

            case STATE_GAME_OVER:
                gameState  = STATE_MENU;
                initSystems();
                break;

            case STATE_WIN:
                gameState = STATE_MENU;
                initSystems();
                break;

            case STATE_PLAYING:
                player.onTap(event.getX(), event.getY());
                break;
        }
        return true;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Create the correct EchoShadow subtype based on what killed the enemy. */
    private EchoShadow createEcho(Enemy e) {
        switch (e.getEchoType()) {
            case EchoShadow.TYPE_DASH:
                return new DashEcho(e.getX(), e.getY(), e.getLastVelocityX());
            case EchoShadow.TYPE_PROJECTILE:
                return new ProjectileEcho(e.getX(), e.getY());
            case EchoShadow.TYPE_BLOCK:
                return new BlockEcho(e.getX(), e.getY(), e.getWidth(), e.getHeight());
            default:
                return null;
        }
    }

    /**
     * Draw text that wraps within maxWidth.
     * Splits the string on spaces and wraps to a new line when the current
     * line exceeds maxWidth. This keeps the objective description readable
     * on small screens.
     */
    private void drawWrappedText(Canvas canvas, String text, float cx, float startY,
                                  float maxWidth, Paint paint) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineHeight = paint.getTextSize() * 1.4f;
        float y = startY;

        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (paint.measureText(test) > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), cx, y, paint);
                y += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) canvas.drawText(line.toString(), cx, y, paint);
    }

    // =========================================================================
    // Accessors for external use
    // =========================================================================

    public int  getGameState()  { return gameState; }
    public void pauseThread()   { if (gameThread != null) gameThread.setPaused(true);  }
    public void resumeThread()  { if (gameThread != null) gameThread.setPaused(false); }
}
