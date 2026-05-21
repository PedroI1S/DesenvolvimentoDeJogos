package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.hitbox.CircleHitbox;
import io.github.some_example_name.hitbox.CompoundHitbox;
import io.github.some_example_name.hitbox.Hitbox;
import io.github.some_example_name.util.GameTimer;

/**
 * Boss enemy rendered as a Wolf sprite at 2× scale (96×96 world units).
 * Compound hitbox: large body circle + directional head circle.
 */
public class EnemyBoss {
    private static final int DIR_U = 0, DIR_D = 1, DIR_L = 2, DIR_R = 3;
    private static final String[] DIR_NAMES = { "U", "D", "L", "R" };
    private static final int FRAME_COUNT = 6;
    private static final float FRAME_DURATION = 0.15f;

    // 2× Wolf sprite: 96×96 world units centered on position
    private static final float SPRITE_HALF = 48f;
    private static final float BOSS_SPEED  = 60f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 tmp;

    private final TextureRegion[][] walkFrames;
    private final TextureRegion[][] deathFrames;

    private final GameTimer frameTimer;
    private final GameTimer moveTimer;

    // Compound hitbox: large body + directional head matching the wolf silhouette at 2× scale
    private final CircleHitbox hitboxBody;
    private final CircleHitbox hitboxHead;
    private final CompoundHitbox hitbox;

    private boolean alive = true;
    private boolean dying = false;
    private int currentFrame = 0;
    private int deathFrame   = 0;
    private int currentDir   = DIR_D;

    public EnemyBoss(float x, float y, TextureAtlas atlas) {
        position = new Vector2(x, y);
        velocity = new Vector2(1, 0);
        tmp      = new Vector2();

        frameTimer = new GameTimer(FRAME_DURATION);
        moveTimer  = new GameTimer(4f);

        walkFrames  = new TextureRegion[4][FRAME_COUNT];
        deathFrames = new TextureRegion[4][FRAME_COUNT];
        for (int d = 0; d < 4; d++) {
            for (int f = 0; f < FRAME_COUNT; f++) {
                walkFrames[d][f]  = atlas.findRegion("Wolf_" + DIR_NAMES[d] + "_Walk",  f + 1);
                deathFrames[d][f] = atlas.findRegion("Wolf_" + DIR_NAMES[d] + "_Death", f + 1);
            }
        }

        hitboxBody = new CircleHitbox(22f);
        hitboxHead = new CircleHitbox(16f);
        hitbox     = new CompoundHitbox();
        hitbox.addHitbox(hitboxBody);
        hitbox.addHitbox(hitboxHead);

        randomizeDirection();
        updateHitboxes();
    }

    public void update(float delta, float wallLeft, float wallRight, float wallBottom, float wallTop) {
        frameTimer.update(delta);
        if (frameTimer.isFinished()) {
            frameTimer.reset();
            if (dying) {
                deathFrame++;
                if (deathFrame >= FRAME_COUNT) dying = false;
            } else {
                currentFrame = (currentFrame + 1) % FRAME_COUNT;
            }
        }

        if (!alive) return;

        moveTimer.update(delta);
        if (moveTimer.isFinished()) {
            randomizeDirection();
            moveTimer.reset();
        }

        position.x += velocity.x * BOSS_SPEED * delta;
        position.y += velocity.y * BOSS_SPEED * delta;

        if (position.x - SPRITE_HALF <= wallLeft  || position.x + SPRITE_HALF >= wallRight)  velocity.x *= -1;
        if (position.y - SPRITE_HALF <= wallBottom || position.y + SPRITE_HALF >= wallTop)    velocity.y *= -1;

        position.x = Math.max(wallLeft  + SPRITE_HALF, Math.min(position.x, wallRight  - SPRITE_HALF));
        position.y = Math.max(wallBottom + SPRITE_HALF, Math.min(position.y, wallTop   - SPRITE_HALF));

        currentDir = dirFromVelocity();
        updateHitboxes();
    }

    public void render(SpriteBatch batch) {
        if (isDead()) return;

        TextureRegion[][] frames = dying ? deathFrames : walkFrames;
        int frame = Math.min(dying ? deathFrame : currentFrame, FRAME_COUNT - 1);
        TextureRegion region = frames[currentDir][frame];
        if (region == null) return;

        batch.draw(region,
            position.x - SPRITE_HALF, position.y - SPRITE_HALF,
            SPRITE_HALF * 2, SPRITE_HALF * 2);
    }

    public void kill() {
        if (!alive) return;
        alive      = false;
        dying      = true;
        deathFrame = 0;
        frameTimer.reset();
    }

    private void updateHitboxes() {
        hitboxBody.update(position);
        if (velocity.len2() > 0.001f) tmp.set(velocity).nor().scl(28f).add(position);
        else                           tmp.set(position);
        hitboxHead.update(tmp);
    }

    private void randomizeDirection() {
        double angle = Math.random() * Math.PI * 2;
        velocity.set((float) Math.cos(angle), (float) Math.sin(angle));
        currentDir = dirFromVelocity();
    }

    private int dirFromVelocity() {
        float ax = Math.abs(velocity.x), ay = Math.abs(velocity.y);
        if (ay >= ax) return velocity.y >= 0 ? DIR_U : DIR_D;
        return velocity.x >= 0 ? DIR_L : DIR_R;
    }

    public boolean isAlive()  { return alive; }
    public boolean isDead()   { return !alive && !dying; }
    public Vector2 getPosition() { return position; }

    public boolean collidesWithHitbox(Hitbox other) {
        return hitbox.collidesWith(other);
    }

    public CompoundHitbox getHitbox() { return hitbox; }
}
