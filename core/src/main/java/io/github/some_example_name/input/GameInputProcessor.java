package io.github.some_example_name.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input;

/**
 * InputProcessor customizado para lidar com input do jogo
 */
public class GameInputProcessor implements InputProcessor {
    
    public boolean wPressed = false;
    public boolean aPressed = false;
    public boolean sPressed = false;
    public boolean dPressed = false;
    public boolean upPressed = false;
    public boolean downPressed = false;
    public boolean rPressed = false;
    public boolean touchPressed = false;
    
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                wPressed = true;
                return true;
            case Input.Keys.A:
                aPressed = true;
                return true;
            case Input.Keys.S:
                sPressed = true;
                return true;
            case Input.Keys.D:
                dPressed = true;
                return true;
            case Input.Keys.UP:
                upPressed = true;
                return true;
            case Input.Keys.DOWN:
                downPressed = true;
                return true;
            case Input.Keys.R:
                rPressed = true;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                wPressed = false;
                return true;
            case Input.Keys.A:
                aPressed = false;
                return true;
            case Input.Keys.S:
                sPressed = false;
                return true;
            case Input.Keys.D:
                dPressed = false;
                return true;
            case Input.Keys.UP:
                upPressed = false;
                return true;
            case Input.Keys.DOWN:
                downPressed = false;
                return true;
            case Input.Keys.R:
                rPressed = false;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            touchPressed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            touchPressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
