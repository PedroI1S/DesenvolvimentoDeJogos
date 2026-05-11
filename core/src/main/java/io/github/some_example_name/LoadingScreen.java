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
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Loading Screen - Carrega todos os assets antes de iniciar o jogo
 */
public class LoadingScreen implements Screen {
    private final Main game;
    private final AssetManager assetManager;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera camera;
    
    private float loadProgress = 0f;
    private boolean loadingComplete = false;
    private String loadingMessage = "Iniciando...";

    public LoadingScreen(Main game) {
        this.game = game;
        this.assetManager = new AssetManager();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(2f);
        this.font.setColor(Color.WHITE);
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1024, 768);
        
        Gdx.app.log("LoadingScreen", "Iniciando carregamento de assets...");
        startLoadingAssets();
    }

    private void startLoadingAssets() {
        Gdx.app.log("Loading", "Loading arrow_swish.mp3...");
        assetManager.load("sounds/arrow_swish.mp3", Sound.class);
        
        Gdx.app.log("Loading", "Loading arrow_impact.mp3...");
        assetManager.load("sounds/arrow_impact.mp3", Sound.class);
        
        Gdx.app.log("Loading", "Loading heart_of_oak.mp3...");
        assetManager.load("sounds/heart_of_oak.mp3", Music.class);
    }

    @Override
    public void render(float delta) {
        // Atualiza o carregamento
        if (!loadingComplete) {
            if (assetManager.update()) {
                // Carregamento completo
                loadingComplete = true;
                loadingMessage = "Carregamento Completo!";
                Gdx.app.log("Loading", "All assets loaded!");
                
                // Verifica quais assets foram carregados
                verifyAssets();
                
                // Aguarda um frame antes de transicionar
            } else {
                // Ainda carregando
                loadProgress = assetManager.getProgress();
                loadingMessage = "Carregando... " + (int)(loadProgress * 100) + "%";
            }
        } else {
            // Carregamento completo - transiciona para o jogo
            game.setLoadedAssets(assetManager);
            game.setScreen(new GameScreen(assetManager));
            return;
        }

        // Renderiza a tela de loading
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
        font.draw(batch, "Bow and Arrow", 400, 450);
        font.draw(batch, loadingMessage, 350, 350);
        
        // Desenha uma barra de progresso simples
        batch.end();
        
        // Desenha a barra com ShapeRenderer
        com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = 
            new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        
        // Barra de fundo
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(300, 300, 400, 30);
        
        // Barra de progresso
        shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f);
        shapeRenderer.rect(300, 300, 400 * loadProgress, 30);
        
        shapeRenderer.end();
        shapeRenderer.dispose();
    }

    private void verifyAssets() {
        Sound shootSound = null;
        Sound impactSound = null;
        Music backgroundMusic = null;
        
        try {
            if (assetManager.isLoaded("sounds/arrow_swish.mp3")) {
                shootSound = assetManager.get("sounds/arrow_swish.mp3", Sound.class);
                Gdx.app.log("LoadingScreen", "shootSound loaded: " + (shootSound != null));
            }
            
            if (assetManager.isLoaded("sounds/arrow_impact.mp3")) {
                impactSound = assetManager.get("sounds/arrow_impact.mp3", Sound.class);
                Gdx.app.log("LoadingScreen", "impactSound loaded: " + (impactSound != null));
            }
            
            if (assetManager.isLoaded("sounds/heart_of_oak.mp3")) {
                backgroundMusic = assetManager.get("sounds/heart_of_oak.mp3", Music.class);
                Gdx.app.log("LoadingScreen", "backgroundMusic loaded: " + (backgroundMusic != null));
            }
        } catch (Exception e) {
            Gdx.app.error("LoadingScreen", "Error verifying assets: " + e.getMessage());
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    @Override
    public void show() {}
}
