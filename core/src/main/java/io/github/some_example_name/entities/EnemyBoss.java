package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.hitbox.CircleHitbox;
import io.github.some_example_name.hitbox.CompoundHitbox;
import io.github.some_example_name.hitbox.Hitbox;
import io.github.some_example_name.util.GameTimer;

/**
 * Boss enemy com múltiplas hitboxes agregadas (CompoundHitbox).
 * Demonstra o uso de hitbox composta onde diferentes partes do inimigo
 * possuem suas próprias hitboxes, mas um acerto em qualquer uma mata o boss.
 */
public class EnemyBoss {
    private Vector2 position;
    private Vector2 velocity;
    private GameTimer moveTimer;
    private boolean alive = true;
    private CompoundHitbox hitbox;
    
    private static final float BOSS_SPEED = 60f;
    private static final float BOSS_SIZE = 30f;

    public EnemyBoss(float x, float y) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(1, 0);
        this.moveTimer = new GameTimer(4f);
        this.alive = true;
        
        // Cria hitbox composta com múltiplas partes
        this.hitbox = new CompoundHitbox();
        
        // Corpo principal (círculo grande no centro)
        CircleHitbox bodyHitbox = new CircleHitbox(BOSS_SIZE);
        hitbox.addHitbox(bodyHitbox);
        
        // Duas "patas" ou "braços" laterais (círculos menores)
        CircleHitbox leftLimbHitbox = new CircleHitbox(12f);
        CircleHitbox rightLimbHitbox = new CircleHitbox(12f);
        hitbox.addHitbox(leftLimbHitbox);
        hitbox.addHitbox(rightLimbHitbox);
        
        // Uma "cabeça" no topo (círculo menor)
        CircleHitbox headHitbox = new CircleHitbox(10f);
        hitbox.addHitbox(headHitbox);
        
        updateHitboxPositions();
        randomizeDirection();
    }

    public void init(float x, float y) {
        this.position.set(x, y);
        this.alive = true;
        this.moveTimer.reset();
        updateHitboxPositions();
        randomizeDirection();
    }

    public void reset() {
        position.set(0, 0);
        velocity.set(0, 0);
        alive = false;
        moveTimer.reset();
    }

    public void update(float delta, float wallLeft, float wallRight, float wallBottom, float wallTop) {
        moveTimer.update(delta);
        
        if (moveTimer.isFinished()) {
            randomizeDirection();
            moveTimer.reset();
        }
        
        position.x += velocity.x * BOSS_SPEED * delta;
        position.y += velocity.y * BOSS_SPEED * delta;
        
        if (position.x - BOSS_SIZE <= wallLeft || position.x + BOSS_SIZE >= wallRight) {
            velocity.x *= -1;
        }
        if (position.y - BOSS_SIZE <= wallBottom || position.y + BOSS_SIZE >= wallTop) {
            velocity.y *= -1;
        }
        
        position.x = Math.max(wallLeft + BOSS_SIZE, Math.min(position.x, wallRight - BOSS_SIZE));
        position.y = Math.max(wallBottom + BOSS_SIZE, Math.min(position.y, wallTop - BOSS_SIZE));
        
        updateHitboxPositions();
    }

    /**
     * Atualiza as posições das hitboxes componentes
     * Cada parte do boss tem sua própria posição relativa ao centro
     */
    private void updateHitboxPositions() {
        if (hitbox.getHitboxCount() < 4) return;
        
        // Corpo principal no centro
        hitbox.getHitboxes().get(0).update(position);
        
        // Pata/braço esquerdo
        Vector2 leftLimb = new Vector2(position).add(-20f, -5f);
        hitbox.getHitboxes().get(1).update(leftLimb);
        
        // Pata/braço direito
        Vector2 rightLimb = new Vector2(position).add(20f, -5f);
        hitbox.getHitboxes().get(2).update(rightLimb);
        
        // Cabeça no topo
        Vector2 head = new Vector2(position).add(0, 35f);
        hitbox.getHitboxes().get(3).update(head);
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!alive) return;
        
        // Corpo principal
        shapeRenderer.setColor(0.8f, 0.2f, 0.8f, 1f); // Magenta
        shapeRenderer.circle(position.x, position.y, BOSS_SIZE);
        
        // Pata esquerda
        shapeRenderer.setColor(0.7f, 0.15f, 0.7f, 1f);
        shapeRenderer.circle(position.x - 20f, position.y - 5f, 12f);
        
        // Pata direita
        shapeRenderer.circle(position.x + 20f, position.y - 5f, 12f);
        
        // Cabeça
        shapeRenderer.setColor(0.9f, 0.3f, 0.9f, 1f);
        shapeRenderer.circle(position.x, position.y + 35f, 10f);
        
        // Olhos
        shapeRenderer.setColor(1, 1, 0, 1); // Amarelo
        shapeRenderer.circle(position.x - 6f, position.y + 40f, 3f);
        shapeRenderer.circle(position.x + 6f, position.y + 40f, 3f);
    }

    private void randomizeDirection() {
        double angle = Math.random() * Math.PI * 2;
        velocity.set((float) Math.cos(angle), (float) Math.sin(angle));
        velocity.nor();
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public boolean collidesWithHitbox(Hitbox other) {
        return hitbox.collidesWith(other);
    }

    public CompoundHitbox getHitbox() {
        return hitbox;
    }
}
