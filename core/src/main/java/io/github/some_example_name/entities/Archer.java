package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

import io.github.some_example_name.hitbox.CircleHitbox;
import io.github.some_example_name.util.GameTimer;

public class Archer {
    // 8-direction names matching ArcherWalking.atlas region keys
    private static final String[] DIRS = {
        "east", "north-east", "north", "north-west",
        "west", "south-west", "south", "south-east"
    };
    private static final int FRAME_COUNT     = 6;
    private static final float FRAME_DURATION = 0.12f;

    // Sprite 88×88 rendered at 64×64 world units — hitbox radius 20
    private static final float SPRITE_HALF   = 32f;
    private static final float ARCHER_RADIUS = 20f;
    private static final float SHOOT_COOLDOWN = 0.5f;

    private final Vector2 position;
    private float rotation; // degrees, 0=east CCW
    private boolean isMoving = false;

    private final TextureRegion[][] walkFrames; // [8 dirs][6 frames]
    private final GameTimer frameTimer;
    private final GameTimer shootCooldown;
    private final Sound shootSound;
    private final CircleHitbox hitbox;

    private int currentFrame = 0;
    private int currentDir   = 6; // start facing south

    public Archer(float x, float y, Sound sound, TextureAtlas atlas) {
        position      = new Vector2(x, y);
        shootSound    = sound;
        frameTimer    = new GameTimer(FRAME_DURATION);
        shootCooldown = new GameTimer(SHOOT_COOLDOWN);
        hitbox        = new CircleHitbox(ARCHER_RADIUS);
        hitbox.update(position);

        walkFrames = new TextureRegion[8][FRAME_COUNT];
        for (int d = 0; d < 8; d++) {
            for (int f = 0; f < FRAME_COUNT; f++) {
                walkFrames[d][f] = atlas.findRegion(DIRS[d] + "/frame", f);
            }
        }
    }

    public void update(float delta, float targetX, float targetY) {
        shootCooldown.update(delta);

        Vector2 toTarget = new Vector2(targetX - position.x, targetY - position.y);
        rotation   = (float) Math.toDegrees(Math.atan2(toTarget.y, toTarget.x));
        currentDir = dirFromAngle(rotation);

        if (isMoving) {
            frameTimer.update(delta);
            if (frameTimer.isFinished()) {
                frameTimer.reset();
                currentFrame = (currentFrame + 1) % FRAME_COUNT;
            }
        } else {
            currentFrame = 0;
        }

        isMoving = false; // reset each frame; move() sets it back to true
    }

    public void render(SpriteBatch batch) {
        TextureRegion region = walkFrames[currentDir][currentFrame];
        if (region == null) return;
        batch.draw(region,
            position.x - SPRITE_HALF, position.y - SPRITE_HALF,
            SPRITE_HALF * 2, SPRITE_HALF * 2);
    }

    public boolean canShoot() {
        return shootCooldown.isFinished();
    }

    public Arrow shoot() {
        if (!canShoot()) return null;
        shootCooldown.reset();
        return new Arrow(position.x, position.y, rotation, shootSound);
    }

    public void registerShot() {
        shootCooldown.reset();
    }

    public void move(float deltaX, float deltaY, float minX, float maxX, float minY, float maxY) {
        float half = ARCHER_RADIUS;
        position.x = Math.max(Math.max(half, minX), Math.min(position.x + deltaX, Math.min(maxX, Float.MAX_VALUE)));
        position.y = Math.max(Math.max(half, minY), Math.min(position.y + deltaY, Math.min(maxY, Float.MAX_VALUE)));
        hitbox.update(position);
        if (deltaX != 0 || deltaY != 0) isMoving = true;
    }

    private int dirFromAngle(float angleDeg) {
        // Map rotation (0=east, CCW) to 8-direction index:
        // east=0, north-east=1, north=2, north-west=3,
        // west=4, south-west=5, south=6, south-east=7
        float a = ((angleDeg % 360) + 360) % 360;
        return (int)((a + 22.5f) / 45f) % 8;
    }

    public Vector2 getPosition()    { return position; }
    public float   getRotation()    { return rotation; }
    public CircleHitbox getHitbox() { return hitbox; }

    public void setPosition(float x, float y) {
        position.set(x, y);
        hitbox.update(position);
    }
}
