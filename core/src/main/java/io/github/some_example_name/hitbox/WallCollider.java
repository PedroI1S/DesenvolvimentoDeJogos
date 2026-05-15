package io.github.some_example_name.hitbox;

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
        float wallThickness = 32f;
        float arenaWidth = wallRightX - wallLeftX;
        float arenaHeight = wallTopY - wallBottomY;

        // Parede do topo: faixa fina sobre o limite superior da arena
        wallTop = new RectangleHitbox(arenaWidth, wallThickness);
        wallTop.update(new Vector2(wallLeftX + arenaWidth / 2f, wallTopY));

        // Parede do fundo: faixa fina sobre o limite inferior da arena
        wallBottom = new RectangleHitbox(arenaWidth, wallThickness);
        wallBottom.update(new Vector2(wallLeftX + arenaWidth / 2f, wallBottomY));

        // Parede da esquerda: faixa fina sobre o limite esquerdo da arena
        wallLeft = new RectangleHitbox(wallThickness, arenaHeight);
        wallLeft.update(new Vector2(wallLeftX, wallBottomY + arenaHeight / 2f));

        // Parede da direita: faixa fina sobre o limite direito da arena
        wallRight = new RectangleHitbox(wallThickness, arenaHeight);
        wallRight.update(new Vector2(wallRightX, wallBottomY + arenaHeight / 2f));
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
