package com.example.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.frozenanimlib.AnimationManager;
import com.frozenanimlib.FadeAnimation;

import java.util.List;

/**
 * GameView - the main SurfaceView that owns the game loop, state machine, and rendering.
 *
 * WHAT IS A SURFACEVIEW?
 *   A regular Android View can only be drawn on the main (UI) thread.
 *   SurfaceView provides a dedicated drawing surface (the "Surface") that a
 *   BACKGROUND THREAD can draw onto safely.  This is essential for smooth games.
 *
 * HOW IT WORKS:
 *   1. GameView extends SurfaceView and implements SurfaceHolder.Callback.
 *   2. When the surface is ready (surfaceCreated), we start the GameThread.
 *   3. GameThread calls update(dt) and draw(canvas) 60 times per second.
 *   4. When the surface is destroyed (app minimised), we stop the thread.
 *
 * GAME STATE MACHINE:
 *   The game is always in one of these states:
 *       MENU          → player sees the main menu
 *       PLAYING       → game loop is active
 *       PAUSED        → game is frozen, pause screen shown
 *       LEVEL_COMPLETE→ brief celebration before next level
 *       GAME_OVER     → player died; offer restart
 *       GAME_WON      → all levels finished
 *
 *   State transitions are triggered by game events (player reaching goal, dying, etc.)
 *   or by button presses detected in onTouchEvent.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    // ---- game state enum ----
    public enum GameState { MENU, PLAYING, PAUSED, LEVEL_COMPLETE, GAME_OVER, GAME_WON }
    private GameState state = GameState.MENU;

    // ---- core systems ----
    private GameThread       gameThread;
    private SoundManager     soundManager;
    private SensorController sensorController;
    private AnimationManager animManager;

    // ---- level state ----
    private Level       currentLevel;
    private Player      player;
    private Goal        goal;
    private List<Hazard> hazards;
    private HUD         hud;
    private int         currentLevelNum = 1;  // 1-based

    // ---- screen dimensions (set when surface is created) ----
    private int screenW, screenH;

    // ---- timing for LEVEL_COMPLETE state (auto-advance after a short delay) ----
    private float levelCompleteTimer = 0f;
    private static final float LEVEL_COMPLETE_DELAY = 2.5f; // seconds before next level

    // ---- timing for GAME_OVER state (brief pause before controls appear) ----
    private float gameOverTimer = 0f;

    // ---- UI button rectangles (updated in draw methods; checked in onTouchEvent) ----
    private final RectF btnPlay    = new RectF();
    private final RectF btnRestart = new RectF();
    private final RectF btnMenu    = new RectF();

    // ---- paints shared across draw methods ----
    private final Paint bgPaint     = new Paint();
    private final Paint titlePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bodyPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint btnPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint btnTextPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint= new Paint();
    private final Paint snowPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---- decorative menu snowflakes ----
    private final float[] snowX  = new float[30];
    private final float[] snowY  = new float[30];
    private final float[] snowR  = new float[30];
    private final float[] snowVY = new float[30]; // fall speed

    // ---- last delta time (stored so drawPlaying can pass it to HUD) ----
    private float lastDt = 0f;

    // ---- wind sound cooldown (prevent spamming wind sfx) ----
    private float windSoundTimer = 0f;
    private static final float WIND_SOUND_INTERVAL = 1.5f;

    // ---- crack sound cooldown ----
    private float crackSoundTimer = 0f;
    private static final float CRACK_SOUND_INTERVAL = 0.5f;

    // ------------------------------------------------------------------ constructor

    public GameView(Context context) {
        super(context);
        // Register this class to receive surface lifecycle events
        getHolder().addCallback(this);

        // Allow this view to receive touch events
        setFocusable(true);

        // Initialise supporting systems
        soundManager     = new SoundManager(context);
        sensorController = new SensorController(context);
        animManager      = new AnimationManager();
        hud              = new HUD();

        // Set up the animations that run on the menu screen
        setupMenuAnimations();

        // Initialise decorative snowflakes with random positions
        for (int i = 0; i < snowX.length; i++) {
            snowX[i]  = (float)(Math.random() * 1080); // will be rescaled in draw
            snowY[i]  = (float)(Math.random() * 1920);
            snowR[i]  = (float)(Math.random() * 4 + 2);
            snowVY[i] = (float)(Math.random() * 40 + 20);
        }
    }

    // ------------------------------------------------------------------ animations

    /** Register all animations used across the game. */
    private void setupMenuAnimations() {
        // Title text pulse on the menu screen
        animManager.registerPulse("menu_title_pulse", 0.95f, 1.05f, 1.6f, true);
        // "Play" button fade-in when menu first appears
        animManager.registerFade("menu_btn_fade", 0f, 1f, 0.8f, false);
        // Level complete message bounce
        animManager.registerBounce("complete_bounce", 18f, 0.4f, 3);
        // Level complete overlay fade
        animManager.registerFade("complete_fade", 0f, 1f, 0.4f, false);
        // Game over overlay fade
        animManager.registerFade("gameover_fade", 0f, 1f, 0.6f, false);
        // Player pulse (glow while alive)
        animManager.registerPulse("player_pulse", 0.92f, 1.08f, 0.9f, true);
        // Death shake registered dynamically in Player.die()
    }

    /** Re-register animations that need resetting when a new level loads. */
    private void resetLevelAnimations() {
        animManager.reset("player_pulse");
        animManager.reset("complete_bounce");
        animManager.reset("complete_fade");
        animManager.reset("gameover_fade");
        goal.registerAnimations(animManager); // re-register goal pulse
        hud.resetHint();
    }

    // ------------------------------------------------------------------ surface callbacks

    /**
     * Called when the SurfaceView is ready to be drawn on.
     * This is where we start the game thread.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = getWidth();
        screenH = getHeight();

        // Start the accelerometer
        sensorController.register();

        // Create the first level and start the menu
        loadLevel(currentLevelNum);

        // Create and start the game thread
        gameThread = new GameThread(holder, this);
        gameThread.setRunning(true);
        gameThread.start();

        // Start menu music
        soundManager.startMenuMusic();
    }

    /**
     * Called when the surface dimensions change (e.g. rotation).
     * We simply update our stored dimensions; the level recalculates tile size.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenW = width;
        screenH = height;
        if (currentLevel != null) {
            currentLevel.fitToScreen(screenW, screenH);
        }
    }

    /**
     * Called when the surface is destroyed (app minimised or closed).
     * We must stop the game thread here to prevent it from trying to draw
     * on a surface that no longer exists (which would crash the app).
     *
     * The join() call waits until the thread has fully stopped before returning.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        sensorController.unregister();
        soundManager.pauseMusic();

        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join(); // wait for the thread to finish
                retry = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ------------------------------------------------------------------ level loading

    /**
     * Load the given level number, building the Level, Player, Goal, and Hazards.
     *
     * @param levelNum  1-based level index
     */
    private void loadLevel(int levelNum) {
        currentLevelNum = levelNum;

        // Build the level tile grid
        currentLevel = LevelManager.createLevel(levelNum);

        // Fit tiles to current screen size (sets tileW, tileH, offsetX, offsetY)
        if (screenW > 0 && screenH > 0) {
            currentLevel.fitToScreen(screenW, screenH);
        }

        // Place the player at the start tile's centre
        float pX = currentLevel.startPixelX();
        float pY = currentLevel.startPixelY();
        float playerRadius = currentLevel.tileW * 0.38f; // slightly smaller than half a tile
        player = new Player(pX, pY, playerRadius);

        // Place the goal at the goal tile's centre
        float gX = currentLevel.tileCentreX(currentLevel.goalCol);
        float gY = currentLevel.tileCentreY(currentLevel.goalRow);
        goal = new Goal(gX, gY, currentLevel.tileW * 0.42f);

        // Create hazards for this level
        hazards = LevelManager.createHazards(levelNum, currentLevel);

        // Set up animations
        resetLevelAnimations();

        levelCompleteTimer = 0f;
        gameOverTimer      = 0f;
        windSoundTimer     = 0f;
        crackSoundTimer    = 0f;
    }

    // ------------------------------------------------------------------ update

    /**
     * Advance all game logic by dt seconds.
     * Called from GameThread — runs on the GAME THREAD, not the UI thread.
     *
     * @param dt  delta time in seconds since the last frame
     */
    public void update(float dt) {
        // Store dt so draw methods can use it (e.g. HUD hint timer)
        lastDt = dt;

        // Always update animations regardless of game state
        animManager.updateAll(dt);

        switch (state) {
            case MENU:
                updateMenu(dt);
                break;
            case PLAYING:
                updatePlaying(dt);
                break;
            case PAUSED:
                // Nothing moves while paused
                break;
            case LEVEL_COMPLETE:
                updateLevelComplete(dt);
                break;
            case GAME_OVER:
                gameOverTimer += dt;
                break;
            case GAME_WON:
                // Nothing to update
                break;
        }
    }

    private void updateMenu(float dt) {
        // Animate falling snowflakes on the menu screen
        for (int i = 0; i < snowY.length; i++) {
            snowY[i] += snowVY[i] * dt;
            if (snowY[i] > screenH + 10) {
                snowY[i] = -10f;
                snowX[i] = (float)(Math.random() * screenW);
            }
        }
    }

    private void updatePlaying(float dt) {
        // 1. Update sensor (smooth the raw accelerometer data)
        sensorController.update();
        float tiltX = sensorController.getTiltX();
        float tiltY = sensorController.getTiltY();

        // 2. Apply wind force from wind-zone tiles
        float[] wind = currentLevel.getWindForceAt(player.x, player.y, 300f);
        tiltX += wind[0] / 550f; // scale to match ACCEL_STRENGTH in Player
        tiltY += wind[1] / 550f;

        // 2a. Wind sound
        if (wind[0] != 0 || wind[1] != 0) {
            windSoundTimer += dt;
            if (windSoundTimer >= WIND_SOUND_INTERVAL) {
                soundManager.playWind();
                windSoundTimer = 0f;
            }
        }

        // 3. Update cracking ice tiles
        int prevTile = currentLevel.getTileAt(player.x, player.y);
        currentLevel.updateCrackingTiles(player.x, player.y, dt);
        int newTile  = currentLevel.getTileAt(player.x, player.y);
        // Detect when a cracking tile reaches full crack — play crack sound
        if (prevTile == Tile.CRACKING || newTile == Tile.CRACKING) {
            crackSoundTimer += dt;
            if (crackSoundTimer >= CRACK_SOUND_INTERVAL) {
                soundManager.playCrack();
                crackSoundTimer = 0f;
            }
        }

        // 4. Update player physics and collision
        player.update(dt, tiltX, tiltY, currentLevel, animManager);

        // 5. Update all moving hazards
        for (Hazard h : hazards) {
            h.update(dt);
            // Check if player touches any moving hazard
            if (player.alive && h.collidesWithPlayer(player.x, player.y, player.radius)) {
                player.die(animManager);
                soundManager.playDeath();
            }
        }

        // 6. Check player death (from tile)
        if (!player.alive) {
            state = GameState.GAME_OVER;
            animManager.reset("gameover_fade");
            return;
        }

        // 7. Check if player reached the goal
        if (goal.isReached(player.x, player.y, player.radius)) {
            state = GameState.LEVEL_COMPLETE;
            animManager.reset("complete_fade");
            animManager.reset("complete_bounce");
            soundManager.playComplete();
        }
    }

    private void updateLevelComplete(float dt) {
        levelCompleteTimer += dt;
        if (levelCompleteTimer >= LEVEL_COMPLETE_DELAY) {
            advanceToNextLevel();
        }
    }

    /** Move to the next level, or show the win screen if all levels are done. */
    private void advanceToNextLevel() {
        if (currentLevelNum < LevelManager.TOTAL_LEVELS) {
            currentLevelNum++;
            loadLevel(currentLevelNum);
            state = GameState.PLAYING;
            soundManager.startGameMusic();
        } else {
            state = GameState.GAME_WON;
        }
    }

    // ------------------------------------------------------------------ draw

    /**
     * Render the current frame onto the given Canvas.
     * Called from GameThread — runs on the GAME THREAD.
     *
     * @param canvas  the Canvas locked from SurfaceHolder
     */
    public void draw(Canvas canvas) {
        if (canvas == null) return;

        switch (state) {
            case MENU:          drawMenu(canvas);         break;
            case PLAYING:       drawPlaying(canvas);      break;
            case PAUSED:        drawPaused(canvas);       break;
            case LEVEL_COMPLETE:drawLevelComplete(canvas);break;
            case GAME_OVER:     drawGameOver(canvas);     break;
            case GAME_WON:      drawGameWon(canvas);      break;
        }
    }

    // ---- draw helpers ----

    private void drawPlaying(Canvas canvas) {
        // Draw tiles (background + all tile types)
        currentLevel.draw(canvas);

        // Draw the goal portal (uses its own pulse animation)
        goal.draw(canvas, animManager);

        // Draw moving hazards
        for (Hazard h : hazards) {
            h.draw(canvas);
        }

        // Draw the player
        player.draw(canvas, animManager);

        // Draw HUD on top — pass lastDt so the hint timer advances correctly
        hud.draw(canvas, currentLevelNum,
                LevelManager.getLevelName(currentLevelNum),
                LevelManager.getLevelHint(currentLevelNum),
                screenW, screenH, lastDt);
    }

    private void drawMenu(Canvas canvas) {
        float density = screenW / 360f;

        // Background gradient effect (dark navy to dark blue)
        bgPaint.setColor(Color.rgb(5, 12, 35));
        canvas.drawRect(0, 0, screenW, screenH, bgPaint);
        bgPaint.setColor(Color.rgb(8, 20, 55));
        canvas.drawRect(0, screenH * 0.4f, screenW, screenH, bgPaint);

        // Falling snowflakes
        snowPaint.setColor(Color.argb(160, 200, 230, 255));
        for (int i = 0; i < snowX.length; i++) {
            canvas.drawCircle(snowX[i] % screenW, snowY[i], snowR[i], snowPaint);
        }

        // ---- Title ----
        float pulseScale = 1f;
        if (animManager.getPulse("menu_title_pulse") != null) {
            pulseScale = animManager.getPulse("menu_title_pulse").getScale();
        }

        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setColor(Color.rgb(0, 220, 210));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(52f * density * pulseScale);

        float titleY = screenH * 0.28f;
        canvas.drawText("FROZEN", screenW / 2f, titleY, titlePaint);
        titlePaint.setTextSize(52f * density * pulseScale);
        titlePaint.setColor(Color.rgb(180, 230, 255));
        canvas.drawText("ESCAPE", screenW / 2f, titleY + 58f * density * pulseScale, titlePaint);

        // Subtitle
        bodyPaint.setColor(Color.rgb(100, 170, 220));
        bodyPaint.setTextAlign(Paint.Align.CENTER);
        bodyPaint.setTextSize(14f * density);
        canvas.drawText("Tilt to navigate the icy maze", screenW / 2f, screenH * 0.42f, bodyPaint);

        // ---- Play button ----
        float btnAlpha = 1f;
        FadeAnimation fade = animManager.getFade("menu_btn_fade");
        if (fade != null) btnAlpha = fade.getAlpha();

        float btnW  = 180f * density;
        float btnH  = 52f  * density;
        float btnX  = (screenW - btnW) / 2f;
        float btnY  = screenH * 0.55f;
        btnPlay.set(btnX, btnY, btnX + btnW, btnY + btnH);

        btnPaint.setStyle(Paint.Style.FILL);
        btnPaint.setColor(Color.argb((int)(btnAlpha * 220), 0, 160, 150));
        canvas.drawRoundRect(btnPlay, 14f, 14f, btnPaint);

        // Button border
        btnPaint.setStyle(Paint.Style.STROKE);
        btnPaint.setStrokeWidth(2f);
        btnPaint.setColor(Color.argb((int)(btnAlpha * 255), 0, 220, 210));
        canvas.drawRoundRect(btnPlay, 14f, 14f, btnPaint);

        btnTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        btnTextPaint.setColor(Color.argb((int)(btnAlpha * 255), 255, 255, 255));
        btnTextPaint.setTextAlign(Paint.Align.CENTER);
        btnTextPaint.setTextSize(20f * density);
        canvas.drawText("PLAY", screenW / 2f, btnY + btnH * 0.65f, btnTextPaint);

        // Small decorative ice crystal dots near title
        snowPaint.setColor(Color.argb(80, 0, 200, 190));
        for (int i = 0; i < 6; i++) {
            float angle = (float)(i * Math.PI / 3f);
            float radius = 90f * density;
            float sx = screenW / 2f + (float)(Math.cos(angle)) * radius;
            float sy = screenH * 0.35f + (float)(Math.sin(angle)) * radius;
            canvas.drawCircle(sx, sy, 3f * density, snowPaint);
        }
    }

    private void drawPaused(Canvas canvas) {
        // Draw the game world behind the overlay
        drawPlaying(canvas);

        float density = screenW / 360f;

        // Semi-transparent dark overlay
        overlayPaint.setColor(Color.argb(160, 0, 5, 20));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        // "PAUSED" title
        titlePaint.setColor(Color.rgb(180, 230, 255));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(42f * density);
        canvas.drawText("PAUSED", screenW / 2f, screenH * 0.35f, titlePaint);

        // Resume button
        float btnW = 180f * density;
        float btnH = 52f  * density;
        float bX   = (screenW - btnW) / 2f;
        float bY   = screenH * 0.48f;
        btnPlay.set(bX, bY, bX + btnW, bY + btnH);
        drawButton(canvas, btnPlay, "RESUME", density);

        // Menu button
        float mY = bY + btnH + 20f * density;
        btnMenu.set(bX, mY, bX + btnW, mY + btnH);
        drawButton(canvas, btnMenu, "MAIN MENU", density);
    }

    private void drawLevelComplete(Canvas canvas) {
        drawPlaying(canvas);

        float density = screenW / 360f;
        float alpha   = 1f;
        FadeAnimation fade = animManager.getFade("complete_fade");
        if (fade != null) alpha = fade.getAlpha();

        // Overlay
        overlayPaint.setColor(Color.argb((int)(alpha * 130), 0, 30, 25));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        // Bounce offset
        float bounceY = 0f;
        if (animManager.getBounce("complete_bounce") != null) {
            bounceY = animManager.getBounce("complete_bounce").getOffsetY();
        }

        titlePaint.setColor(Color.argb((int)(alpha * 255), 0, 230, 210));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(38f * density);
        canvas.drawText("LEVEL COMPLETE!", screenW / 2f, screenH * 0.38f + bounceY, titlePaint);

        if (currentLevelNum < LevelManager.TOTAL_LEVELS) {
            bodyPaint.setColor(Color.argb((int)(alpha * 200), 180, 230, 255));
            bodyPaint.setTextSize(15f * density);
            bodyPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Next: " + LevelManager.getLevelName(currentLevelNum + 1),
                    screenW / 2f, screenH * 0.48f, bodyPaint);
        }
    }

    private void drawGameOver(Canvas canvas) {
        drawPlaying(canvas);

        float density = screenW / 360f;
        float alpha   = Math.min(gameOverTimer / 0.6f, 1f);

        // Dark red overlay
        overlayPaint.setColor(Color.argb((int)(alpha * 150), 30, 0, 0));
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);

        // Shake offset for the game over text (from death shake)
        float shakeX = 0f;
        if (animManager.getShake("player_death_shake") != null) {
            shakeX = animManager.getShake("player_death_shake").getOffsetX();
        }

        titlePaint.setColor(Color.argb((int)(alpha * 255), 220, 80, 60));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(42f * density);
        canvas.drawText("FROZEN SOLID", screenW / 2f + shakeX, screenH * 0.35f, titlePaint);

        bodyPaint.setColor(Color.argb((int)(alpha * 200), 180, 200, 220));
        bodyPaint.setTextSize(14f * density);
        bodyPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("You didn't make it...", screenW / 2f, screenH * 0.44f, bodyPaint);

        // Show buttons after a brief pause
        if (gameOverTimer > 1.0f) {
            float btnW = 180f * density;
            float btnH = 52f  * density;
            float bX   = (screenW - btnW) / 2f;
            float bY   = screenH * 0.55f;
            btnRestart.set(bX, bY, bX + btnW, bY + btnH);
            drawButton(canvas, btnRestart, "TRY AGAIN", density);

            float mY = bY + btnH + 20f * density;
            btnMenu.set(bX, mY, bX + btnW, mY + btnH);
            drawButton(canvas, btnMenu, "MAIN MENU", density);
        }
    }

    private void drawGameWon(Canvas canvas) {
        float density = screenW / 360f;

        bgPaint.setColor(Color.rgb(0, 20, 40));
        canvas.drawRect(0, 0, screenW, screenH, bgPaint);

        // Snowflakes celebration
        snowPaint.setColor(Color.argb(200, 0, 220, 200));
        for (int i = 0; i < snowX.length; i++) {
            canvas.drawCircle(snowX[i] % screenW, snowY[i], snowR[i] * 1.5f, snowPaint);
        }

        titlePaint.setColor(Color.rgb(0, 230, 210));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(40f * density);
        canvas.drawText("YOU ESCAPED!", screenW / 2f, screenH * 0.30f, titlePaint);

        titlePaint.setColor(Color.rgb(180, 230, 255));
        titlePaint.setTextSize(24f * density);
        canvas.drawText("The ice is behind you.", screenW / 2f, screenH * 0.40f, titlePaint);

        bodyPaint.setColor(Color.rgb(120, 180, 220));
        bodyPaint.setTextSize(14f * density);
        bodyPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("All " + LevelManager.TOTAL_LEVELS + " levels complete!", screenW / 2f, screenH * 0.50f, bodyPaint);

        float btnW = 180f * density;
        float btnH = 52f  * density;
        float bX   = (screenW - btnW) / 2f;
        float bY   = screenH * 0.62f;
        btnMenu.set(bX, bY, bX + btnW, bY + btnH);
        drawButton(canvas, btnMenu, "MAIN MENU", density);
    }

    /** Helper: draw a styled ice-theme button rectangle with text. */
    private void drawButton(Canvas canvas, RectF rect, String label, float density) {
        btnPaint.setStyle(Paint.Style.FILL);
        btnPaint.setColor(Color.argb(220, 20, 70, 130));
        canvas.drawRoundRect(rect, 12f, 12f, btnPaint);

        btnPaint.setStyle(Paint.Style.STROKE);
        btnPaint.setStrokeWidth(2f);
        btnPaint.setColor(Color.rgb(80, 160, 220));
        canvas.drawRoundRect(rect, 12f, 12f, btnPaint);

        btnTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        btnTextPaint.setColor(Color.rgb(220, 240, 255));
        btnTextPaint.setTextAlign(Paint.Align.CENTER);
        btnTextPaint.setTextSize(18f * density);
        float textY = rect.top + (rect.height() + btnTextPaint.getTextSize() * 0.7f) / 2f;
        canvas.drawText(label, rect.centerX(), textY, btnTextPaint);
    }

    // ------------------------------------------------------------------ touch input

    /**
     * Handle all touch events for button presses.
     * This runs on the MAIN (UI) thread — state changes are safe because
     * they are simple field writes (atomic on Android's JVM).
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        float tx = event.getX();
        float ty = event.getY();

        switch (state) {
            case MENU:
                if (btnPlay.contains(tx, ty)) {
                    soundManager.playClick();
                    currentLevelNum = 1;
                    loadLevel(1);
                    state = GameState.PLAYING;
                    soundManager.startGameMusic();
                    animManager.reset("menu_btn_fade");
                }
                break;

            case PLAYING:
                // Check the pause button (drawn by HUD)
                if (hud.getPauseButtonBounds().contains(tx, ty)) {
                    soundManager.playClick();
                    state = GameState.PAUSED;
                }
                break;

            case PAUSED:
                if (btnPlay.contains(tx, ty)) {      // "RESUME"
                    soundManager.playClick();
                    state = GameState.PLAYING;
                } else if (btnMenu.contains(tx, ty)) { // "MAIN MENU"
                    soundManager.playClick();
                    state = GameState.MENU;
                    soundManager.startMenuMusic();
                    animManager.reset("menu_btn_fade");
                    animManager.reset("menu_title_pulse");
                }
                break;

            case GAME_OVER:
                if (gameOverTimer > 1.0f) {
                    if (btnRestart.contains(tx, ty)) {
                        soundManager.playClick();
                        loadLevel(currentLevelNum);
                        state = GameState.PLAYING;
                        soundManager.startGameMusic();
                    } else if (btnMenu.contains(tx, ty)) {
                        soundManager.playClick();
                        state = GameState.MENU;
                        soundManager.startMenuMusic();
                        animManager.reset("menu_btn_fade");
                    }
                }
                break;

            case GAME_WON:
                if (btnMenu.contains(tx, ty)) {
                    soundManager.playClick();
                    state = GameState.MENU;
                    soundManager.startMenuMusic();
                    animManager.reset("menu_btn_fade");
                    animManager.reset("menu_title_pulse");
                }
                break;

            default:
                break;
        }
        return true;
    }

    // ------------------------------------------------------------------ lifecycle

    /**
     * Call from Activity.onPause() — pauses music and sensor.
     * The game thread itself keeps running unless surfaceDestroyed is called.
     */
    public void onPause() {
        soundManager.pauseMusic();
        sensorController.unregister();
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        }
    }

    /**
     * Call from Activity.onResume() — restores music and sensor.
     */
    public void onResume() {
        soundManager.resumeMusic();
        sensorController.register();
    }

    /**
     * Call from Activity.onDestroy() — releases all audio resources.
     */
    public void release() {
        soundManager.release();
    }
}
