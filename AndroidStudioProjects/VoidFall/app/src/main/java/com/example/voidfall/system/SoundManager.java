package com.example.voidfall.system;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.example.voidfall.R;

/**
 * SoundManager — wraps SoundPool (effects) and MediaPlayer (music).
 *
 * WHY TWO DIFFERENT CLASSES:
 *
 *   SoundPool:
 *   - Loads ALL sounds into RAM on creation.
 *   - Can play multiple sounds simultaneously (important: player hit + fracture at same time).
 *   - Latency: < 20ms — essential for responsive hit feedback.
 *   - Max recommended file size: ~512KB (must be short sounds).
 *   - NOT suitable for music (too large for RAM, streaming not supported).
 *
 *   MediaPlayer:
 *   - Streams audio from storage — no large RAM requirement.
 *   - Designed for one continuous track at a time.
 *   - Latency: ~100-200ms — acceptable for background music that loops.
 *   - Supports pause/resume for app lifecycle management.
 *   - NOT suitable for rapid-fire effects (high setup cost per play).
 *
 * SOUND IDs:
 *   SoundPool.load() returns an integer ID.
 *   We store these IDs and pass them to SoundPool.play() when needed.
 *   -1 means the sound file is missing (graceful no-op).
 *
 * MISSING FILES:
 *   If a sound file doesn't exist in res/raw/, the load() call will throw.
 *   We wrap every load in try/catch to fail gracefully — the game still runs
 *   without sound rather than crashing. See res/raw/README.txt for the
 *   list of required audio files.
 *
 * USAGE FROM GAME:
 *   sound.playPlayerHit();      // SoundPool — immediate
 *   sound.playEnemyDefeat();    // SoundPool — immediate
 *   sound.playFracture();       // SoundPool — immediate
 *   sound.playBossWarning();    // SoundPool — immediate
 *   sound.playGameplayMusic();  // MediaPlayer — starts looping gameplay track
 *   sound.playBossMusic();      // MediaPlayer — switches to boss track
 *   sound.pauseMusic();         // MediaPlayer — pause
 *   sound.resumeMusic();        // MediaPlayer — resume
 *   sound.stopMusic();          // MediaPlayer — stop (level complete/game over)
 *   sound.release();            // Must call in onDestroy to free resources
 */
public class SoundManager {

    private SoundPool soundPool;

    // Sound effect IDs (-1 = not loaded)
    private int sfxPlayerHit    = -1;
    private int sfxEnemyDefeat  = -1;
    private int sfxBossWarning  = -1;
    private int sfxFracture     = -1;
    private int sfxDash         = -1;
    private int sfxAttack       = -1;
    private int sfxShardCollect = -1;
    private int sfxSealActivate = -1;
    private int sfxLevelComplete= -1;

    // Music
    private MediaPlayer musicPlayer;
    private int currentMusicRes = -1; // currently loaded music resource
    private boolean musicPaused = false;

    private final Context context;

    public SoundManager(Context context) {
        this.context = context;

        // Build SoundPool: max 8 simultaneous streams, game audio category
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attrs)
                .build();

        loadSounds();
    }

    /** Load all effect files. Wrapped in try/catch so missing files don't crash. */
    private void loadSounds() {
        sfxPlayerHit    = safeLoad(R.raw.sfx_player_hit);
        sfxEnemyDefeat  = safeLoad(R.raw.sfx_enemy_defeat);
        sfxBossWarning  = safeLoad(R.raw.sfx_boss_warning);
        sfxFracture     = safeLoad(R.raw.sfx_fracture);
        sfxDash         = safeLoad(R.raw.sfx_dash);
        sfxAttack       = safeLoad(R.raw.sfx_attack);
        sfxShardCollect = safeLoad(R.raw.sfx_shard_collect);
        sfxSealActivate = safeLoad(R.raw.sfx_seal_activate);
        sfxLevelComplete= safeLoad(R.raw.sfx_level_complete);
    }

    private int safeLoad(int resId) {
        try {
            return soundPool.load(context, resId, 1);
        } catch (Exception e) {
            // File not found or invalid — return -1 so plays are no-ops
            return -1;
        }
    }

    // ---- Sound effect playback ----

    public void playPlayerHit()    { play(sfxPlayerHit,     1.0f); }
    public void playEnemyDefeat()  { play(sfxEnemyDefeat,   1.0f); }
    public void playBossWarning()  { play(sfxBossWarning,   1.0f); }
    public void playFracture()     { play(sfxFracture,      0.9f); }
    public void playDash()         { play(sfxDash,          0.8f); }
    public void playAttack()       { play(sfxAttack,        0.7f); }
    public void playShardCollect() { play(sfxShardCollect,  1.0f); }
    public void playSealActivate() { play(sfxSealActivate,  1.0f); }
    public void playLevelComplete(){ play(sfxLevelComplete, 1.0f); }

    private void play(int soundId, float volume) {
        if (soundPool != null && soundId != -1) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
            // Parameters: soundId, leftVol, rightVol, priority, loop(-1=infinite/0=once), rate
        }
    }

    // ---- Music playback (MediaPlayer) ----

    public void playGameplayMusic() {
        startMusic(R.raw.music_gameplay);
    }

    public void playBossMusic() {
        startMusic(R.raw.music_boss);
    }

    private void startMusic(int resId) {
        if (currentMusicRes == resId && musicPlayer != null && musicPlayer.isPlaying()) return;
        stopMusic();
        try {
            musicPlayer     = MediaPlayer.create(context, resId);
            currentMusicRes = resId;
            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(0.6f, 0.6f);
                musicPlayer.start();
                musicPaused = false;
            }
        } catch (Exception e) {
            // Music file missing — continue without music
            musicPlayer = null;
        }
    }

    public void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
            musicPaused = true;
        }
    }

    public void resumeMusic() {
        if (musicPlayer != null && musicPaused) {
            musicPlayer.start();
            musicPaused = false;
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
            musicPlayer     = null;
            currentMusicRes = -1;
            musicPaused     = false;
        }
    }

    /**
     * Release all SoundPool and MediaPlayer resources.
     * Must be called in Activity.onDestroy() to prevent memory leaks.
     * After release(), this object should not be used.
     */
    public void release() {
        stopMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
