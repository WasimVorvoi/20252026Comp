package com.example.project;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Hazard - a moving obstacle that can kill the player on contact.
 *
 * There are two hazard types, each with distinct movement:
 *
 *   FALLING_ICICLE:
 *     Starts at the top of the screen and falls downward at a constant speed.
 *     When it exits the bottom of the level, it resets to the top.
 *     The player must cross corridors while watching for falling icicles.
 *
 *   SLIDING_BLOCKER:
 *     Moves horizontally (or vertically) back and forth between two boundary
 *     positions. It reverses direction when it hits either boundary.
 *     The player must time their movement to avoid the blocker.
 *
 * COLLISION:
 *   Both types use Axis-Aligned Bounding Box (AABB) collision — we check if the
 *   player's circular bounding box overlaps the hazard's rectangular bounding box.
 */
public class Hazard {

    /** The two types of moving hazard. */
    public enum HazardType {
        FALLING_ICICLE,  // falls from top to bottom, loops
        SLIDING_BLOCKER  // slides back and forth in a corridor
    }

    // ---- type and position ----
    public final HazardType type;
    public float x, y;          // top-left corner of the hazard's bounding box
    public float width, height; // size of the hazard

    // ---- movement parameters ----
    private float speedX, speedY;     // current velocity direction (pixels/second)
    private float minBound, maxBound; // movement boundary (X for slider, Y for icicle)
    private final boolean moveHorizontal; // true = slider moves left-right, false = up-down

    // ---- reset position (for icicles looping back to top) ----
    private final float resetY;    // Y position to reset to when falling off bottom
    private final float levelH;    // total level pixel height (to detect off-screen)

    // ---- paint ----
    private static final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Create a FALLING_ICICLE hazard.
     *
     * @param x         horizontal centre of the icicle (pixels)
     * @param startY    starting Y (top of the icicle, pixels)
     * @param size      width and size of the icicle
     * @param speed     fall speed in pixels/second (positive = downward)
     * @param levelH    total level height in pixels (to know when to reset)
     */
    public static Hazard createIcicle(float x, float startY, float size,
                                       float speed, float levelH) {
        return new Hazard(HazardType.FALLING_ICICLE,
                x - size / 2f, startY, size, size * 1.5f,
                0f, speed,
                startY, 0f, levelH,   // minBound=0 unused for icicles
                false, levelH);
    }

    /**
     * Create a SLIDING_BLOCKER hazard that moves horizontally.
     *
     * @param startX    starting X (left edge)
     * @param y         vertical position (fixed)
     * @param w         width of the blocker
     * @param h         height of the blocker
     * @param speed     horizontal speed in pixels/second (positive = right)
     * @param minX      left boundary (blocker bounces here)
     * @param maxX      right boundary (blocker bounces here)
     */
    public static Hazard createSlider(float startX, float y, float w, float h,
                                       float speed, float minX, float maxX) {
        return new Hazard(HazardType.SLIDING_BLOCKER,
                startX, y, w, h,
                speed, 0f,
                0f, minX, maxX,
                true, 0f);
    }

    // private constructor — use factory methods above
    private Hazard(HazardType type, float x, float y, float w, float h,
                   float speedX, float speedY,
                   float resetY, float minBound, float maxBound,
                   boolean moveHorizontal, float levelH) {
        this.type           = type;
        this.x              = x;
        this.y              = y;
        this.width          = w;
        this.height         = h;
        this.speedX         = speedX;
        this.speedY         = speedY;
        this.resetY         = resetY;
        this.minBound       = minBound;
        this.maxBound       = maxBound;
        this.moveHorizontal = moveHorizontal;
        this.levelH         = levelH;
    }

