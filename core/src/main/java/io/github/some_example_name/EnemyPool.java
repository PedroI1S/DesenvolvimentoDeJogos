package io.github.some_example_name;

/**
 * Object Pool para Enemies
 * Reutiliza instâncias em vez de criar/destruir continuamente
 */
public class EnemyPool extends ObjectPool<Enemy> {

    public EnemyPool(int initialSize, int maxSize) {
        super(initialSize, maxSize);
    }

    @Override
    protected Enemy createNew() {
        // Cria enemy dummy, será inicializado ao obter do pool
        return new Enemy(0, 0);
    }

    @Override
    protected void reset(Enemy enemy) {
        // Reset para estado inicial
        enemy.reset();
    }

    /**
     * Obtém enemy inicializado com posição
     */
    public Enemy obtain(float x, float y) {
        Enemy enemy = obtain();
        enemy.init(x, y);
        return enemy;
    }
}
