package com.example.voidfall.arena;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ArenaManager — owns and manages all FracturePiece objects.
 *
 * THE KEY MECHANIC:
 *   ArenaManager translates tilt input into visual and gameplay changes:
 *
 *   tiltX (left/right):
 *     → Each column of pieces moves away from or toward the centre.
 *       Tilting right pushes the right column right and left column left
 *       (the arena "stretches"). Tilting left compresses them inward.
 *     → The player and enemies stand on these pieces, so as pieces move,
 *       it changes the layout of the battlefield.
 *
 *   tiltY (forward/back):
 *     → Tilting backward (negative tiltY) builds up "fracture energy".
 *       When enough builds up, a FractureEvent fires:
 *         - A random piece gains crack intensity
 *         - It may split into two smaller pieces
 *         - A brief cooldown prevents continuous fracturing
 *
 * FRACTURE EVENTS:
 *   triggerFracture(count) is also called by bosses (Shatter King).
 *   consumeFractureEvent() is checked by GameView to trigger screen shake + sound.
 *
 * PIECE GRID:
 *   On reset(), the arena is rebuilt as a COLS × ROWS grid of tiles.
 *   Each tile has a small gutter gap (GUTTER) between it and its neighbour.
 *   The arena sits vertically centred and fills most of the screen width.
 *
 * COLLISION INTERFACE:
 *   isOnAnyPiece(bounds) — true if the bounding rect overlaps any active piece's top surface.
 *   getGroundY(bounds)   — returns the Y coordinate of the ground directly below the entity.
 *   isInGap(x, y)        — true if position (x,y) is not above any active piece (it's a gap).
 */
public class ArenaManager {

    // Grid dimensions
    private static final int   COLS   = 3;
    private static final int   ROWS   = 4;
    private static final float GUTTER = 8f; // pixels between tiles

    // Tilt response constants
    private static final float STRETCH_SPEED      = 180f; // pixels/second of stretch per unit tilt
    private static final float FRACTURE_CHARGE_RATE= 0.9f; // charge units per second per unit tilt
    private static final float FRACTURE_THRESHOLD  = 1.0f; // charge needed to trigger a fracture
    private static final float FRACTURE_COOLDOWN   = 2.0f; // seconds before next tilt-fracture

    // Arena vertical extent (as fraction of screen height)
    private static final float ARENA_TOP_FRAC    = 0.18f;  // top of arena at 18% from screen top
    private static final float ARENA_BOTTOM_FRAC = 0.88f;  // bottom of arena at 88%

    private final List<FracturePiece> pieces = new ArrayList<>();

    // Baseline positions for each piece (used to compute stretch displacement)
    private final float[] baseCenterX;
    private final float[] baseCenterY;

    // Current horizontal stretch offsets per column (columns 0=left,1=mid,2=right)
    private float[] columnOffset; // pixels added to each column's base X

    // Fracture state
    private float fractureCharge   = 0f;
    private float fractureCooldown = 0f;
    private boolean fractureEventPending = false; // consumed by GameView

    // Screen dimensions (set on reset/init)
    private float screenW, screenH;

    // Reusable paint for ambient glow lines between pieces
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ArenaManager(float screenW, float screenH) {
        baseCenterX  = new float[COLS * ROWS];
        baseCenterY  = new float[COLS * ROWS];
        columnOffset = new float[COLS];
        glowPaint.setColor(Color.argb(60, 0, 212, 255));
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(1f);
        reset(screenW, screenH);
    }

    // -------------------------------------------------------------------------
    // Reset / Init
    // -------------------------------------------------------------------------

    /**
     * Rebuild the arena as a clean COLS×ROWS grid.
     * Called at the start of each level.
     */
    public void reset(float screenW, float screenH) {
        this.screenW = screenW;
        this.screenH = screenH;

        pieces.clear();
        fractureCharge   = 0f;
        fractureCooldown = 0f;

        for (int i = 0; i < COLS; i++) columnOffset[i] = 0f;

        float arenaTop    = screenH * ARENA_TOP_FRAC;
        float arenaBottom = screenH * ARENA_BOTTOM_FRAC;
        float arenaHeight = arenaBottom - arenaTop;
        float tileW       = (screenW - GUTTER * (COLS + 1)) / COLS;
        float tileH       = (arenaHeight - GUTTER * (ROWS + 1)) / ROWS;

        int idx = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                float tx = GUTTER + col * (tileW + GUTTER);
                float ty = arenaTop + GUTTER + row * (tileH + GUTTER);
                FracturePiece piece = new FracturePiece(tx, ty, tileW, tileH,
                        col % 3, screenW, screenH);
                pieces.add(piece);
                baseCenterX[idx] = tx + tileW / 2f;
                baseCenterY[idx] = ty + tileH / 2f;
                idx++;
            }
        }
    }

    /**
     * Pre-crack the arena at level start.
     * Called with the level's initialFractureCount.
     * Each call triggers one fracture event without needing tilt input.
     */
    public void setFractureLevel(int count) {
        for (int i = 0; i < count; i++) {
            triggerFracture(1);
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    public void update(float dt, float tiltX, float tiltY) {
        // 1. Cooldown timer
        if (fractureCooldown > 0f) fractureCooldown -= dt;

        // 2. Horizontal stretch — tiltX moves columns apart/together
        applyColumnStretch(dt, tiltX);

        // 3. Fracture charge from backward tilt (tiltY < 0 means tilting backward)
        if (tiltY < -0.4f) {
            float chargeRate = (-tiltY - 0.4f) * FRACTURE_CHARGE_RATE;
            fractureCharge += chargeRate * dt;

            if (fractureCharge >= FRACTURE_THRESHOLD && fractureCooldown <= 0f) {
                triggerFracture(1);
                fractureCharge   = 0f;
                fractureCooldown = FRACTURE_COOLDOWN;
            }
        } else {
            // Charge decays when not holding back tilt
            fractureCharge = Math.max(0f, fractureCharge - dt * 0.5f);
        }

        // 4. Update each piece's drift
        Iterator<FracturePiece> iter = pieces.iterator();
        while (iter.hasNext()) {
            FracturePiece p = iter.next();
            p.update(dt);
            if (!p.isActive()) iter.remove();
        }
    }

    /**
     * Apply horizontal stretch: each column moves by an amount proportional
     * to its distance from the centre column.
     * Column 0 (left) moves in the opposite direction of column 2 (right).
     * Column 1 (middle) moves very little.
     *
     * columnOffset[0] = leftward when tiltX > 0 (stretching)
     * columnOffset[2] = rightward when tiltX > 0
     */
    private void applyColumnStretch(float dt, float tiltX) {
        float targetLeft  = -tiltX * screenW * 0.10f; // max 10% of screen width
        float targetRight =  tiltX * screenW * 0.10f;
        float targetMid   =  0f;

        // Smoothly interpolate column offsets toward target
        columnOffset[0] += (targetLeft  - columnOffset[0]) * 5f * dt;
        columnOffset[1] += (targetMid   - columnOffset[1]) * 5f * dt;
        columnOffset[2] += (targetRight - columnOffset[2]) * 5f * dt;

        // Apply offsets to each piece based on its column index
        int idx = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (idx >= pieces.size()) break;
                FracturePiece p = pieces.get(idx);
                if (p.isActive()) {
                    // Target X = base centre X + column offset - half width
                    float targetX = baseCenterX[idx] + columnOffset[col] - p.getWidth() / 2f;
                    // Snap to target (stretch is driven by us, not physics)
                    p.setPosition(targetX, p.getY());
                }
                idx++;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Fracture Events
    // -------------------------------------------------------------------------

    /**
     * Trigger `count` fracture events.
     * Each event: picks a heavily-cracked or random active piece, increases
     * its crack intensity, and if above threshold gives it a drift velocity
     * so it floats away from its position.
     * Called by tilt threshold, bosses, and setFractureLevel().
     */
    public void triggerFracture(int count) {
        List<FracturePiece> active = getActivePieces();
        if (active.isEmpty()) return;

        for (int i = 0; i < count; i++) {
            // Prefer the most-cracked piece; fall back to a random one
            FracturePiece target = getMostCrackedPiece(active);
            if (target == null) target = active.get((int)(Math.random() * active.size()));

            target.addCrackIntensity(0.35f);

            // Once crack intensity exceeds 0.7, the piece starts drifting
            if (target.getCrackIntensity() >= 0.70f) {
                float angle = (float)(Math.random() * Math.PI * 2);
                float speed = 30f + (float)(Math.random() * 50f);
                target.setVelocity(
                    (float)Math.cos(angle) * speed,
                    (float)Math.sin(angle) * speed + 20f // slight downward bias
                );
                target.setRotationVel((float)(Math.random() * 30 - 15));
                active.remove(target); // don't fracture the same piece twice per event
            }
        }

        fractureEventPending = true;
    }

    /**
     * Returns true once after a fracture event occurred (then resets the flag).
     * GameView checks this each frame to trigger screen shake + sound.
     */
    public boolean consumeFractureEvent() {
        if (fractureEventPending) {
            fractureEventPending = false;
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Collision queries
    // -------------------------------------------------------------------------

    /**
     * Returns the Y coordinate of the top surface directly below the given
     * bottom edge. Used by Player/Enemy to stand on the arena.
     * Returns screenH (off the bottom) if nothing is below.
     */
    public float getGroundY(float entityLeft, float entityRight, float entityBottom) {
        float best = screenH + 1000f;
        for (FracturePiece p : pieces) {
            if (!p.isActive()) continue;
            RectF b = p.getBounds();
            // The piece must overlap the entity's horizontal span
            if (b.right < entityLeft || b.left > entityRight) continue;
            // The piece's top must be below the entity's bottom (or very close)
            if (b.top >= entityBottom - 5f && b.top < best) {
                best = b.top;
            }
        }
        return best;
    }

    /**
     * Returns true if the given rect overlaps the top surface of any active piece.
     * Used by CollisionManager to decide if an entity is "grounded."
     */
    public boolean isOnAnyPiece(RectF entityBounds) {
        for (FracturePiece p : pieces) {
            if (!p.isActive()) continue;
            RectF b = p.getBounds();
            // Check horizontal overlap and that entity's bottom is near the piece's top
            boolean hOverlap = entityBounds.right > b.left && entityBounds.left < b.right;
            boolean vTouch   = Math.abs(entityBounds.bottom - b.top) < 12f;
            if (hOverlap && vTouch) return true;
        }
        return false;
    }

    /**
     * Returns true if position (x, y) is not directly above any active piece
     * — i.e., it is above a gap. Used by ObjectiveManager (knock-into-gap objective)
     * and by entities to detect if they are falling.
     */
    public boolean isInGap(float x, float y) {
        for (FracturePiece p : pieces) {
            if (!p.isActive()) continue;
            RectF b = p.getBounds();
            if (x >= b.left && x <= b.right && y >= b.top && y <= b.bottom) return false;
        }
        return true;
    }

    /**
     * Returns all pieces that lie in the vertical column containing x.
     * Used by ObjectiveManager to place shards and seals on specific columns.
     */
    public List<FracturePiece> getPiecesInColumn(float x) {
        List<FracturePiece> result = new ArrayList<>();
        for (FracturePiece p : pieces) {
            if (!p.isActive()) continue;
            RectF b = p.getBounds();
            if (x >= b.left && x <= b.right) result.add(p);
        }
        return result;
    }

    /** Returns the full list of all currently active pieces. */
    public List<FracturePiece> getActivePieces() {
        List<FracturePiece> result = new ArrayList<>();
        for (FracturePiece p : pieces) {
            if (p.isActive()) result.add(p);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Draw
    // -------------------------------------------------------------------------

    public void draw(Canvas canvas) {
        for (FracturePiece p : pieces) {
            p.draw(canvas);
        }
        // Draw faint glow in gaps between active pieces
        drawGapGlow(canvas);
    }

    /**
     * Draw a faint ambient cyan glow at the gap between each pair of horizontally
     * adjacent pieces. This makes the gaps feel dangerous and intentional —
     * not just "empty background."
     */
    private void drawGapGlow(Canvas canvas) {
        List<FracturePiece> active = getActivePieces();
        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                FracturePiece a = active.get(i);
                FracturePiece b = active.get(j);
                RectF ra = a.getBounds();
                RectF rb = b.getBounds();
                // Only draw for pieces that are horizontally adjacent (small gap)
                float gapX = Math.max(ra.left, rb.left) - Math.min(ra.right, rb.right);
                if (gapX > 0 && gapX < screenW * 0.15f) {
                    float overlapTop    = Math.max(ra.top, rb.top);
                    float overlapBottom = Math.min(ra.bottom, rb.bottom);
                    if (overlapBottom > overlapTop) {
                        float midX = (Math.min(ra.right, rb.right) + Math.max(ra.left, rb.left)) / 2f;
                        canvas.drawLine(midX, overlapTop, midX, overlapBottom, glowPaint);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private FracturePiece getMostCrackedPiece(List<FracturePiece> candidates) {
        FracturePiece best = null;
        float maxCrack = 0f;
        for (FracturePiece p : candidates) {
            if (p.getCrackIntensity() > maxCrack && p.getCrackIntensity() < 0.70f) {
                maxCrack = p.getCrackIntensity();
                best     = p;
            }
        }
        return best;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public float getScreenW()          { return screenW; }
    public float getScreenH()          { return screenH; }
    public float getFractureCharge()   { return fractureCharge; }  // 0-1, for HUD tilt bar
    public float getFractureThreshold(){ return FRACTURE_THRESHOLD; }
    public List<FracturePiece> getAllPieces() { return pieces; }
}
