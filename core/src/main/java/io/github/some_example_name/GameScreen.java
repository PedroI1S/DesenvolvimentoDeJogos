package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

/**
 * GameScreen - Tela principal do jogo
 */
public class GameScreen implements Screen {
    private final AssetManager assetManager;
    
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;
    private BitmapFont font;
    
    private static final float VIEWPORT_WIDTH = 1024f;
    private static final float VIEWPORT_HEIGHT = 768f;
    private static final float WORLD_WIDTH = 4096f;
    private static final float WORLD_HEIGHT = 3072f;
    private static final float MAX_VIEW_HALF_WIDTH = VIEWPORT_WIDTH * 0.5f * 3f;
    private static final float MAX_VIEW_HALF_HEIGHT = VIEWPORT_HEIGHT * 0.5f * 3f;
    private static final float WALL_LEFT = MAX_VIEW_HALF_WIDTH;
    private static final float WALL_RIGHT = WORLD_WIDTH - MAX_VIEW_HALF_WIDTH;
    private static final float WALL_BOTTOM = MAX_VIEW_HALF_HEIGHT;
    private static final float WALL_TOP = WORLD_HEIGHT - MAX_VIEW_HALF_HEIGHT;
    
    private Sound shootSound;
    private Sound impactSound;
    private Music backgroundMusic;
    
    private Archer archer;
    private ArrowPool arrowPool;
    private EnemyPool enemyPool;
    private SpeechBubble speechBubble;
    
    private float currentZoom = 1f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 3f;
    private static final float CAMERA_SPEED = 300f;
    private static final float ZOOM_SPEED = 1.5f;
    
    private int arrowsShot = 0;
    private int enemiesKilled = 0;
    private Vector2 mouseWorldPos;
    private GameTimer spawnTimer;
    private static final float SPAWN_INTERVAL = 4f;
    
    private GameInputProcessor inputProcessor;
    private final Vector3 touchPoint = new Vector3();

    public GameScreen(AssetManager assetManager) {
        this.assetManager = assetManager;
        
        // Carrega assets do AssetManager
        try {
            shootSound = assetManager.get("sounds/arrow_swish.mp3", Sound.class);
            Gdx.app.log("GameScreen", "shootSound: " + (shootSound != null));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error getting shootSound: " + e.getMessage());
            shootSound = null;
        }
        
        try {
            impactSound = assetManager.get("sounds/arrow_impact.mp3", Sound.class);
            Gdx.app.log("GameScreen", "impactSound: " + (impactSound != null));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error getting impactSound: " + e.getMessage());
            impactSound = null;
        }
        
        try {
            backgroundMusic = assetManager.get("sounds/heart_of_oak.mp3", Music.class);
            Gdx.app.log("GameScreen", "backgroundMusic: " + (backgroundMusic != null));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error getting backgroundMusic: " + e.getMessage());
            backgroundMusic = null;
        }
    }

    @Override
    public void show() {
        // Inicializa sistemas gráficos
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        camera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        
        // Inicializa input processor
        inputProcessor = new GameInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
        
        // Inicializa object pools
        arrowPool = new ArrowPool(shootSound, 10, 50);
        enemyPool = new EnemyPool(5, 30);
        
        // Inicializa objetos do jogo
        archer = new Archer(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, shootSound);
        speechBubble = new SpeechBubble(font);
        mouseWorldPos = new Vector2();
        spawnTimer = new GameTimer(SPAWN_INTERVAL);
        
        // Spawna inimigos iniciais
        spawnEnemy();
        spawnEnemy();
        
        // Inicia música
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.play();
            Gdx.app.log("GameScreen", "Background music started");
        }
        
