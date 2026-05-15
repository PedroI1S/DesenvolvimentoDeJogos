package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

import io.github.some_example_name.util.GameTimer;

/**
 * Classe Arrow representa uma flecha no jogo
 * Cada flecha tem sua própria instância de som de disparo
 */
public class Arrow {
    private Vector2 position;
    private Vector2 velocity;
    private float rotation;
    private static final float ARROW_SPEED = 500f;
    private static final float ARROW_SIZE = 15f;
    private GameTimer lifeTimer;
    private Sound shootSound;
    private boolean soundPlayed;

    public Arrow(float x, float y, float angle, Sound sound) {
        this.position = new Vector2(x, y);
        this.rotation = angle;
        this.shootSound = sound;
        this.soundPlayed = false;
        
        // Velocidade baseada no ângulo
        this.velocity = new Vector2(
            (float) Math.cos(Math.toRadians(angle)) * ARROW_SPEED,
            (float) Math.sin(Math.toRadians(angle)) * ARROW_SPEED
        );
        
        // Timer interno - flecha dura 10 segundos
        this.lifeTimer = new GameTimer(10f);
    }

    /**
     * Inicializa arrow com nova posição e ângulo (para object pooling)
     */
    public void init(float x, float y, float angle) {
        this.position.set(x, y);
        this.rotation = angle;
        this.soundPlayed = false;
        
        // Velocidade baseada no ângulo
        this.velocity.set(
            (float) Math.cos(Math.toRadians(angle)) * ARROW_SPEED,
            (float) Math.sin(Math.toRadians(angle)) * ARROW_SPEED
        );
        
        lifeTimer.reset();
    }

    public void playShootSound() {
        if (soundPlayed || shootSound == null) {
            return;
        }

        try {
            shootSound.play();
            soundPlayed = true;
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("Arrow", "Error playing shoot sound: " + e.getMessage());
        }
    }

    /**
     * Reset para reutilização no pool
     */
    public void reset() {
        position.set(0, 0);
        velocity.set(0, 0);
        rotation = 0;
        soundPlayed = false;
        lifeTimer.reset();
    }

    public void update(float delta) {
        lifeTimer.update(delta);
        
        // Atualiza posição
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(1, 0.8f, 0.2f, 1); // Cor amarela/ouro
        
        // Desenha a flecha como um triângulo
        float x1 = position.x;
        float y1 = position.y;
        
        float radians = (float) Math.toRadians(rotation);
        float tipX = x1 + (float) Math.cos(radians) * ARROW_SIZE;
        float tipY = y1 + (float) Math.sin(radians) * ARROW_SIZE;
        
        float tailX = x1 - (float) Math.cos(radians) * ARROW_SIZE / 2;
        float tailY = y1 - (float) Math.sin(radians) * ARROW_SIZE / 2;
        
        float perpX = (float) Math.sin(radians) * (ARROW_SIZE / 3);
        float perpY = -(float) Math.cos(radians) * (ARROW_SIZE / 3);
        
        shapeRenderer.triangle(tipX, tipY, 
                             tailX + perpX, tailY + perpY,
                             tailX - perpX, tailY - perpY);
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isAlive() {
        return !lifeTimer.isFinished();
    }

    public GameTimer getLifeTimer() {
        return lifeTimer;
    }
}
