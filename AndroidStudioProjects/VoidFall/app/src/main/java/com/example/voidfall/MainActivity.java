package com.example.voidfall;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.voidfall.engine.GameView;
import com.example.voidfall.input.TiltInput;
import com.example.voidfall.system.SoundManager;

/**
 * MainActivity — the single Activity that hosts the game.
 *
 * RESPONSIBILITIES:
 *   1. Set up fullscreen window flags.
 *   2. Create TiltInput and SoundManager (both need a Context).
 *   3. Create GameView and set it as the content view (no XML layout needed).
 *   4. Forward lifecycle events to the correct sub-systems:
 *        onPause  → stop sensor + pause game thread + pause music
 *        onResume → restart sensor + resume game thread + resume music
 *        onDestroy→ release SoundManager resources
 *
 * WHY NO XML LAYOUT:
 *   The game renders entirely via SurfaceView / Canvas.
 *   There are no TextView, Button, or ConstraintLayout widgets.
 *   Adding a layout file would just add unnecessary inflation overhead.
 *
 * CONFIGCHANGES in Manifest:
 *   We declare android:configChanges="orientation|screenSize|keyboardHidden"
 *   so the Activity does NOT restart on rotation.
 *   The game is locked to portrait, but the configChanges declaration means
 *   even a brief sensor event or keyboard trigger won't restart the Activity
 *   and destroy our SurfaceView + game state.
 */
public class MainActivity extends AppCompatActivity {

    private GameView     gameView;
    private TiltInput    tiltInput;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on while playing — tilting doesn't touch the screen,
        // so the phone would otherwise dim after the timeout.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Hide system bars to make SurfaceView fill the entire display.
        hideSystemUI();

        // Create sensor wrapper — registers listener in onResume.
        tiltInput = new TiltInput(this);

        // Create sound manager — loads all sound effects into SoundPool.
        soundManager = new SoundManager(this);

        // Create the SurfaceView that runs the entire game.
        // No XML layout: GameView IS the content view.
        gameView = new GameView(this, tiltInput, soundManager);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tiltInput.register();      // start receiving accelerometer events
        gameView.resumeThread();   // unpause the game loop
        soundManager.resumeMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tiltInput.unregister();   // stop sensor to save battery
        gameView.pauseThread();   // pause the game loop (thread stays alive)
        soundManager.pauseMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();   // free SoundPool + MediaPlayer resources
    }

    /**
     * Hides the status bar and navigation bar using immersive sticky mode.
     * The system bars reappear if the user swipes from the edge, then hide
     * again automatically after a short delay.
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
}
