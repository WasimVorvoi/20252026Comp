package com.example.voidfall.engine;

import android.view.SurfaceHolder;

/**
 * GameThread — the background thread that drives the game loop.
 *
 * RESPONSIBILITIES:
 *   1. Run at a consistent ~60 fps.
 *   2. Tell GameView to update game state (logic, physics, AI).
 *   3. Tell GameView to draw the current state to the SurfaceHolder's Canvas.
 *   4. Sleep the remaining time so we don't spin the CPU at 100%.
 *
 * FIXED TIMESTEP DESIGN:
 *   TARGET_FPS = 60 → TARGET_MS = 16 ms per frame.
 *   After update+draw, if time < 16ms, we sleep the rest.
 *   dt (delta time) passed to update() is capped at MAX_DT (50ms).
 *
 *   WHY CAP dt?
 *   If a single frame takes 200ms (e.g. GC pause), an uncapped dt would
 *   make every object "teleport" — a physics glitch called the
 *   "spiral of death". Capping at 50ms limits the damage from any spike.
 *
 * THREAD SAFETY:
 *   - `running` is volatile: written by main thread (stop/start),
 *     read by this thread in the loop condition.
 *   - `paused` is volatile: written by main thread (pause/resume),
 *     read here to decide whether to call update().
 *   - GameView.draw() locks the SurfaceHolder canvas before drawing,
 *     which is the correct pattern for SurfaceView.
 *
 * LIFECYCLE:
 *   surfaceCreated  → new GameThread(view, holder).start()
 *   surfaceDestroyed→ thread.stopLoop(); thread.join()
 *   onPause         → thread.setPaused(true)
 *   onResume        → thread.setPaused(false)
 */
public class GameThread extends Thread {

    private static final int   TARGET_FPS = 60;
    private static final long  TARGET_MS  = 1000L / TARGET_FPS;   // ≈ 16ms
    private static final float MAX_DT     = 0.050f;               // cap at 50ms

    private final GameView      gameView;
    private final SurfaceHolder surfaceHolder;

    // volatile: both read by this thread, written by the main thread.
    private volatile boolean running = false;
    private volatile boolean paused  = false;

    public GameThread(GameView gameView, SurfaceHolder surfaceHolder) {
        super("GameThread");
        this.gameView      = gameView;
        this.surfaceHolder = surfaceHolder;
    }

    /** Called from surfaceCreated via thread.start(). */
    @Override
    public void run() {
        running = true;
        long lastTime = System.currentTimeMillis();

        while (running) {
            long frameStart = System.currentTimeMillis();

            // Compute delta time in SECONDS (capped to prevent spiral of death).
            float dt = Math.min((frameStart - lastTime) / 1000f, MAX_DT);
            lastTime = frameStart;

            if (!paused) {
                // 1. Update all game logic for this frame.
                gameView.update(dt);

                // 2. Draw the updated state to the surface.
                gameView.draw();
            }

            // 3. Sleep for whatever time is left in the 16ms budget.
            long frameTime = System.currentTimeMillis() - frameStart;
            long sleepTime = TARGET_MS - frameTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Signals the loop to exit.
     * Call this before join() in surfaceDestroyed so the thread exits cleanly.
     */
    public void stopLoop() {
        running = false;
    }

    /** Freeze updates (but keep thread alive) when app goes to background. */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isRunning() {
        return running;
    }
}
