package io.github.some_example_name.pools;

import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.entities.Particle;

/**
 * Object Pool para Particles.
 * Reutiliza instâncias em vez de criar/destruir continuamente.
 */
public class ParticlePool extends ObjectPool<Particle> {

    public ParticlePool(int initialSize, int maxSize) {
        super(initialSize, maxSize);
    }

    @Override
    protected Particle createNew() {
        // Cria partícula dummy com valores padrão
        return new Particle(0, 0, 0, 0, 1f, 5f, Color.WHITE);
    }

    @Override
    protected void reset(Particle particle) {
        particle.getLifeTimer().reset();
    }

    /**
     * Obtém partícula inicializada com posição, velocidade e cor
     */
    public Particle obtain(float x, float y, float velocityX, float velocityY, 
                          float lifetime, float size, Color color) {
        Particle particle = obtain();
        particle.reset(x, y, velocityX, velocityY, color);
        return particle;
    }
}
