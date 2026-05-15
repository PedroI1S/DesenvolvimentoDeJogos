package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.some_example_name.util.GameTimer;

/**
 * Balão de fala com fundo em NinePatch e texto medido com GlyphLayout.
 */
public class SpeechBubble {
    private static final float DEFAULT_DURATION = 2.0f;
    private static final float PADDING_X = 18f;
    private static final float PADDING_Y = 12f;
    private static final float LIFT_DISTANCE = 28f;

    private final BitmapFont font;
    private final GlyphLayout layout;
    private final NinePatch bubblePatch;
    private final Texture bubbleTexture;
    private final GameTimer timer;

    private String text;
    private float anchorX;
    private float anchorY;
    private boolean visible;

    public SpeechBubble(BitmapFont font) {
        this.font = font;
        this.layout = new GlyphLayout();
        this.timer = new GameTimer(DEFAULT_DURATION);
        this.bubbleTexture = createBubbleTexture();
        this.bubblePatch = new NinePatch(bubbleTexture, 8, 8, 8, 8);
        this.text = "";
        this.visible = false;
    }

    public void show(String text, float anchorX, float anchorY) {
        this.text = text;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.visible = true;
        this.timer.reset();
    }

    public void update(float delta) {
        if (!visible) {
            return;
        }

        timer.update(delta);
        if (timer.isFinished()) {
            visible = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!visible || text.isEmpty()) {
            return;
        }

        layout.setText(font, text);

        float progress = timer.getProgress();
        float bubbleScale = 0.92f + 0.08f * (float) Math.sin(progress * Math.PI * 2f);
        float alpha = 1f - (progress * 0.45f);

        float bubbleWidth = (layout.width + PADDING_X * 2f) * bubbleScale;
        float bubbleHeight = (layout.height + PADDING_Y * 2f) * bubbleScale;
        float bubbleX = anchorX - bubbleWidth / 2f;
        float bubbleY = anchorY + LIFT_DISTANCE + (progress * 12f);

        bubblePatch.draw(batch, bubbleX, bubbleY, bubbleWidth, bubbleHeight);

        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, layout,
            bubbleX + (bubbleWidth - layout.width) / 2f,
            bubbleY + (bubbleHeight + layout.height) / 2f - 6f);
        font.setColor(Color.WHITE);
    }

    public void dispose() {
        bubbleTexture.dispose();
    }

    private Texture createBubbleTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.15f, 0.15f, 0.2f, 0.92f));
        pixmap.fill();

        pixmap.setColor(new Color(0.9f, 0.9f, 1f, 0.95f));
        pixmap.drawRectangle(0, 0, 32, 32);

        pixmap.setColor(new Color(0.25f, 0.25f, 0.3f, 0.5f));
        pixmap.drawRectangle(1, 1, 30, 30);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}