    /**
     * Advance hazard movement by dt seconds.
     * Called each frame from GameView.update().
     */
    public void update(float dt) {
        if (type == HazardType.FALLING_ICICLE) {
            y += speedY * dt;
            // Reset to top when icicle falls below the level
            if (y > levelH) {
                y = resetY - height; // start just above the level top
            }
        } else { // SLIDING_BLOCKER
            if (moveHorizontal) {
                x += speedX * dt;
                // Bounce at boundaries
                if (x < minBound) {
                    x       = minBound;
                    speedX  = Math.abs(speedX);  // reverse to rightward
                } else if (x + width > maxBound) {
                    x       = maxBound - width;
                    speedX  = -Math.abs(speedX); // reverse to leftward
                }
            } else {
                y += speedY * dt;
                if (y < minBound) {
                    y       = minBound;
                    speedY  = Math.abs(speedY);
                } else if (y + height > maxBound) {
                    y       = maxBound - height;
                    speedY  = -Math.abs(speedY);
                }
            }
        }
    }

    /**
     * Returns the bounding rectangle used for collision detection.
     * AABB = Axis-Aligned Bounding Box.
     */
    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    /**
     * Returns true if the player circle (centre px, py with radius r) overlaps
     * this hazard's bounding box.
     *
     * Method: find the closest point on the rectangle to the circle centre,
     * then check if the distance to that point is less than the radius.
     */
    public boolean collidesWithPlayer(float px, float py, float radius) {
        // Closest point on the AABB to the circle centre
        float closestX = Math.max(x, Math.min(px, x + width));
        float closestY = Math.max(y, Math.min(py, y + height));

        float dx = px - closestX;
        float dy = py - closestY;
        return (dx * dx + dy * dy) < (radius * radius);
    }

    /** Draw the hazard on the canvas. */
    public void draw(Canvas canvas) {
        if (type == HazardType.FALLING_ICICLE) {
            drawIcicle(canvas);
        } else {
            drawSlider(canvas);
        }
    }

    private void drawIcicle(Canvas canvas) {
        // Main icicle body: tapered triangle pointing downward
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(180, 220, 250));

        float cx = x + width / 2f;
        Path path = new Path();
        path.moveTo(x, y);                    // top-left
        path.lineTo(x + width, y);            // top-right
        path.lineTo(cx, y + height);          // bottom tip
        path.close();
        canvas.drawPath(path, fillPaint);

        // Highlight streak (lighter colour on left face)
        fillPaint.setColor(Color.argb(120, 240, 250, 255));
        Path shine = new Path();
        shine.moveTo(x + width * 0.15f, y);
        shine.lineTo(x + width * 0.35f, y);
        shine.lineTo(cx - width * 0.05f, y + height * 0.6f);
        shine.close();
        canvas.drawPath(shine, fillPaint);

        // Blue shadow on right face
        fillPaint.setColor(Color.argb(80, 60, 120, 180));
        Path shadow = new Path();
        shadow.moveTo(cx, y);
        shadow.lineTo(x + width, y);
        shadow.lineTo(cx, y + height);
        shadow.close();
        canvas.drawPath(shadow, fillPaint);
    }

    private void drawSlider(Canvas canvas) {
        // Sliding ice block: rectangular, blue-grey
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(80, 160, 220));
        canvas.drawRect(x, y, x + width, y + height, fillPaint);

        // Highlight top-left
        fillPaint.setColor(Color.rgb(140, 200, 240));
        float bevel = height * 0.1f;
        canvas.drawRect(x, y, x + width, y + bevel, fillPaint);
        canvas.drawRect(x, y, x + bevel, y + height, fillPaint);

        // Dark edge bottom-right
        fillPaint.setColor(Color.rgb(40, 90, 150));
        canvas.drawRect(x, y + height - bevel, x + width, y + height, fillPaint);
        canvas.drawRect(x + width - bevel, y, x + width, y + height, fillPaint);

        // Danger stripe lines across the block
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setColor(Color.argb(90, 0, 180, 220));
        edgePaint.setStrokeWidth(2f);
        float stripeSpacing = width / 5f;
        for (int i = 1; i < 5; i++) {
            float sx = x + i * stripeSpacing;
            canvas.drawLine(sx, y, sx, y + height, edgePaint);
        }
    }
}
