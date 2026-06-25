package com.example.project;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - the single Activity that hosts the entire game.
 *
 * WHAT IS AN ACTIVITY?
 *   In Android, an Activity is one "screen" of an app. It has a lifecycle:
 *   onCreate → onStart → onResume → (running) → onPause → onStop → onDestroy.
 *
 * WHY ONLY ONE ACTIVITY?
 *   All game screens (menu, gameplay, game-over) are handled by GameView's
 *   internal state machine. This avoids Activity overhead when switching screens,
 *   which matters for performance in games.
 *
 * WHAT DOES MAINACTIVITY DO?
 *   1. Sets the window to full-screen (hides status bar and title bar).
 *   2. Keeps the screen on while the game is running (FLAG_KEEP_SCREEN_ON).
 *   3. Creates the GameView and sets it as the content view.
 *   4. Forwards lifecycle events (pause, resume, destroy) to GameView.
 */
public class MainActivity extends AppCompatActivity {

    // The main game view — this is the only view we set as content
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ---- Full-screen setup ----

        // Remove the title bar at the top of the Activity window
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set window flags:
        //   FLAG_FULLSCREEN     → hides the status bar (clock, battery, etc.)
        //   FLAG_KEEP_SCREEN_ON → prevents the screen from dimming during gameplay
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        // ---- Create and show the GameView ----

        // Pass 'this' (the Activity context) so GameView can access sensors,
        // sound services, and resources.
        gameView = new GameView(this);

        // setContentView replaces any XML layout — the entire screen is the GameView.
        setContentView(gameView);
    }

    /**
     * Called when the app loses focus (user presses Home or a notification appears).
     * We pause the game so it doesn't run wastefully in the background.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.onPause();
        }
    }

    /**
     * Called when the app regains focus (user returns to the game).
     * We resume music and re-register the tilt sensor.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.onResume();
        }
    }

    /**
     * Called when the Activity is permanently destroyed (app closed by system).
     * We release all audio resources here to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.release();
        }
    }
}
