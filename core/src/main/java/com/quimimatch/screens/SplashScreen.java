package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.quimimatch.QuimiMatchGame;

public class SplashScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer shapeRenderer;
    private float timer = 0f;
    private static final float DURATION = 2f;

    public SplashScreen(QuimiMatchGame game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        timer += delta;

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float cx = Gdx.graphics.getWidth()  / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;
        float progress = Math.min(timer / DURATION, 1f);
        float barW = Gdx.graphics.getWidth() * 0.6f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Logo placeholder
        shapeRenderer.setColor(0.15f, 0.25f, 0.55f, 1f);
        shapeRenderer.rect(cx - 200, cy - 50, 400, 100);
        shapeRenderer.setColor(0.3f, 0.6f, 1f, 1f);
        shapeRenderer.rect(cx - 200, cy - 50, 400, 5);
        shapeRenderer.rect(cx - 200, cy + 45, 400, 5);

        // Barra de carga
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(cx - barW / 2f, cy - 110, barW, 18);
        shapeRenderer.setColor(0.4f, 0.8f, 1f, 1f);
        shapeRenderer.rect(cx - barW / 2f, cy - 110, barW * progress, 18);

        shapeRenderer.end();

        if (timer >= DURATION) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void hide()    { dispose(); }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
