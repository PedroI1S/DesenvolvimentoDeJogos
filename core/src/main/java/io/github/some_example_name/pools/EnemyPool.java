package io.github.some_example_name.pools;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import io.github.some_example_name.entities.Enemy;

/**
 * Object Pool for enemies.
 * Passes initialSize=0 to super so createNew() (which needs the atlas) is never
 * called before the subclass field is initialized. Objects are created on demand.
 */
public class EnemyPool extends ObjectPool<Enemy> {
    private final TextureAtlas atlas;

    public EnemyPool(int maxSize, TextureAtlas atlas) {
        super(0, maxSize);
        this.atlas = atlas;
    }

    @Override
    protected Enemy createNew() {
        return new Enemy(0, 0, atlas);
    }

    @Override
    protected void reset(Enemy enemy) {
        enemy.reset();
    }

    public Enemy obtain(float x, float y) {
        Enemy enemy = obtain();
        enemy.init(x, y);
        return enemy;
    }
}
