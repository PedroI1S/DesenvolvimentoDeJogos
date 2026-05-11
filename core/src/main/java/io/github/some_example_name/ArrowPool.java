package io.github.some_example_name;

import com.badlogic.gdx.audio.Sound;

/**
 * Object Pool para Arrows
 * Reutiliza instâncias em vez de criar/destruir continuamente
 */
public class ArrowPool extends ObjectPool<Arrow> {
    private Sound shootSound;

    public ArrowPool(Sound shootSound, int initialSize, int maxSize) {
        super(initialSize, maxSize);
        this.shootSound = shootSound;
    }

    @Override
    protected Arrow createNew() {
        // Cria arrow dummy, será inicializado ao obter do pool
        return new Arrow(0, 0, 0, shootSound);
    }

    @Override
    protected void reset(Arrow arrow) {
        // Reset para estado inicial
        arrow.reset();
    }

    /**
     * Obtém arrow inicializado com posição e ângulo
     */
    public Arrow obtain(float x, float y, float angle) {
        Arrow arrow = obtain();
        arrow.init(x, y, angle);
        arrow.playShootSound();
        return arrow;
    }
}
