package com.example.project;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

/**
 * SoundManager - loads and plays all game audio.
 *
 * ARCHITECTURE DECISION:
 *   Short sound effects (< 2 seconds): use SoundPool.
 *     SoundPool keeps sounds pre-loaded in memory so they play with no delay.
 *     It is designed for frequent, short playback — perfect for game SFX.
 *
 *   Background music tracks: use MediaPlayer.
 *     MediaPlayer streams from storage and uses less memory than SoundPool for
 *     long audio. It is designed for one track at a time.
 *
 * GRACEFUL DEGRADATION:
 *   If a sound resource file does not exist in res/raw/, the method returns 0
 *   (via getIdentifier) and we simply skip that sound.  This means the game
 *   compiles and runs even if no audio files have been added yet.
 *
 * HOW TO ADD SOUND FILES:
 *   1. Place your .ogg or .mp3 files in:  app/src/main/res/raw/
 *   2. Use these exact filenames (lowercase, no spaces):
 *        music_menu.ogg
 *        music_game.ogg
 *        sfx_slide.ogg
 *        sfx_death.ogg
 *        sfx_complete.ogg
 *        sfx_click.ogg
 *        sfx_crack.ogg
 *        sfx_wind.ogg
 *   3. Rebuild the project — SoundManager will detect them automatically.
 *
 * FREE SOUND SOURCES:
 *   freesound.org, zapsplat.com, mixkit.co (all have royalty-free files)
 */
public class SoundManager {

    private static final String TAG = "SoundManager";

    private final Context context;

    // SoundPool: plays short effects. Max 6 simultaneous streams.
    private SoundPool soundPool;

    // SFX sound IDs (0 = not loaded / not found)
    private int sfxSlide    = 0;
    private int sfxDeath    = 0;
    private int sfxComplete = 0;
    private int sfxClick    = 0;
    private int sfxCrack    = 0;
    private int sfxWind     = 0;

    // MediaPlayer: plays background music (one at a time)
    private MediaPlayer musicPlayer;

    // Currently playing track name (so we don't restart the same track)
    private String currentTrack = "";

    // Master volume toggle
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;

    public SoundManager(Context context) {
        this.context = context;
        buildSoundPool();
        loadSoundEffects();
    }

    /** Set up the SoundPool with AudioAttributes configured for game sounds. */
    private void buildSoundPool() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attrs)
                .build();
    }

    /**
     * Load each SFX by looking up its resource ID dynamically.
     * If a file doesn't exist, getIdentifier() returns 0 and we skip it.
     */
    private void loadSoundEffects() {
        sfxSlide    = loadSound("sfx_slide");
        sfxDeath    = loadSound("sfx_death");
        sfxComplete = loadSound("sfx_complete");
        sfxClick    = loadSound("sfx_click");
        sfxCrack    = loadSound("sfx_crack");
        sfxWind     = loadSound("sfx_wind");
    }

    /**
     * Load a sound from res/raw/ by filename (without extension).
     * Returns the SoundPool sound ID, or 0 if the resource doesn't exist.
     */
    private int loadSound(String filename) {
        int resId = context.getResources().getIdentifier(
                filename, "raw", context.getPackageName());
        if (resId == 0) {
            Log.d(TAG, "Sound not found: " + filename + " (this is OK — add the file to res/raw/)");
            return 0;
        }
        return soundPool.load(context, resId, 1);
    }

    // ------------------------------------------------------------------ SFX

    /** Play the ice-sliding movement sound (looping or one-shot). */
    public void playSlide() { playEffect(sfxSlide, 0.5f); }

    /** Play the player death sound. */
    public void playDeath() { playEffect(sfxDeath, 1.0f); }

    /** Play the level complete fanfare. */
    public void playComplete() { playEffect(sfxComplete, 1.0f); }

    /** Play a UI button click. */
    public void playClick() { playEffect(sfxClick, 0.8f); }

    /** Play the cracking ice sound (Level 4). */
    public void playCrack() { playEffect(sfxCrack, 0.7f); }

    /** Play the wind sound (Level 4). */
    public void playWind() { playEffect(sfxWind, 0.6f); }

    /** Internal helper — plays a sound if it was loaded and sound is enabled. */
    private void playEffect(int soundId, float volume) {
        if (!soundEnabled || soundId == 0) return;
        // play(soundID, leftVolume, rightVolume, priority, loop, rate)
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
    }

    // ------------------------------------------------------------------ Music

    /**
     * Start playing the menu background music.
     * Does nothing if menu music is already playing.
     */
    public void startMenuMusic() {
        startMusic("music_menu");
    }

    /**
     * Start playing the in-game background music.
     * Does nothing if game music is already playing.
     */
    public void startGameMusic() {
        startMusic("music_game");
    }

    /** Internal helper — loads and starts a music track. */
    private void startMusic(String filename) {
        if (!musicEnabled) return;
        if (currentTrack.equals(filename)) return; // already playing this track

        stopMusic(); // stop whatever is currently playing

        int resId = context.getResources().getIdentifier(
                filename, "raw", context.getPackageName());
        if (resId == 0) {
            Log.d(TAG, "Music not found: " + filename + " (add it to res/raw/)");
            return;
        }

        try {
            musicPlayer = MediaPlayer.create(context, resId);
            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(0.5f, 0.5f);
                musicPlayer.start();
                currentTrack = filename;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start music: " + filename, e);
        }
    }

    /** Stop and release the current music player. */
    public void stopMusic() {
        if (musicPlayer != null) {
            try {
                if (musicPlayer.isPlaying()) musicPlayer.stop();
                musicPlayer.release();
            } catch (Exception ignored) {}
            musicPlayer = null;
        }
        currentTrack = "";
    }

    /** Pause music (called when app goes to background). */
    public void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            try { musicPlayer.pause(); } catch (Exception ignored) {}
        }
    }

    /** Resume music (called when app comes back to foreground). */
    public void resumeMusic() {
        if (!musicEnabled) return;
        if (musicPlayer != null && !musicPlayer.isPlaying()) {
            try { musicPlayer.start(); } catch (Exception ignored) {}
        }
    }

    // ------------------------------------------------------------------ lifecycle

    /**
     * Release all audio resources.
     * Call from GameView.surfaceDestroyed() or Activity.onDestroy().
     * Not releasing causes memory leaks in Android.
     */
    public void release() {
        stopMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    // ------------------------------------------------------------------ toggles

    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) pauseMusic();
        else resumeMusic();
    }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
}
