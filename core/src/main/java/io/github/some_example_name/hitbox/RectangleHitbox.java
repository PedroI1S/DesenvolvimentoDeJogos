package io.github.some_example_name.hitbox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Hitbox retangular simples (Axis-Aligned Bounding Box - AABB).
 * Uma única hitbox quadrada/retangular por objeto.
 */
public class RectangleHitbox implements Hitbox {
    private float x;
    private float y;
    private float width;
    private float height;
    private final Vector2 center;

    public RectangleHitbox(float width, float height) {
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;
        this.center = new Vector2();
    }

    @Override
    public void update(Vector2 position) {
        this.x = position.x - width / 2f;
        this.y = position.y - height / 2f;
        this.center.set(position.x, position.y);
    }

    @Override
    public boolean collidesWith(Vector2 point) {
        return point.x >= x && point.x <= x + width &&
               point.y >= y && point.y <= y + height;
    }

    @Override
    public boolean collidesWith(Hitbox other) {
        if (other instanceof RectangleHitbox) {
            RectangleHitbox rect = (RectangleHitbox) other;
            return !(x + width < rect.x || 
                     x > rect.x + rect.width ||
                     y + height < rect.y ||
                     y > rect.y + rect.height);
        } else if (other instanceof CircleHitbox) {
            CircleHitbox circle = (CircleHitbox) other;
            return circle.collidesWith(this);
        }
        return false;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(x, y, width, height);
    }

    @Override
    public Vector2 getCenter() {
        return center;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
