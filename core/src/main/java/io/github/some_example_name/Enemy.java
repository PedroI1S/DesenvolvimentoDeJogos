package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Classe Enemy representa um inimigo no jogo
 * Patrulha em um padrão e pode ser destruído por flechas
 * Usa hitbox circular para colisão mais precisa
 */
public class Enemy {
    private Vector2 position;
    private Vector2 velocity;
    private float radius;
    private GameTimer moveTimer;
    private float moveInterval = 3f; // Muda direção a cada 3 segundos
    private boolean alive = true;
    private Hitbox hitbox;
    
    private static final float ENEMY_SPEED = 100f;
    private static final float ENEMY_SIZE = 12f;
    
    public Enemy(float x, float y) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(1, 0);
        this.radius = ENEMY_SIZE;
        this.moveTimer = new GameTimer(moveInterval);
        this.hitbox = new CircleHitbox(ENEMY_SIZE);
        this.hitbox.update(position);
        this.alive = true;
        
        // Inicia movimento aleatório
        randomizeDirection();
    }

    /**
     * Inicializa enemy com nova posição (para object pooling)
     */
    public void init(float x, float y) {
        this.position.set(x, y);
        this.hitbox.update(position);
        this.alive = true;
        this.moveTimer.reset();
        randomizeDirection();
    }

    /**
     * Reset para reutilização no pool
     */
    public void reset() {
        position.set(0, 0);
        velocity.set(0, 0);
        alive = false;
        moveTimer.reset();
    }

    public void update(float delta, float wallLeft, float wallRight, float wallBottom, float wallTop) {
        moveTimer.update(delta);
        
        // Muda direção quando timer termina
        if (moveTimer.isFinished()) {
            randomizeDirection();
            moveTimer.reset();
        }
        
        // Movimento
        position.x += velocity.x * ENEMY_SPEED * delta;
        position.y += velocity.y * ENEMY_SPEED * delta;
        
        // Bounce nas bordas do mundo
        if (position.x - radius <= wallLeft || position.x + radius >= wallRight) {
            velocity.x *= -1;
        }
        if (position.y - radius <= wallBottom || position.y + radius >= wallTop) {
            velocity.y *= -1;
        }
        
        // Clamp para evitar sair do mundo
        position.x = Math.max(wallLeft + radius, Math.min(position.x, wallRight - radius));
        position.y = Math.max(wallBottom + radius, Math.min(position.y, wallTop - radius));
        
        // Atualiza hitbox
        hitbox.update(position);
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!alive) return;
        
        // Desenha inimigo em vermelho
        shapeRenderer.setColor(1, 0.2f, 0.2f, 1f); // Vermelho
        shapeRenderer.circle(position.x, position.y, radius);
        
        // Desenha olho para mostrar direção
        shapeRenderer.setColor(1, 1, 1, 1); // Branco
        float eyeX = position.x + velocity.x * radius * 0.5f;
        float eyeY = position.y + velocity.y * radius * 0.5f;
        shapeRenderer.circle(eyeX, eyeY, radius * 0.3f);
    }

    private void randomizeDirection() {
        // Escolhe uma direção aleatória (8 direções)
        double angle = Math.random() * Math.PI * 2;
        velocity.set((float) Math.cos(angle), (float) Math.sin(angle));
        velocity.nor(); // Normaliza para magnitude 1
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    /**
     * Verifica colisão usando a hitbox circular
     */
    public boolean collidesWith(Vector2 point) {
        return hitbox.collidesWith(point);
    }

    /**
     * Verifica colisão com outra hitbox
     */
    public boolean collidesWithHitbox(Hitbox other) {
        return hitbox.collidesWith(other);
    }

    /**
     * Verifica colisão com círculo (método antigo para compatibilidade)
     */
    public boolean collidesWith(Vector2 point, float pointRadius) {
        float distance = position.dst(point);
        return distance < (radius + pointRadius);
    }

    public Hitbox getHitbox() {
        return hitbox;
    }
}
