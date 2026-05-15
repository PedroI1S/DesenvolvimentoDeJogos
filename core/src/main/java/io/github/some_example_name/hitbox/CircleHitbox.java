package io.github.some_example_name.hitbox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Hitbox circular implementada com funções matemáticas próprias.
 * Usa distância euclidiana para colisão ponto-círculo.
 * Usa algoritmo matemático para colisão círculo-retângulo.
 */
public class CircleHitbox implements Hitbox {
    private final Vector2 position;
    private float radius;

    public CircleHitbox(float radius) {
        this.radius = radius;
        this.position = new Vector2();
    }

    @Override
    public void update(Vector2 pos) {
        this.position.set(pos);
    }

    /**
     * Colisão ponto-círculo usando distância euclidiana.
     * Distância = sqrt((x2 - x1)² + (y2 - y1)²)
     * Se distância <= raio, há colisão
     */
    @Override
    public boolean collidesWith(Vector2 point) {
        float dx = point.x - position.x;
        float dy = point.y - position.y;
        float distanceSquared = dx * dx + dy * dy;
        float radiusSquared = radius * radius;
        return distanceSquared <= radiusSquared;
    }

    @Override
    public boolean collidesWith(Hitbox other) {
        if (other instanceof CircleHitbox) {
            CircleHitbox circle = (CircleHitbox) other;
            // Colisão círculo-círculo: distância entre centros <= soma dos raios
            float dx = circle.position.x - position.x;
            float dy = circle.position.y - position.y;
            float distanceSquared = dx * dx + dy * dy;
            float radiusSum = radius + circle.radius;
            return distanceSquared <= (radiusSum * radiusSum);
        } else if (other instanceof RectangleHitbox) {
            // Colisão círculo-retângulo
            return collidesWithRectangle((RectangleHitbox) other);
        }
        return false;
    }

    /**
     * Colisão círculo-retângulo usando função matemática.
     * Encontra o ponto mais próximo do retângulo ao círculo
     * e verifica se a distância é menor que o raio.
     */
    private boolean collidesWithRectangle(RectangleHitbox rect) {
        // Encontra o ponto mais próximo do retângulo ao centro do círculo
        float closestX = Math.max(rect.getX(), Math.min(position.x, rect.getX() + rect.getWidth()));
        float closestY = Math.max(rect.getY(), Math.min(position.y, rect.getY() + rect.getHeight()));
        
        // Calcula a distância entre o centro do círculo e o ponto mais próximo
        float dx = position.x - closestX;
        float dy = position.y - closestY;
        float distanceSquared = dx * dx + dy * dy;
        
        // Há colisão se a distância é menor ou igual ao raio
        return distanceSquared <= (radius * radius);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(position.x, position.y, radius);
    }

    @Override
    public Vector2 getCenter() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public Vector2 getPosition() {
        return position;
    }
}
