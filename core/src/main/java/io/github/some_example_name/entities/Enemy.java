package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.hitbox.CircleHitbox;
import io.github.some_example_name.hitbox.CompoundHitbox;
import io.github.some_example_name.hitbox.Hitbox;
import io.github.some_example_name.util.GameTimer;

public class Enemy {
    public enum Type { BEE, GOBLIN, SLIME, WOLF }

    private static final int DIR_U = 0, DIR_D = 1, DIR_L = 2, DIR_R = 3;
    private static final String[] DIR_NAMES = { "U", "D", "L", "R" };
    private static final int FRAME_COUNT = 6;
    private static final float FRAME_DURATION = 0.12f;

    // Sprite is 48x48 world units, centered on position
    public static final float SPRITE_HALF = 24f;
    private static final float SPEED = 100f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 tmp;
    private final Type type;

    // [direction][frame] — loaded once at construction, reused for lifetime
    private final TextureRegion[][] walkFrames;
    private final TextureRegion[][] deathFrames;

    private final GameTimer frameTimer;
    private final GameTimer moveTimer;

    // Compound-hitbox parts (non-null for GOBLIN and WOLF only)
    private final CircleHitbox hitboxBody;
    private final CircleHitbox hitboxHead;
    private final Hitbox hitbox;

    private boolean alive = true;
    private boolean dying = false;
    private int currentFrame = 0;
    private int deathFrame  = 0;
    private int currentDir  = DIR_D;

    public Enemy(float x, float y, TextureAtlas atlas) {
        position = new Vector2(x, y);
        velocity = new Vector2(1, 0);
        tmp      = new Vector2();

        frameTimer = new GameTimer(FRAME_DURATION);
        moveTimer  = new GameTimer(3f);

        Type[] all = Type.values();
        type = all[(int)(Math.random() * all.length)];

        walkFrames  = new TextureRegion[4][FRAME_COUNT];
        deathFrames = new TextureRegion[4][FRAME_COUNT];
        loadFrames(atlas);

        // Build type-specific hitbox matching the sprite's actual shape.
        // Bee  : compact flying body → single circle r=10
        // Goblin: humanoid → compound: body circle + head circle above
        // Slime : wide blob sitting low → single circle r=13, shifted down 6
        // Wolf  : four-legged animal → compound: body + directional head
        switch (type) {
            case GOBLIN: {
                CircleHitbox body = new CircleHitbox(10f);
                CircleHitbox head = new CircleHitbox(8f);
                CompoundHitbox c = new CompoundHitbox();
                c.addHitbox(body);
                c.addHitbox(head);
                hitboxBody = body;
                hitboxHead = head;
                hitbox = c;
                break;
            }
            case WOLF: {
                CircleHitbox body = new CircleHitbox(10f);
                CircleHitbox head = new CircleHitbox(8f);
                CompoundHitbox c = new CompoundHitbox();
                c.addHitbox(body);
                c.addHitbox(head);
                hitboxBody = body;
                hitboxHead = head;
                hitbox = c;
                break;
            }
            default: {
                hitboxBody = null;
                hitboxHead = null;
                hitbox = new CircleHitbox(type == Type.SLIME ? 13f : 10f);
                break;
            }
        }

        randomizeDirection();
        updateHitboxes();
    }

    // Called by pool when obtaining a recycled instance — resets state, keeps type/frames/hitbox
    public void init(float x, float y) {
        position.set(x, y);
        alive        = true;
        dying        = false;
        currentFrame = 0;
        deathFrame   = 0;
        frameTimer.reset();
        moveTimer.reset();
        randomizeDirection();
        updateHitboxes();
    }

    public void reset() {
        position.set(0, 0);
        velocity.set(0, 0);
        alive        = false;
        dying        = false;
        currentFrame = 0;
        deathFrame   = 0;
        frameTimer.reset();
        moveTimer.reset();
    }

    public void update(float delta, float wallLeft, float wallRight, float wallBottom, float wallTop) {
        // Advance animation frame regardless of alive/dying state
        frameTimer.update(delta);
        if (frameTimer.isFinished()) {
            frameTimer.reset();
            if (dying) {
                deathFrame++;
                if (deathFrame >= FRAME_COUNT) dying = false; // animation complete → isDead()
            } else {
                currentFrame = (currentFrame + 1) % FRAME_COUNT;
            }
        }

        if (!alive) return; // no AI movement while dying

        moveTimer.update(delta);
        if (moveTimer.isFinished()) {
            randomizeDirection();
            moveTimer.reset();
        }

        position.x += velocity.x * SPEED * delta;
        position.y += velocity.y * SPEED * delta;

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

    private void loadFrames(TextureAtlas atlas) {
        String prefix = type == Type.BEE ? "Bee" : type == Type.GOBLIN ? "Goblin"
                      : type == Type.SLIME ? "Slime" : "Wolf";
        for (int d = 0; d < 4; d++) {
            String walkKey  = prefix + "_" + DIR_NAMES[d] + "_Walk";
            String deathKey = prefix + "_" + DIR_NAMES[d] + "_Death";
            for (int f = 0; f < FRAME_COUNT; f++) {
                walkFrames[d][f]  = atlas.findRegion(walkKey,  f + 1);
                deathFrames[d][f] = atlas.findRegion(deathKey, f + 1);
            }
        }
    }

    private int dirFromVelocity() {
        float ax = Math.abs(velocity.x), ay = Math.abs(velocity.y);
        if (ay >= ax) return velocity.y >= 0 ? DIR_U : DIR_D;
        return velocity.x >= 0 ? DIR_L : DIR_R;
    }

    private void updateHitboxes() {
        switch (type) {
            case GOBLIN:
                // Body at sprite center, head 10 units above
                hitboxBody.update(position);
                tmp.set(position).add(0, 10f);
                hitboxHead.update(tmp);
                break;
            case WOLF:
                // Body at center, head pushed in movement direction
                hitboxBody.update(position);
                if (velocity.len2() > 0.001f) tmp.set(velocity).nor().scl(12f).add(position);
                else                           tmp.set(position);
                hitboxHead.update(tmp);
                break;
            case SLIME:
                // Slime blob sits low in the sprite
                tmp.set(position).add(0, -6f);
                hitbox.update(tmp);
                break;
            default: // BEE
                hitbox.update(position);
                break;
        }
    }

    private void randomizeDirection() {
        double angle = Math.random() * Math.PI * 2;
        velocity.set((float) Math.cos(angle), (float) Math.sin(angle));
        currentDir = dirFromVelocity();
    }

    public boolean isAlive()  { return alive; }           // collidable, AI active
    public boolean isDying()  { return dying; }            // playing death anim
    public boolean isDead()   { return !alive && !dying; } // fully done, ready for pool
    public Vector2 getPosition() { return position; }
    public float   getRadius()   { return SPRITE_HALF; }
    public Hitbox  getHitbox()   { return hitbox; }
    public Type    getType()     { return type; }

    public boolean collidesWith(Vector2 point, float pointRadius) {
        return position.dst(point) < (SPRITE_HALF + pointRadius);
    }

    public boolean collidesWithHitbox(Hitbox other) {
        return hitbox.collidesWith(other);
    }
}
