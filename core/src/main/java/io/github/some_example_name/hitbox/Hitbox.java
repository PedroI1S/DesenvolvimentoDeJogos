package io.github.some_example_name.hitbox;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Interface base para todos os tipos de hitbox no jogo.
 * Define o contrato que qualquer hitbox deve implementar.
 */
public interface Hitbox {
    
    /**
     * Verifica se esta hitbox colide com um ponto no espaço
     */
    boolean collidesWith(Vector2 point);
    
    /**
     * Verifica se esta hitbox colide com outra hitbox
     */
    boolean collidesWith(Hitbox other);
    
    /**
     * Atualiza a posição da hitbox (geralmente segue um objeto)
     */
    void update(Vector2 position);
    
    /**
     * Renderiza a hitbox para debug (usando ShapeRenderer)
     */
    void render(ShapeRenderer shapeRenderer);
    
    /**
     * Retorna o centro aproximado da hitbox para efeitos de partículas
     */
    Vector2 getCenter();
}
