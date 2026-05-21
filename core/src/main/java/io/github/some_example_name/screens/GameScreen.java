package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

import io.github.some_example_name.entities.Archer;
import io.github.some_example_name.entities.Arrow;
import io.github.some_example_name.entities.Enemy;
import io.github.some_example_name.entities.EnemyBoss;
import io.github.some_example_name.entities.Particle;
import io.github.some_example_name.entities.SpeechBubble;
import io.github.some_example_name.hitbox.CircleHitbox;
import io.github.some_example_name.hitbox.WallCollider;
import io.github.some_example_name.input.GameInputProcessor;
import io.github.some_example_name.pools.ArrowPool;
import io.github.some_example_name.pools.EnemyPool;
import io.github.some_example_name.pools.ParticlePool;
import io.github.some_example_name.util.GameTimer;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;
    private BitmapFont font;

    private static final float VIEWPORT_WIDTH  = 1024f;
    private static final float VIEWPORT_HEIGHT  = 768f;
    private static final float WORLD_WIDTH      = 4096f;
    private static final float WORLD_HEIGHT     = 3072f;
    private static final float MAX_VIEW_HALF_WIDTH  = VIEWPORT_WIDTH  * 0.5f * 3f;
    private static final float MAX_VIEW_HALF_HEIGHT = VIEWPORT_HEIGHT * 0.5f * 3f;
    private static final float WALL_LEFT   = MAX_VIEW_HALF_WIDTH;
    private static final float WALL_RIGHT  = WORLD_WIDTH  - MAX_VIEW_HALF_WIDTH;
    private static final float WALL_BOTTOM = MAX_VIEW_HALF_HEIGHT;
    private static final float WALL_TOP    = WORLD_HEIGHT - MAX_VIEW_HALF_HEIGHT;

    private Sound shootSound;
    private Sound impactSound;
    private Music backgroundMusic;
    private TextureAtlas enemyAtlas;
    private TextureAtlas characterAtlas;

    private Archer archer;
    private ArrowPool arrowPool;
    private EnemyPool enemyPool;
    private ParticlePool particlePool;
    private SpeechBubble speechBubble;
    private EnemyBoss boss;
    private WallCollider wallCollider;

    // Reusable arrow hitbox — avoids per-frame allocation during collision checks
    private final CircleHitbox arrowCheckHitbox = new CircleHitbox(5f);

    private float currentZoom = 1f;
    private static final float MIN_ZOOM    = 0.5f;
    private static final float MAX_ZOOM    = 3f;
    private static final float CAMERA_SPEED = 300f;
    private static final float ZOOM_SPEED   = 1.5f;

    private int arrowsShot    = 0;
    private int enemiesKilled = 0;
    private Vector2 mouseWorldPos;
    private GameTimer spawnTimer;
    private static final float SPAWN_INTERVAL = 4f;

    private GameInputProcessor inputProcessor;
    private final Vector3 touchPoint = new Vector3();
    private boolean debugHitboxes = false;

    public GameScreen(AssetManager assetManager) {
        try { shootSound      = assetManager.get("sounds/arrow_swish.mp3", Sound.class); }
        catch (Exception e) { Gdx.app.error("GameScreen", "shootSound: " + e.getMessage()); }

        try { impactSound     = assetManager.get("sounds/arrow_impact.mp3", Sound.class); }
        catch (Exception e) { Gdx.app.error("GameScreen", "impactSound: " + e.getMessage()); }

        try { backgroundMusic = assetManager.get("sounds/heart_of_oak.mp3", Music.class); }
        catch (Exception e) { Gdx.app.error("GameScreen", "music: " + e.getMessage()); }

        try { enemyAtlas      = assetManager.get("enemies/Enemies.atlas", TextureAtlas.class); }
        catch (Exception e) { Gdx.app.error("GameScreen", "enemyAtlas: " + e.getMessage()); }

        try { characterAtlas  = assetManager.get("character/ArcherWalking.atlas", TextureAtlas.class); }
        catch (Exception e) { Gdx.app.error("GameScreen", "characterAtlas: " + e.getMessage()); }
    }

    @Override
    public void show() {
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera    = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        viewport  = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);

        inputProcessor = new GameInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);

        arrowPool    = new ArrowPool(shootSound, 10, 50);
        enemyPool    = new EnemyPool(30, enemyAtlas);
        particlePool = new ParticlePool(20, 100);

        wallCollider = new WallCollider(WORLD_WIDTH, WORLD_HEIGHT, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);

        archer       = new Archer(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, shootSound, characterAtlas);
        speechBubble = new SpeechBubble(font);
        mouseWorldPos = new Vector2();
        spawnTimer   = new GameTimer(SPAWN_INTERVAL);

        spawnEnemy();
        spawnEnemy();

        boss = new EnemyBoss(WORLD_WIDTH * 0.75f, WORLD_HEIGHT / 2f, enemyAtlas);

        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.play();
        }

        Gdx.graphics.setTitle("Bow and Arrow - WASD: Move | UP/DOWN: Zoom | R: Reset | CLICK: Shoot | H: Hitboxes");
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 0.016f);
        update(dt);

        ScreenUtils.clear(0.2f, 0.2f, 0.25f, 1f);

        // 1. Background (ShapeRenderer)
        renderBackground();

        // 2. Animated sprites: archer, enemies, boss, speech bubble (SpriteBatch)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        archer.render(batch);
        for (Enemy enemy : enemyPool.getInUse()) {
            if (!enemy.isDead()) enemy.render(batch);
        }
        if (boss != null && !boss.isDead()) boss.render(batch);
        speechBubble.render(batch);
        batch.end();

        // 3. Shape-only objects: arrows, particles, debug hitboxes (ShapeRenderer)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Arrow arrow : arrowPool.getInUse()) {
            if (arrow.isAlive()) arrow.render(shapeRenderer);
        }
        for (Particle particle : particlePool.getInUse()) {
            if (particle.isAlive()) particle.render(shapeRenderer);
        }
        if (debugHitboxes) {
            archer.getHitbox().render(shapeRenderer);
            for (Enemy enemy : enemyPool.getInUse()) {
                if (!enemy.isDead()) enemy.getHitbox().render(shapeRenderer);
            }
            if (boss != null && !boss.isDead()) boss.getHitbox().render(shapeRenderer);
            shapeRenderer.setColor(0f, 1f, 0f, 0.5f);
            wallCollider.getWallTop().render(shapeRenderer);
            wallCollider.getWallBottom().render(shapeRenderer);
            wallCollider.getWallLeft().render(shapeRenderer);
            wallCollider.getWallRight().render(shapeRenderer);
        }
        shapeRenderer.end();

        // 4. HUD (SpriteBatch, hudCamera)
        batch.begin();
        renderUI();
        batch.end();
    }

    private void update(float delta) {
        processInput(delta);
        speechBubble.update(delta);
        updateCamera(delta);

        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPoint);
        mouseWorldPos.set(touchPoint.x, touchPoint.y);
        archer.update(delta, mouseWorldPos.x, mouseWorldPos.y);

        // Update arrows
        for (Arrow arrow : arrowPool.getInUse()) {
            if (arrow.isAlive()) {
                arrow.update(delta);
                if (wallCollider.collidesWithWall(arrow.getPosition())) {
                    arrow.getLifeTimer().finished = true;
                }
            }
        }

        // Update enemies — call update even while dying so death animation advances
        for (Enemy enemy : enemyPool.getInUse()) {
            if (!enemy.isDead()) {
                enemy.update(delta, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
            }
        }

        // Update boss
        if (boss != null && !boss.isDead()) {
            boss.update(delta, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }

        // Arrow ↔ enemy collisions (only alive enemies can be hit)
        List<Arrow> arrows  = arrowPool.getInUse();
        List<Enemy> enemies = enemyPool.getInUse();
        for (int i = arrows.size() - 1; i >= 0; i--) {
            Arrow arrow = arrows.get(i);
            if (!arrow.isAlive()) continue;

            arrowCheckHitbox.update(arrow.getPosition());

            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (!enemy.isAlive()) continue;

                if (enemy.collidesWithHitbox(arrowCheckHitbox)) {
                    Gdx.app.log("Collision", "Arrow hit " + enemy.getType());
                    enemy.kill();
                    arrow.getLifeTimer().finished = true;

                    Vector2 pos = enemy.getPosition();
                    speechBubble.show("Acertou!", pos.x, pos.y + enemy.getRadius());
                    createImpactParticles(pos.x, pos.y);

                    if (impactSound != null) { try { impactSound.play(); } catch (Exception ignored) {} }
                    enemiesKilled++;
                    break;
                }
            }
        }

        // Arrow ↔ boss collision
        if (boss != null && boss.isAlive()) {
            for (int i = arrows.size() - 1; i >= 0; i--) {
                Arrow arrow = arrows.get(i);
                if (!arrow.isAlive()) continue;

                arrowCheckHitbox.update(arrow.getPosition());
                if (boss.collidesWithHitbox(arrowCheckHitbox)) {
                    Gdx.app.log("Collision", "Arrow hit boss!");
                    boss.kill();
                    arrow.getLifeTimer().finished = true;

                    Vector2 pos = boss.getPosition();
                    speechBubble.show("Boss Derrotado!", pos.x, pos.y + 50f);
                    for (int p = 0; p < 3; p++) createImpactParticles(pos.x, pos.y);
                    if (impactSound != null) { try { impactSound.play(); } catch (Exception ignored) {} }
                    enemiesKilled += 5;
                    break;
                }
            }
        }

        // Free dead arrows
        for (int i = arrows.size() - 1; i >= 0; i--) {
            if (!arrows.get(i).isAlive()) arrowPool.free(arrows.get(i));
        }

        // Free fully dead enemies (still dying → keep until animation ends)
        for (int i = enemies.size() - 1; i >= 0; i--) {
            if (enemies.get(i).isDead()) enemyPool.free(enemies.get(i));
        }

        // Update & free particles
        List<Particle> particles = particlePool.getInUse();
        for (Particle p : particles) {
            if (p.isAlive()) p.update(delta);
        }
        for (int i = particles.size() - 1; i >= 0; i--) {
            if (!particles.get(i).isAlive()) particlePool.free(particles.get(i));
        }

        // Spawn new enemy
        spawnTimer.update(delta);
        if (spawnTimer.isFinished()) {
            spawnEnemy();
            spawnTimer.reset();
        }
    }

    private void processInput(float delta) {
        float moveSpeed = CAMERA_SPEED * delta;

        if (inputProcessor.wPressed) {
            archer.move(0f, moveSpeed, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
            if (wallCollider.collidesWithWall(archer.getHitbox()))
                archer.move(0f, -moveSpeed, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        if (inputProcessor.sPressed) {
            archer.move(0f, -moveSpeed, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
            if (wallCollider.collidesWithWall(archer.getHitbox()))
                archer.move(0f, moveSpeed, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        if (inputProcessor.aPressed) {
            archer.move(-moveSpeed, 0f, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
            if (wallCollider.collidesWithWall(archer.getHitbox()))
                archer.move(moveSpeed, 0f, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        if (inputProcessor.dPressed) {
            archer.move(moveSpeed, 0f, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
            if (wallCollider.collidesWithWall(archer.getHitbox()))
                archer.move(-moveSpeed, 0f, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }

        if (inputProcessor.upPressed)   currentZoom = Math.min(currentZoom + ZOOM_SPEED * delta, MAX_ZOOM);
        if (inputProcessor.downPressed) currentZoom = Math.max(currentZoom - ZOOM_SPEED * delta, MIN_ZOOM);

        if (inputProcessor.touchPressed && archer.canShoot()) {
            arrowPool.obtain(archer.getPosition().x, archer.getPosition().y, archer.getRotation());
            archer.registerShot();
            arrowsShot++;
            speechBubble.show("Tiro!", archer.getPosition().x, archer.getPosition().y);
            inputProcessor.touchPressed = false;
        }

        if (inputProcessor.rPressed) {
            archer.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
            currentZoom = 1f;
            inputProcessor.rPressed = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            debugHitboxes = !debugHitboxes;
        }
    }

    private void updateCamera(float delta) {
        camera.position.set(archer.getPosition().x, archer.getPosition().y, 0);
        camera.zoom = 1.0f / currentZoom;

        float halfW = (camera.viewportWidth  * camera.zoom) / 2f;
        float halfH = (camera.viewportHeight * camera.zoom) / 2f;
        camera.position.x = Math.max(halfW, Math.min(camera.position.x, WORLD_WIDTH  - halfW));
        camera.position.y = Math.max(halfH, Math.min(camera.position.y, WORLD_HEIGHT - halfH));
        camera.update();
    }

    private void renderBackground() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.12f, 0.14f, 1f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(WALL_LEFT, WALL_BOTTOM, WALL_RIGHT - WALL_LEFT, WALL_TOP - WALL_BOTTOM);
        shapeRenderer.end();
    }

    private void renderUI() {
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudCamera.combined);

        int y = Gdx.graphics.getHeight() - 20;
        font.draw(batch, "Arrows Shot: " + arrowsShot,                                                          20, y);
        font.draw(batch, "Arrows Pool: " + arrowPool.getInUseCount() + " in use / " + arrowPool.getAvailableCount() + " available", 20, y -= 30);
        font.draw(batch, "Enemies Killed: " + enemiesKilled,                                                     20, y -= 30);
        font.draw(batch, "Enemies Pool: " + enemyPool.getInUseCount() + " in use / " + enemyPool.getAvailableCount() + " available", 20, y -= 30);
        font.draw(batch, "Particles: " + particlePool.getInUseCount() + " in use / " + particlePool.getAvailableCount() + " available", 20, y -= 30);
        font.draw(batch, "Zoom: " + String.format("%.2f", currentZoom) + "x",                                   20, y -= 30);
        font.draw(batch, "Debug Hitboxes: " + (debugHitboxes ? "ON [H]" : "OFF [H]"),                           20, y -= 30);
        font.draw(batch, "WASD: Move | UP/DOWN: Zoom | R: Reset | CLICK: Shoot | H: Toggle Hitboxes",           20, 20);
    }

    private void createImpactParticles(float x, float y) {
        Color[] colors = { Color.RED, Color.ORANGE, Color.YELLOW, Color.YELLOW };
        int count = 8;
        for (int i = 0; i < count; i++) {
            float angle = (float)(Math.PI * 2 * i / count);
            particlePool.obtain(x, y,
                (float)Math.cos(angle) * 200f,
                (float)Math.sin(angle) * 200f,
                0.5f, 8f, colors[i % colors.length]);
        }
    }

    private void spawnEnemy() {
        float x, y;
        int side = (int)(Math.random() * 4);
        switch (side) {
            case 0: x = WALL_LEFT  + (float)(Math.random() * (WALL_RIGHT - WALL_LEFT)); y = WALL_TOP    - 60; break;
            case 1: x = WALL_LEFT  + (float)(Math.random() * (WALL_RIGHT - WALL_LEFT)); y = WALL_BOTTOM + 60; break;
            case 2: x = WALL_LEFT  + 60; y = WALL_BOTTOM + (float)(Math.random() * (WALL_TOP - WALL_BOTTOM)); break;
            default:x = WALL_RIGHT - 60; y = WALL_BOTTOM + (float)(Math.random() * (WALL_TOP - WALL_BOTTOM)); break;
        }
        enemyPool.obtain(x, y);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        if (batch != null)         batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null)          font.dispose();
        if (speechBubble != null)  speechBubble.dispose();
    }
}
