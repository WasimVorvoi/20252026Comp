package com.example.voidfall.arena;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * FracturePiece — one tile/chunk of the arena.
 *
 * WHAT IT IS:
 *   The arena is divided into a grid of FracturePiece objects.
 *   Initially they tile perfectly with no gaps.
 *   As the level progresses, pieces drift, rotate, and separate — creating gaps
 *   that both the player and enemies must navigate.
 *
 * STATE:
 *   active = true  → piece is part of the arena
 *   active = false → piece has fallen off-screen and should be removed
 *
 * DRAWING:
 *   Each piece draws itself as a filled quadrilateral with:
 *     - A gradient fill (dark navy → slightly lighter navy) for depth
 *     - Bright cyan edge lines to suggest a glowing fractured edge
 *     - Interior crack lines (drawn as Path segments) that appear over time
 *
 * PHYSICS (simple):
 *   velX, velY — drift velocity in pixels/second
 *   rotationVel  — rotation speed in degrees/second
 *   These are set by ArenaManager when a fracture event occurs.
 */
public class FracturePiece {

    // Position of the top-left corner (world/screen space)
    private float x, y;
    // Dimensions
    private float width, height;
    // Drift velocity (pixels per second)
    private float velX, velY;
    // Rotation state (degrees)
    private float rotation;
    private float rotationVel;      // degrees per second
    // How cracked this piece looks: 0=pristine, 1=shattered
    private float crackIntensity;
    // Colour theme index (0=base navy, 1=purple-navy, 2=teal-navy)
    private final int colorIndex;

    private boolean active = true;

    // Screen bounds — piece deactivates when it drifts fully off screen
    private final float screenW, screenH;

    // Reusable paints — created once per piece, not every frame
    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crackPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // The crack path is rebuilt when crackIntensity changes
    private final Path crackPath = new Path();
    private float lastCrackIntensity = -1f;

    // Base colours for each theme index
    private static final int[] BASE_COLORS = {
        Color.rgb(26, 26, 46),   // 0: deep navy
        Color.rgb(30, 15, 50),   // 1: purple-navy
        Color.rgb(10, 30, 46),   // 2: teal-navy
    };
    private static final int[] EDGE_COLORS = {
        Color.rgb(0, 212, 255),  // 0: cyan
        Color.rgb(150, 100, 255),// 1: violet
        Color.rgb(0, 255, 180),  // 2: teal
    };

    public FracturePiece(float x, float y, float width, float height,
                         int colorIndex, float screenW, float screenH) {
        this.x          = x;
        this.y          = y;
        this.width      = width;
        this.height     = height;
        this.colorIndex = colorIndex % 3;
        this.screenW    = screenW;
        this.screenH    = screenH;
        this.rotation   = 0f;
        this.crackIntensity = 0f;

        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(2.5f);
        edgePaint.setColor(EDGE_COLORS[this.colorIndex]);

        crackPaint.setStyle(Paint.Style.STROKE);
        crackPaint.setStrokeWidth(1.5f);
        crackPaint.setColor(Color.argb(180, 0, 212, 255));

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(BASE_COLORS[this.colorIndex]);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Move this piece according to its drift velocity and rotation.
     * Deactivate when it has drifted off all four screen edges.
     */
    public void update(float dt) {
        if (!active) return;

        x        += velX * dt;
        y        += velY * dt;
        rotation += rotationVel * dt;

        // Deactivate if fully off screen (add generous margin for rotation)
        float margin = Math.max(width, height) * 1.5f;
        if (x + margin < 0 || x - margin > screenW ||
            y + margin < 0 || y - margin > screenH) {
            active = false;
        }
    }

    // -------------------------------------------------------------------------
    // Draw
    // -------------------------------------------------------------------------

    public void draw(Canvas canvas) {
        if (!active) return;

        float cx = x + width / 2f;
        float cy = y + height / 2f;

        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(rotation);
        canvas.translate(-width / 2f, -height / 2f);

        // Fill
        canvas.drawRect(0, 0, width, height, fillPaint);

        // Glowing edge border
        canvas.drawRect(0, 0, width, height, edgePaint);

        // Crack lines (only rebuild path when intensity changes)
        if (crackIntensity > 0.05f) {
            if (Math.abs(crackIntensity - lastCrackIntensity) > 0.05f) {
                rebuildCrackPath();
                lastCrackIntensity = crackIntensity;
            }
            canvas.drawPath(crackPath, crackPaint);
        }

        canvas.restore();
    }

    /**
     * Build the crack path based on current crackIntensity.
     * Cracks radiate from the centre toward the edges like a broken pane of glass.
     * The path is deterministic (no random) so it doesn't flicker every frame.
     */
    private void rebuildCrackPath() {
        crackPath.reset();
        int numCracks = (int)(crackIntensity * 6) + 1; // 1 to 7 cracks
        float cx = width / 2f;
        float cy = height / 2f;

        for (int i = 0; i < numCracks; i++) {
            // Evenly-spaced angles, slightly offset by crack index for variety
            double angle = (Math.PI * 2 / numCracks) * i + (i * 0.37);
            float reach  = crackIntensity * Math.min(width, height) * 0.6f;
            float ex     = cx + (float)(Math.cos(angle) * reach);
            float ey     = cy + (float)(Math.sin(angle) * reach);

            crackPath.moveTo(cx, cy);
            // Add a mid-point deflection to make the crack look jagged
            float mx = (cx + ex) / 2f + (i % 2 == 0 ? 8f : -8f);
            float my = (cy + ey) / 2f + (i % 3 == 0 ? -6f : 6f);
            crackPath.quadTo(mx, my, ex, ey);
        }
    }

    // -------------------------------------------------------------------------
    // Setters used by ArenaManager
    // -------------------------------------------------------------------------

    public void setVelocity(float vx, float vy)     { this.velX = vx; this.velY = vy; }
    public void setRotationVel(float rv)             { this.rotationVel = rv; }
    public void setCrackIntensity(float intensity)   { this.crackIntensity = Math.max(0f, Math.min(1f, intensity)); }
    public void addCrackIntensity(float delta)       { setCrackIntensity(crackIntensity + delta); }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Axis-aligned bounding rect of this piece (ignores rotation for simplicity). */
    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }
    public float getCenterX(){ return x + width / 2f; }
    public float getCenterY(){ return y + height / 2f; }
    public float getTop()    { return y; }
    public float getBottom() { return y + height; }
    public boolean isActive(){ return active; }
    public float getCrackIntensity() { return crackIntensity; }

    /** Move the piece by a delta (used by ArenaManager for tilt stretching). */
    public void translate(float dx, float dy) { x += dx; y += dy; }

    /** Directly set position (used by ArenaManager on reset). */
    public void setPosition(float nx, float ny) { x = nx; y = ny; }
}
