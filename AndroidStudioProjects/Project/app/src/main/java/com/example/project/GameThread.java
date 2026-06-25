package com.example.project;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * GameThread - the dedicated thread that drives the game loop.
 *
 * WHY A SEPARATE THREAD?
 *   The Android UI runs on the "main thread" (also called the UI thread).
 *   If we ran heavy game logic on the main thread, Android would block the UI,
 *   and the phone would become unresponsive or throw an "Application Not Responding" error.
 *   By running the game loop on a background thread, the UI remains smooth.
 *
 * THE GAME LOOP (update → draw → sleep):
 *   1. Record the time at the start of the frame.
 *   2. Call gameView.update(dt) — advance all game logic by delta-time.
 *   3. Lock the SurfaceHolder's Canvas (gives us a drawable surface).
 *   4. Call gameView.draw(canvas) — render the current frame.
 *   5. Unlock and post the canvas (makes the frame visible on screen).
 *   6. Calculate how long steps 1–5 took.
 *   7. Sleep for the remaining time in the target frame budget.
 *
 * DELTA TIME (dt):
 *   dt = time elapsed since the last frame, measured in seconds.
 *   Using dt in physics formulas makes movement frame-rate independent.
 *   Example: position += velocity * dt  works the same at 30 fps or 60 fps.
 *
 * TARGET FPS:
 *   We aim for 60 frames per second → target frame time = 1000ms / 60 ≈ 16ms.
 *   If a frame finishes faster, we sleep to avoid wasting CPU.
 *   If a frame finishes slower, we skip the sleep and run the next frame immediately.
 *   dt is capped at 0.05s (20 fps minimum) so slow frames don't cause giant physics steps.
 */
public class GameThread extends Thread {

    // Target frame rate
    private static final int   TARGET_FPS       = 60;
    // Target time per frame in milliseconds
    private static final long  TARGET_FRAME_MS  = 1000L / TARGET_FPS;
    // Maximum delta time cap — prevents huge physics jumps on very slow frames
    private static final float MAX_DELTA_SECONDS = 0.05f;

    // The SurfaceHolder gives us a Canvas to draw on
    private final SurfaceHolder surfaceHolder;
    // The GameView provides update() and draw() methods
    private final GameView      gameView;

    // Controls the game loop — set to false to stop the thread
    private volatile boolean running = false;

    /**
     * @param surfaceHolder  the holder from the SurfaceView (provides the Canvas)
     * @param gameView       the view that contains update() and draw() logic
     */
    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView      = gameView;
        setName("GameThread"); // gives the thread a readable name for debugging
    }

    /** Start the game loop. Call this from surfaceCreated(). */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /** Returns whether the loop is currently active. */
    public boolean isRunning() {
        return running;
    }

    /**
     * The game loop — runs continuously while 'running' is true.
     * This method is called automatically by Thread.start().
     */
    @Override
    public void run() {
        long previousTime = System.currentTimeMillis();

        while (running) {
            Canvas canvas = null;

            // ---- 1. Calculate delta time ----
            long currentTime = System.currentTimeMillis();
            long elapsedMs   = currentTime - previousTime;
            previousTime     = currentTime;

            // Convert to seconds and cap it to prevent runaway physics
            float dt = Math.min(elapsedMs / 1000f, MAX_DELTA_SECONDS);

            // ---- 2. Update game logic ----
            gameView.update(dt);

            // ---- 3 & 4. Draw to the Surface ----
            try {
                // lockCanvas() blocks if the surface is not yet available
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    // Draw everything onto the canvas
                    // Synchronise on surfaceHolder so the main thread can't destroy
                    // the surface while we're drawing on it
                    synchronized (surfaceHolder) {
                        gameView.draw(canvas);
                    }
                }
            } finally {
                // ---- 5. Post the frame ----
                // Always unlock, even if an exception was thrown during drawing.
                // Failing to unlock would permanently freeze the surface.
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        // Surface may have been destroyed — ignore gracefully
                    }
                }
            }

            // ---- 6 & 7. Sleep for the remaining frame budget ----
            long frameTime    = System.currentTimeMillis() - currentTime;
            long sleepTime    = TARGET_FRAME_MS - frameTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupted status
                    break;
                }
            }
        }
    }
}
