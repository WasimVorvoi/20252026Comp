package com.frozenanimlib;

/**
 * Animatable interface - the contract for every animation in this library.
 *
 * Every animation must be able to:
 *   1. advance its internal timer (update)
 *   2. report whether it is still running (isRunning)
 *   3. report whether it repeats forever (isLooping)
 *   4. reset itself to the beginning (reset)
 *
 * This interface does NOT depend on Android at all - it is pure Java.
 * That is why this library can be compiled into a plain .jar file and
 * imported into any Java or Android project.
 */
public interface Animatable {

    /**
     * Advance the animation by deltaSeconds seconds.
     * Called once per game frame from AnimationManager.updateAll().
     *
     * @param deltaSeconds time elapsed since the last frame (e.g. 0.016 for 60 fps)
     */
    void update(float deltaSeconds);

    /**
     * Returns true while the animation has not yet finished.
     * A looping animation always returns true.
     */
    boolean isRunning();

    /**
     * Returns true if the animation loops indefinitely.
     */
    boolean isLooping();

    /**
     * Resets all internal state so the animation can play again from the start.
     */
    void reset();
}
