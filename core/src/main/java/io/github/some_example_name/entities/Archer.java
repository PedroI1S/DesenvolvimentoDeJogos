package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

import io.github.some_example_name.hitbox.CircleHitbox;
import io.github.some_example_name.util.GameTimer;

/**
 * Classe Archer representa o arqueiro no jogo
 * Pode atirar flechas com um cooldown interno
 */
public class Archer {
    private Vector2 position;
    private float rotation;
    private static final float ARCHER_SIZE = 20f;
    private static final float SHOOT_COOLDOWN = 0.5f; // 500ms entre tiros
    
    private GameTimer shootCooldown;
    private Sound shootSound;
    private CircleHitbox hitbox;

    public Archer(float x, float y, Sound sound) {
        this.position = new Vector2(x, y);
        this.rotation = 0f;
        this.shootCooldown = new GameTimer(SHOOT_COOLDOWN);
        this.shootSound = sound;
        this.hitbox = new CircleHitbox(ARCHER_SIZE / 2);
        this.hitbox.update(position);
    }

    public void update(float delta, float targetX, float targetY) {
        shootCooldown.update(delta);
        
        // Calcula ângulo em direção ao alvo
        Vector2 toTarget = new Vector2(targetX - position.x, targetY - position.y);
        this.rotation = (float) Math.toDegrees((float) Math.atan2(toTarget.y, toTarget.x));
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.2f, 0.6f, 1f, 1); // Cor azul
        
        // Desenha o corpo do arqueiro como um círculo
        shapeRenderer.circle(position.x, position.y, ARCHER_SIZE / 2);
        
        // Desenha a direção do arco
        float radians = (float) Math.toRadians(rotation);
        float tipX = position.x + (float) Math.cos(radians) * ARCHER_SIZE;
        float tipY = position.y + (float) Math.sin(radians) * ARCHER_SIZE;
        
        shapeRenderer.setColor(0.8f, 0.4f, 0.2f, 1);
        shapeRenderer.line(position.x, position.y, tipX, tipY);
    }

    public boolean canShoot() {
        return shootCooldown.isFinished();
    }

    public Arrow shoot() {
        if (canShoot()) {
            shootCooldown.reset();
            return new Arrow(position.x, position.y, rotation, shootSound);
        }
        return null;
    }

    public void registerShot() {
        shootCooldown.reset();
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void move(float deltaX, float deltaY, float minX, float maxX, float minY, float maxY) {
        float halfSize = ARCHER_SIZE / 2f;
        position.x = Math.max(Math.max(halfSize, minX), Math.min(position.x + deltaX, Math.min(maxX, Float.MAX_VALUE)));
        position.y = Math.max(Math.max(halfSize, minY), Math.min(position.y + deltaY, Math.min(maxY, Float.MAX_VALUE)));
        hitbox.update(position);
    }

    public float getRotation() {
        return rotation;
    }
    
    public CircleHitbox getHitbox() {
        return hitbox;
    }
}