        Gdx.graphics.setTitle("Bow and Arrow - WASD: Move | UP/DOWN: Zoom | R: Reset | CLICK: Shoot");
    }

    @Override
    public void render(float delta) {
        float cappedDelta = Math.min(delta, 0.016f); // Cap at 60fps
        
        // Atualiza lógica
        update(cappedDelta);
        
        // Renderiza
        ScreenUtils.clear(0.2f, 0.2f, 0.25f, 1f);
        
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Renderiza fundo
        renderBackground();
        
        // Renderiza game objects
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        archer.render(shapeRenderer);
        
        // Renderiza arrows do pool
        for (Arrow arrow : arrowPool.getInUse()) {
            if (arrow.isAlive()) {
                arrow.render(shapeRenderer);
            }
        }
        
        // Renderiza enemies do pool
        for (Enemy enemy : enemyPool.getInUse()) {
            if (enemy.isAlive()) {
                enemy.render(shapeRenderer);
            }
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        speechBubble.render(batch);
        batch.end();
        
        // Renderiza UI
        batch.begin();
        renderUI();
        batch.end();
    }

    private void update(float delta) {
        // Processa input com InputProcessor
        processInput(delta);

        speechBubble.update(delta);
        
        // Atualiza câmera
        updateCamera(delta);
        
        // Converte posição do mouse para coordenadas do mundo
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPoint);
        mouseWorldPos.set(touchPoint.x, touchPoint.y);
        
        // Atualiza archer apontando para o mouse
        archer.update(delta, mouseWorldPos.x, mouseWorldPos.y);
        
        // Atualiza arrows (usando pool)
        List<Arrow> arrowsInUse = arrowPool.getInUse();
        for (Arrow arrow : arrowsInUse) {
            if (arrow.isAlive()) {
                arrow.update(delta);
            }
        }
        
        // Atualiza inimigos (usando pool)
        List<Enemy> enemiesInUse = enemyPool.getInUse();
        for (Enemy enemy : enemiesInUse) {
            if (enemy.isAlive()) {
                enemy.update(delta, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
            }
        }
        
        // Detecta colisões entre flechas e inimigos
        for (int i = arrowsInUse.size() - 1; i >= 0; i--) {
            Arrow arrow = arrowsInUse.get(i);
            if (!arrow.isAlive()) continue;
            
            for (int j = enemiesInUse.size() - 1; j >= 0; j--) {
                Enemy enemy = enemiesInUse.get(j);
                if (!enemy.isAlive()) continue;
                
                if (enemy.collidesWith(arrow.getPosition(), 5f)) {
                    Gdx.app.log("Collision", "Arrow hit enemy!");
                    enemy.kill();
                    arrow.getLifeTimer().finished = true;
                    speechBubble.show("Acertou!", enemy.getPosition().x, enemy.getPosition().y + enemy.getRadius());
                    
                    if (impactSound != null) {
                        try {
                            impactSound.play();
                            Gdx.app.log("Impact", "Impact sound played!");
                        } catch (Exception e) {
                            Gdx.app.error("Impact", "Error playing impact sound: " + e.getMessage());
                        }
                    } else {
                        Gdx.app.log("Impact", "impactSound is NULL!");
                    }
                    
                    enemiesKilled++;
                }
            }
        }
        
        // Remove arrows mortas do pool
        for (int i = arrowsInUse.size() - 1; i >= 0; i--) {
            Arrow arrow = arrowsInUse.get(i);
            if (!arrow.isAlive()) {
                arrowPool.free(arrow);
            }
        }
        
        // Remove inimigos mortos do pool
        for (int i = enemiesInUse.size() - 1; i >= 0; i--) {
            Enemy enemy = enemiesInUse.get(i);
            if (!enemy.isAlive()) {
                enemyPool.free(enemy);
            }
        }
        
        // Spawna novo inimigo
        spawnTimer.update(delta);
        if (spawnTimer.isFinished()) {
            spawnEnemy();
            spawnTimer.reset();
        }
    }

    private void processInput(float delta) {
        // Movimento do jogador com WASD
        float moveSpeed = CAMERA_SPEED * delta;
        
        if (inputProcessor.wPressed) {
            archer.move(0f, moveSpeed, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        if (inputProcessor.sPressed) {
            archer.move(0f, -moveSpeed, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        if (inputProcessor.aPressed) {
            archer.move(-moveSpeed, 0f, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        if (inputProcessor.dPressed) {
            archer.move(moveSpeed, 0f, WALL_LEFT, WALL_RIGHT, WALL_BOTTOM, WALL_TOP);
        }
        
        // Zoom com UP/DOWN
        if (inputProcessor.upPressed) {
            currentZoom = Math.min(currentZoom + ZOOM_SPEED * delta, MAX_ZOOM);
        }
        if (inputProcessor.downPressed) {
            currentZoom = Math.max(currentZoom - ZOOM_SPEED * delta, MIN_ZOOM);
        }
        
        // Disparo de flecha com clique do mouse
        if (inputProcessor.touchPressed && archer.canShoot()) {
            arrowPool.obtain(archer.getPosition().x, archer.getPosition().y, archer.getRotation());
            archer.registerShot();
            arrowsShot++;
            speechBubble.show("Tiro!", archer.getPosition().x, archer.getPosition().y);
            inputProcessor.touchPressed = false; // Evita múltiplos disparos
        }
        
        // Reset de câmera com R
        if (inputProcessor.rPressed) {
            archer.setPosition((WALL_LEFT + WALL_RIGHT) / 2f, (WALL_BOTTOM + WALL_TOP) / 2f);
            currentZoom = 1f;
            inputProcessor.rPressed = false;
        }
    }

    private void updateCamera(float delta) {
        // Mantém a câmera centrada no personagem
        camera.position.set(archer.getPosition().x, archer.getPosition().y, 0);
        
        // Aplica zoom: currentZoom = 1.0 é normal, > 1.0 é zoom in, < 1.0 é zoom out
        // libGDX: camera.zoom = 1.0 é normal, > 1.0 é zoom out, < 1.0 é zoom in
        // Então: camera.zoom = 1.0 / currentZoom
        camera.zoom = 1.0f / currentZoom;

        float halfWidth = (camera.viewportWidth * camera.zoom) / 2f;
        float halfHeight = (camera.viewportHeight * camera.zoom) / 2f;
        camera.position.x = Math.max(halfWidth, Math.min(camera.position.x, WORLD_WIDTH - halfWidth));
        camera.position.y = Math.max(halfHeight, Math.min(camera.position.y, WORLD_HEIGHT - halfHeight));
        
        camera.update();
    }

    private void renderBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Área externa às paredes invisíveis: mais escura
        shapeRenderer.setColor(0.12f, 0.12f, 0.14f, 1f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Área jogável interna: cinza principal
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(WALL_LEFT, WALL_BOTTOM, WALL_RIGHT - WALL_LEFT, WALL_TOP - WALL_BOTTOM);
        
        shapeRenderer.end();
    }

    private void renderUI() {
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudCamera.combined);
        
        font.draw(batch, "Arrows Shot: " + arrowsShot, 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "Arrows Pool: " + arrowPool.getInUseCount() + " in use / " + arrowPool.getAvailableCount() + " available", 20, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "Enemies Killed: " + enemiesKilled, 20, Gdx.graphics.getHeight() - 80);
        font.draw(batch, "Enemies Pool: " + enemyPool.getInUseCount() + " in use / " + enemyPool.getAvailableCount() + " available", 20, Gdx.graphics.getHeight() - 110);
        font.draw(batch, "Zoom: " + String.format("%.2f", currentZoom) + "x", 20, Gdx.graphics.getHeight() - 140);
        font.draw(batch, "WASD: Move | UP/DOWN: Zoom | R: Reset | CLICK: Shoot", 20, 20);
    }

    private void spawnEnemy() {
        // Spawna inimigo em posição aleatória na borda do mundo
        float x, y;
        int side = (int) (Math.random() * 4);
        
        switch (side) {
            case 0: // Topo
                x = WALL_LEFT + (float) (Math.random() * (WALL_RIGHT - WALL_LEFT));
                y = WALL_TOP - 50;
                break;
            case 1: // Fundo
                x = WALL_LEFT + (float) (Math.random() * (WALL_RIGHT - WALL_LEFT));
                y = WALL_BOTTOM + 50;
                break;
            case 2: // Esquerda
                x = WALL_LEFT + 50;
                y = WALL_BOTTOM + (float) (Math.random() * (WALL_TOP - WALL_BOTTOM));
                break;
            default: // Direita
                x = WALL_RIGHT - 50;
                y = WALL_BOTTOM + (float) (Math.random() * (WALL_TOP - WALL_BOTTOM));
                break;
        }
        
        enemyPool.obtain(x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (speechBubble != null) speechBubble.dispose();
    }
}
