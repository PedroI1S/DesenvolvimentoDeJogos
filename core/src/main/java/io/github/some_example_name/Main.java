package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

import io.github.some_example_name.screens.LoadingScreen;

/**
 * Projeto Bow and Arrow - Protótipo minimalista
 * Gerenciador de screens com carregamento de assets via LoadingScreen
 */
public class Main extends Game {
    private AssetManager assetManager;

    @Override
    public void create() {
        Gdx.app.log("Main", "Game created. Starting LoadingScreen...");
        // Inicia com a tela de carregamento
        setScreen(new LoadingScreen(this));
    }

    /**
     * Define o AssetManager depois que os assets foram carregados
     */
    public void setLoadedAssets(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Retorna o AssetManager para o GameScreen
     */
    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assetManager != null) {
            assetManager.dispose();
        }
    }
}
