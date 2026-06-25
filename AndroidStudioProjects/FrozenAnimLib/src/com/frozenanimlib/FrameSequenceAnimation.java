package com.frozenanimlib;

/**
 * FrameSequenceAnimation - cycles through a numbered sequence of sprite frames.
 *
 * HOW IT WORKS:
 *   The animation has N frames numbered 0 to (frameCount - 1).
 *   Each frame is held for (frameDuration) seconds, then the index advances.
 *   The caller asks getCurrentFrameIndex() to know which bitmap to draw.
 *
 *   This class does NOT load or hold bitmaps — it only manages timing and
 *   frame indices. This keeps the library pure Java with no Android dependency.
 *
 * FRAME INDEX FORMULA:
 *   frameIndex = (int)(time / frameDuration) % frameCount
 *   e.g. 8 frames at 0.1s each → full cycle = 0.8 seconds
 *
 * PLAYBACK MODES:
 *   looping = true  → plays frames 0→N-1 → 0→N-1 → ... forever
 *   looping = false → plays frames 0→N-1 once, then stays on last frame
 *
 * USED IN GAME FOR:
 *   - Player attack animation (4-frame slash)
 *   - Boss special ability frames (8-frame charge-up glow)
 *   - Fracture crack spreading effect (5-frame crack animation)
 *   - Echo shadow flicker (3-frame ghost flicker)
 *   - Level complete celebration (6-frame burst)
 *
 * CALLER USAGE:
 *   // During setup:
 *   FrameSequenceAnimation attackAnim = new FrameSequenceAnimation(4, 0.08f, false);
 *
 *   // During draw:
 *   int frameIdx = attackAnim.getCurrentFrameIndex();
 *   Bitmap frame = attackFrames[frameIdx];  // caller's bitmap array
 *   canvas.drawBitmap(frame, x, y, paint);
 *
 * HOW TO PREPARE BITMAPS IN ANDROID:
 *   Store frames as: res/drawable/player_attack_0.png, player_attack_1.png, ...
 *   Load them into an array: Bitmap[] frames = new Bitmap[4];
 *   for (int i = 0; i < 4; i++) {
 *       int id = context.getResources().getIdentifier(
 *           "player_attack_" + i, "drawable", context.getPackageName());
 *       frames[i] = BitmapFactory.decodeResource(context.getResources(), id);
 *   }
 *   Then pass frames[attackAnim.getCurrentFrameIndex()] to drawBitmap each frame.
 */
public class FrameSequenceAnimation implements Animatable {

    private final int   frameCount;      // total number of frames (e.g. 4)
    private final float frameDuration;   // seconds per frame (e.g. 0.1f)
    private final boolean looping;

    private float time;
    private boolean running;

    /**
     * Create a frame sequence animation.
     *
     * @param frameCount    total frames in the sequence (must be >= 1)
     * @param frameDuration seconds to display each frame (e.g. 0.08f for snappy attack)
     * @param looping       true = loops forever (idle animation), false = one-shot (attack)
     */
    public FrameSequenceAnimation(int frameCount, float frameDuration, boolean looping) {
        if (frameCount < 1) throw new IllegalArgumentException("frameCount must be >= 1");
        if (frameDuration <= 0f) throw new IllegalArgumentException("frameDuration must be > 0");
        this.frameCount    = frameCount;
        this.frameDuration = frameDuration;
        this.looping       = looping;
        reset();
    }

    /** Convenience: fast 4-frame attack (non-looping). */
    public static FrameSequenceAnimation attack(int frames) {
        return new FrameSequenceAnimation(frames, 0.07f, false);
    }

    /** Convenience: idle breathing loop (looping). */
    public static FrameSequenceAnimation idle(int frames) {
        return new FrameSequenceAnimation(frames, 0.15f, true);
    }

    @Override
    public void update(float deltaSeconds) {
        if (!running) return;

        time += deltaSeconds;

        if (!looping && time >= frameCount * frameDuration) {
            time    = (frameCount - 1) * frameDuration; // clamp on last frame
            running = false;
        }
    }

    /**
     * Returns the index of the frame that should be drawn right now.
     * Range: [0, frameCount - 1]
     *
     * Use this to index into a Bitmap array in the Android drawing code.
     */
    public int getCurrentFrameIndex() {
        if (!running) return frameCount - 1; // frozen on last frame when done
        int idx = (int)(time / frameDuration);
        if (looping) {
            idx = idx % frameCount;
        } else {
            idx = Math.min(idx, frameCount - 1);
        }
        return idx;
    }

    /**
     * Returns a progress value in [0.0, 1.0] across the whole animation.
     * Useful for triggering events at specific points (e.g. "hit lands at 60%").
     */
    public float getProgress() {
        float totalDuration = frameCount * frameDuration;
        if (totalDuration <= 0f) return 1f;
        return Math.min(time / totalDuration, 1f);
    }

    /** Returns the total animation duration in seconds. */
    public float getTotalDuration() {
        return frameCount * frameDuration;
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isLooping() { return looping; }

    @Override
    public void reset() {
        time    = 0f;
        running = true;
    }
}
