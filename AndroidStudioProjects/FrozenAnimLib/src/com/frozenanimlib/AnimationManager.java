package com.frozenanimlib;

import java.util.HashMap;
import java.util.Map;

/**
 * AnimationManager - central registry for all named animations.
 *
 * PURPOSE:
 *   Instead of keeping individual animation references scattered across the game,
 *   the game creates ONE AnimationManager and stores every animation in it under a
 *   string name.  Any part of the code can then call:
 *       animManager.update(dt)          ← advance all animations every frame
 *       animManager.getPulse("goal")    ← read a specific animation's value
 *
 * PATTERN:
 *   This is called a "registry" or "service locator" pattern.
 *   It avoids passing many animation objects through every method.
 *
 * THREAD SAFETY:
 *   This class is NOT thread-safe on its own.
 *   In our game, all animation reads happen on the game thread (inside draw/update),
 *   so no synchronisation is needed.
 */
public class AnimationManager {

    // Internal storage: animation name → Animatable instance
    private final Map<String, Animatable> animations = new HashMap<>();

    // ------------------------------------------------------------------ register

    /** Register any Animatable under a unique name. Overwrites any previous entry. */
    public void register(String name, Animatable anim) {
        animations.put(name, anim);
    }

    /** Convenience: register a PulseAnimation. */
    public PulseAnimation registerPulse(String name, float minScale, float maxScale,
                                        float period, boolean looping) {
        PulseAnimation p = new PulseAnimation(minScale, maxScale, period, looping);
        animations.put(name, p);
        return p;
    }

    /** Convenience: register a FadeAnimation. */
    public FadeAnimation registerFade(String name, float startAlpha, float endAlpha,
                                      float duration, boolean looping) {
        FadeAnimation f = new FadeAnimation(startAlpha, endAlpha, duration, looping);
        animations.put(name, f);
        return f;
    }

    /** Convenience: register a BounceAnimation. */
    public BounceAnimation registerBounce(String name, float maxHeight,
                                          float period, int bounces) {
        BounceAnimation b = new BounceAnimation(maxHeight, period, bounces);
        animations.put(name, b);
        return b;
    }

    /** Convenience: register a ShakeAnimation. */
    public ShakeAnimation registerShake(String name, float magnitude,
                                        float duration, float interval) {
        ShakeAnimation s = new ShakeAnimation(magnitude, duration, interval);
        animations.put(name, s);
        return s;
    }

    /** Convenience: register a FloatRiseAnimation. */
    public FloatRiseAnimation registerFloatRise(String name, float riseHeight,
                                                float duration, boolean looping) {
        FloatRiseAnimation f = new FloatRiseAnimation(riseHeight, duration, looping);
        animations.put(name, f);
        return f;
    }

    /** Convenience: register a FrameSequenceAnimation. */
    public FrameSequenceAnimation registerFrameSequence(String name, int frameCount,
                                                        float frameDuration, boolean looping) {
        FrameSequenceAnimation fs = new FrameSequenceAnimation(frameCount, frameDuration, looping);
        animations.put(name, fs);
        return fs;
    }

    // ------------------------------------------------------------------ retrieve

    /** Returns any Animatable by name, or null if not found. */
    public Animatable get(String name) {
        return animations.get(name);
    }

    /** Returns a PulseAnimation by name, or null if not found or wrong type. */
    public PulseAnimation getPulse(String name) {
        Animatable a = animations.get(name);
        return (a instanceof PulseAnimation) ? (PulseAnimation) a : null;
    }

    /** Returns a FadeAnimation by name, or null if not found or wrong type. */
    public FadeAnimation getFade(String name) {
        Animatable a = animations.get(name);
        return (a instanceof FadeAnimation) ? (FadeAnimation) a : null;
    }

    /** Returns a BounceAnimation by name, or null if not found or wrong type. */
    public BounceAnimation getBounce(String name) {
        Animatable a = animations.get(name);
        return (a instanceof BounceAnimation) ? (BounceAnimation) a : null;
    }

    /** Returns a ShakeAnimation by name, or null if not found or wrong type. */
    public ShakeAnimation getShake(String name) {
        Animatable a = animations.get(name);
        return (a instanceof ShakeAnimation) ? (ShakeAnimation) a : null;
    }

    /** Returns a FloatRiseAnimation by name, or null if not found or wrong type. */
    public FloatRiseAnimation getFloatRise(String name) {
        Animatable a = animations.get(name);
        return (a instanceof FloatRiseAnimation) ? (FloatRiseAnimation) a : null;
    }

    /** Returns a FrameSequenceAnimation by name, or null if not found or wrong type. */
    public FrameSequenceAnimation getFrameSequence(String name) {
        Animatable a = animations.get(name);
        return (a instanceof FrameSequenceAnimation) ? (FrameSequenceAnimation) a : null;
    }

    // ------------------------------------------------------------------ lifecycle

    /**
     * Advance ALL registered animations by deltaSeconds.
     * Called once per game frame from GameView.update().
     */
    public void updateAll(float deltaSeconds) {
        for (Animatable anim : animations.values()) {
            anim.update(deltaSeconds);
        }
    }

    /** Reset a single named animation. */
    public void reset(String name) {
        Animatable a = animations.get(name);
        if (a != null) a.reset();
    }

    /** Reset ALL animations (e.g. when restarting a level). */
    public void resetAll() {
        for (Animatable anim : animations.values()) {
            anim.reset();
        }
    }

    /** Remove a named animation from the registry. */
    public void unregister(String name) {
        animations.remove(name);
    }

    /** Remove all animations. */
    public void clear() {
        animations.clear();
    }
}
