package io.github.some_example_name.hitbox;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

/**
 * Hitbox composta que agrega múltiplas hitboxes.
 * Se acertar em qualquer uma das hitboxes, acertou no objeto.
 * Útil para personagens mais complexos com múltiplas partes.
 */
public class CompoundHitbox implements Hitbox {
    private final List<Hitbox> hitboxes;
    private final Vector2 center;

    public CompoundHitbox() {
        this.hitboxes = new ArrayList<>();
        this.center = new Vector2();
    }

    public void addHitbox(Hitbox hitbox) {
        hitboxes.add(hitbox);
    }

    public void removeHitbox(Hitbox hitbox) {
        hitboxes.remove(hitbox);
    }

    @Override
    public void update(Vector2 position) {
        this.center.set(position);
        // Cada hitbox componente cuida de sua própria atualização
        // interna conforme necessário. Esta classe apenas coordena.
    }

    /**
     * Colide se QUALQUER uma das hitboxes colide com o ponto
     */
    @Override
    public boolean collidesWith(Vector2 point) {
        for (Hitbox hitbox : hitboxes) {
            if (hitbox.collidesWith(point)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Colide se QUALQUER uma das hitboxes colide com outra hitbox
     */
    @Override
    public boolean collidesWith(Hitbox other) {
        for (Hitbox hitbox : hitboxes) {
            if (hitbox.collidesWith(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        for (Hitbox hitbox : hitboxes) {
            hitbox.render(shapeRenderer);
        }
    }

    @Override
    public Vector2 getCenter() {
        return center;
    }

    public List<Hitbox> getHitboxes() {
        return hitboxes;
    }

    public int getHitboxCount() {
        return hitboxes.size();
    }
}
