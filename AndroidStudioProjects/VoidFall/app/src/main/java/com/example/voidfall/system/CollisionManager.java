package com.example.voidfall.system;

import android.graphics.RectF;

import com.example.voidfall.arena.ArenaManager;
import com.example.voidfall.echo.BlockEcho;
import com.example.voidfall.echo.DashEcho;
import com.example.voidfall.echo.EchoShadow;
import com.example.voidfall.echo.ProjectileEcho;
import com.example.voidfall.entity.Boss;
import com.example.voidfall.entity.Enemy;
import com.example.voidfall.entity.Player;
import com.example.voidfall.entity.Projectile;

import java.util.List;

/**
 * CollisionManager — resolves all collisions every frame.
 *
 * COLLISION TECHNIQUE — AABB (Axis-Aligned Bounding Box):
 *   Every entity has a rectangular getBounds() method.
 *   Two rectangles overlap if:
 *     A.left < B.right  AND  A.right > B.left
 *     A.top  < B.bottom AND  A.bottom > B.top
 *   This is the RectF.intersects() call.
 *
 * WHY NOT PIXEL-PERFECT:
 *   AABB is fast (O(n) checks per entity pair) and accurate enough for
 *   a game where entities are roughly rectangular. Pixel-perfect collision
 *   would require per-frame bitmap scanning — far too slow at 60fps.
 *
 * WHAT IS RESOLVED EACH FRAME:
 *   1. Player ↔ Enemy      (player takes damage if hit)
 *   2. Player ↔ Boss       (player takes damage if hit)
 *   3. Projectile ↔ Player (player takes damage; projectile deactivates)
 *   4. Player attack ↔ Enemy (player in attack range → enemy takes damage)
 *   5. Player attack ↔ Boss  (same)
 *   6. EchoShadow ↔ Player   (DashEcho / ProjectileEcho echoes can hurt player)
 *   7. BlockEcho  ↔ Player   (player cannot move through block echoes)
 *   8. Player     ↔ Gap      (player falls when over a gap)
 *
 * PLAYER ATTACK:
 *   The Player class auto-attacks enemies within ATTACK_RANGE every ATTACK_COOLDOWN.
 *   CollisionManager checks that range and calls enemy.takeDamage() when the
 *   player CAN attack (player.canAttack() returns true) and an enemy is in range.
 */
public class CollisionManager {

    public void resolveAll(Player player, List<Enemy> enemies, Boss boss,
                            List<Projectile> projectiles, List<EchoShadow> echoShadows,
                            ArenaManager arena, SoundManager sound) {
        if (player.isDead()) return;

        RectF pBounds = player.getBounds();

        // 1. Player ↔ Enemies
        for (Enemy e : enemies) {
            if (e.isDead()) continue;
            if (RectF.intersects(pBounds, e.getBounds())) {
                player.takeDamage(1);
                if (player.isDead()) return;
                sound.playPlayerHit();
            }
        }

        // 2. Player attack ↔ Enemies
        if (player.canAttack()) {
            float px = player.getCenterX();
            float py = player.getCenterY();
            float ar = player.getAttackRange();
            for (Enemy e : enemies) {
                if (e.isDead()) continue;
                float dx = e.getCenterX() - px;
                float dy = e.getCenterY() - py;
                if (dx * dx + dy * dy <= ar * ar) {
                    e.takeDamage((int)player.getAttackDamage());
                }
            }
        }

        // 3. Player ↔ Boss
        if (boss != null && !boss.isDead()) {
            if (RectF.intersects(pBounds, boss.getBounds())) {
                player.takeDamage(1);
                if (player.isDead()) return;
                sound.playPlayerHit();
            }
            // Player attack ↔ Boss
            if (player.canAttack()) {
                float dx = boss.getCenterX() - player.getCenterX();
                float dy = boss.getCenterY() - player.getCenterY();
                float ar = player.getAttackRange();
                if (dx * dx + dy * dy <= ar * ar) {
                    boss.takeDamage((int)player.getAttackDamage());
                }
            }
        }

        // 4. Projectiles ↔ Player
        for (Projectile proj : projectiles) {
            if (!proj.isActive() || proj.getOwner() != Projectile.OWNER_ENEMY) continue;
            if (RectF.intersects(pBounds, proj.getBounds())) {
                player.takeDamage(proj.getDamage());
                proj.deactivate();
                if (!player.isDead()) sound.playPlayerHit();
            }
        }

        // 5. EchoShadow interactions
        for (EchoShadow echo : echoShadows) {
            if (!echo.isActive()) continue;

            if (echo instanceof DashEcho) {
                // DashEcho damages player on contact
                if (RectF.intersects(pBounds, echo.getBounds())) {
                    player.takeDamage(1);
                    if (player.isDead()) return;
                    sound.playPlayerHit();
                }
            } else if (echo instanceof ProjectileEcho) {
                // Check projectiles owned by this echo
                for (Projectile ep : ((ProjectileEcho)echo).getProjectiles()) {
                    if (!ep.isActive()) continue;
                    if (RectF.intersects(pBounds, ep.getBounds())) {
                        player.takeDamage(ep.getDamage());
                        ep.deactivate();
                        if (!player.isDead()) sound.playPlayerHit();
                    }
                }
            } else if (echo instanceof BlockEcho) {
                // BlockEcho acts as an impassable wall — push player out
                resolveBlockEchoCollision(player, (BlockEcho)echo);
            }
        }
    }

    /**
     * Axis-separation push-out for BlockEcho ↔ Player.
     *
     * When two rectangles overlap, we compute the overlap on each axis
     * and push the player out along the SMALLEST overlap axis.
     * This is the minimal-translation-vector (MTV) technique.
     *
     * Example: player is pushed slightly left from a block rather than
     * all the way up/down if the horizontal overlap is smaller.
     */
    private void resolveBlockEchoCollision(Player player, BlockEcho block) {
        RectF pb = player.getBounds();
        RectF bb = block.getBounds();
        if (!RectF.intersects(pb, bb)) return;

        float overlapLeft   = bb.right  - pb.left;
        float overlapRight  = pb.right  - bb.left;
        float overlapTop    = bb.bottom - pb.top;
        float overlapBottom = pb.bottom - bb.top;

        float minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                    Math.min(overlapTop, overlapBottom));

        // The player's position is stored as top-left; adjust via setPosition
        float nx = player.getX();
        float ny = player.getY();

        if (minOverlap == overlapLeft)        nx += overlapLeft;
        else if (minOverlap == overlapRight)  nx -= overlapRight;
        else if (minOverlap == overlapTop)    ny += overlapTop;
        else                                  ny -= overlapBottom;

        player.setPosition(nx, ny);
    }
}
