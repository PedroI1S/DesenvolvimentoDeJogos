package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.util.GameTimer;

/**
 * Partícula visual de impacto/hit.
 * Usa timer interno para controlar duração e animação.
 */
public class Particle {
    private Vector2 position;
    private Vector2 velocity;
    private GameTimer lifeTimer;
    private float size;
    private Color color;
    private static final float GRAVITY = -200f;

    public Particle(float x, float y, float velocityX, float velocityY, 
                   float lifetime, float size, Color color) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(velocityX, velocityY);
        this.lifeTimer = new GameTimer(lifetime);
        this.size = size;
        this.color = new Color(color);
    }

    public void update(float delta) {
        lifeTimer.update(delta);
        
        // Aplica gravidade
        velocity.y += GRAVITY * delta;
        
        // Atualiza posição
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!isAlive()) {
            return;
        }

        // Fade out conforme a partícula envelhece
        float progress = lifeTimer.getProgress();
        float alpha = 1f - progress;
        
        Color renderColor = new Color(color);
        renderColor.a = alpha;
        shapeRenderer.setColor(renderColor);
        
        // Partícula fica menor conforme envelhece
        float currentSize = size * (1f - progress * 0.5f);
        shapeRenderer.circle(position.x, position.y, currentSize);
    }

    public boolean isAlive() {
        return !lifeTimer.isFinished();
    }

    public Vector2 getPosition() {
        return position;
    }

    public GameTimer getLifeTimer() {
        return lifeTimer;
    }

    public void reset(float x, float y, float velocityX, float velocityY, Color newColor) {
        this.position.set(x, y);
        this.velocity.set(velocityX, velocityY);
        this.color = new Color(newColor);
        this.lifeTimer.reset();
    }
}
