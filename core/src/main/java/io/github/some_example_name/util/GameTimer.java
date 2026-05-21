package io.github.some_example_name.util;

/**
 * Timer simples baseado em delta time
 * Cada objeto pode ter seu próprio timer interno
 */
public class GameTimer {
    private float elapsed;
    private float duration;
    public boolean finished; // Public para permitir set direto
    
    public GameTimer(float duration) {
        this.duration = duration;
        this.elapsed = 0f;
        this.finished = false;
    }

    public void update(float delta) {
        if (!finished) {
            elapsed += delta;
            if (elapsed >= duration) {
                finished = true;
                elapsed -= duration;
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public float getProgress() {
        return Math.min(elapsed / duration, 1f);
    }

    public void reset() {
        elapsed = 0f;
        finished = false;
    }

    public boolean consumeFinished() {
        if (finished) {
            finished = false;
            return true;
        }
        return false;
    }

    public float getElapsed() {
        return elapsed;
    }
}
