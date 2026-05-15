package io.github.some_example_name;

import com.badlogic.gdx.math.Vector2;

/**
 * Sistema de colisão com as paredes do mapa usando RectangleHitbox.
 * Define as 4 paredes como hitboxes retangulares para detectar colisão
 * com player, inimigos e flechas.
 */
public class WallCollider {
    private RectangleHitbox wallTop;
    private RectangleHitbox wallBottom;
    private RectangleHitbox wallLeft;
    private RectangleHitbox wallRight;
    
    public WallCollider(float worldWidth, float worldHeight, float wallLeftX, float wallRightX, 
                        float wallBottomY, float wallTopY) {
        // Parede do topo (linha horizontal no topo)
        // x: 0 a worldWidth, y: wallTopY a worldHeight
        wallTop = new RectangleHitbox(worldWidth, worldHeight - wallTopY);
        wallTop.update(new Vector2(0, wallTopY));
        
        // Parede do fundo (linha horizontal no fundo)
        // x: 0 a worldWidth, y: 0 a wallBottomY
        wallBottom = new RectangleHitbox(worldWidth, wallBottomY);
        wallBottom.update(new Vector2(0, 0));
        
        // Parede da esquerda (linha vertical na esquerda)
        // x: 0 a wallLeftX, y: 0 a worldHeight
        wallLeft = new RectangleHitbox(wallLeftX, worldHeight);
        wallLeft.update(new Vector2(0, 0));
        
        // Parede da direita (linha vertical na direita)
        // x: wallRightX a worldWidth, y: 0 a worldHeight
        float rightWallWidth = worldWidth - wallRightX;
        wallRight = new RectangleHitbox(rightWallWidth, worldHeight);
        wallRight.update(new Vector2(wallRightX, 0));
    }
    
    /**
     * Testa colisão entre uma posição e qualquer parede
     * @param position Posição a testar
     * @return true se colidiu com alguma parede
     */
    public boolean collidesWithWall(Vector2 position) {
        return wallTop.collidesWith(position) || 
               wallBottom.collidesWith(position) ||
               wallLeft.collidesWith(position) ||
               wallRight.collidesWith(position);
    }
    
    /**
     * Testa colisão entre uma hitbox e qualquer parede
     * @param hitbox Hitbox a testar
     * @return true se colidiu com alguma parede
     */
    public boolean collidesWithWall(Hitbox hitbox) {
        return hitbox.collidesWith(wallTop) ||
               hitbox.collidesWith(wallBottom) ||
               hitbox.collidesWith(wallLeft) ||
               hitbox.collidesWith(wallRight);
    }
    
    /**
     * Testa colisão com parede específica
     */
    public boolean collidesWithWallTop(Vector2 position) {
        return wallTop.collidesWith(position);
    }
    
    public boolean collidesWithWallBottom(Vector2 position) {
        return wallBottom.collidesWith(position);
    }
    
    public boolean collidesWithWallLeft(Vector2 position) {
        return wallLeft.collidesWith(position);
    }
    
    public boolean collidesWithWallRight(Vector2 position) {
        return wallRight.collidesWith(position);
    }
    
    // Getters para as hitboxes
    public RectangleHitbox getWallTop() {
        return wallTop;
    }
    
    public RectangleHitbox getWallBottom() {
        return wallBottom;
    }
    
    public RectangleHitbox getWallLeft() {
        return wallLeft;
    }
    
    public RectangleHitbox getWallRight() {
        return wallRight;
    }
}
