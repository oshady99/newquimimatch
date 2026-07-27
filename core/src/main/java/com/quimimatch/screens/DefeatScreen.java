package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.managers.GameSession;

public class DefeatScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontBig;
    private BitmapFont font;
    private GlyphLayout layout;

    private float btnRetryX, btnRetryY, btnRetryW, btnRetryH;
    private float btnMenuX,  btnMenuY,  btnMenuW,  btnMenuH;

    public DefeatScreen(QuimiMatchGame game) {
        this.game     = game;
        shapeRenderer = new ShapeRenderer();
        fontBig       = new BitmapFont();
        fontBig.getData().setScale(3.4f);
        font          = new BitmapFont();
        font.getData().setScale(2.0f);
        layout        = new GlyphLayout();

        GameSession.get().loseLife();

        float cx = Gdx.graphics.getWidth()  / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;

        btnRetryW = 280; btnRetryH = 70;
        btnRetryX = cx - btnRetryW / 2f;
        btnRetryY = cy - 140;

        btnMenuW = 200; btnMenuH = 55;
        btnMenuX = cx - btnMenuW / 2f;
        btnMenuY = cy - 230;
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.03f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float cx = sw / 2f;
        float cy = sh / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Panel
        shapeRenderer.setColor(0.2f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(cx - 300, cy - 260, 600, 460);

        // Vidas restantes
        drawLives(shapeRenderer, cx, cy + 60);

        // Botón REINTENTAR
        boolean hasLives = GameSession.get().hasLives();
        shapeRenderer.setColor(hasLives ? new Color(0.8f, 0.2f, 0.2f, 1f)
                                        : new Color(0.4f, 0.4f, 0.4f, 1f));
        shapeRenderer.rect(btnRetryX, btnRetryY, btnRetryW, btnRetryH);

        // Botón MENÚ
        shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
        shapeRenderer.rect(btnMenuX, btnMenuY, btnMenuW, btnMenuH);

        shapeRenderer.end();

        game.batch.begin();

        fontBig.setColor(new Color(1f, 0.3f, 0.3f, 1f));
        drawCentered(fontBig, "SIN MOVIMIENTOS", cx, cy + 220);

        font.setColor(Color.WHITE);
        drawCentered(font, "Puntos: " + GameSession.get().getScore(), cx, cy + 150);
        drawCentered(font, "Vidas: " + GameSession.get().getLives() + " / 5", cx, cy + 110);

        // Progreso de objetivos
        GameSession gs = GameSession.get();
        for (int i = 0; i < gs.getConfig().goalAtoms.length; i++) {
            int collected = gs.getCollected()[i];
            int needed    = gs.getConfig().goalAmounts[i];
            String atom   = gs.getConfig().goalAtoms[i].getSymbol();
            font.setColor(collected >= needed ? Color.GREEN : Color.RED);
            drawCentered(font, atom + ": " + collected + "/" + needed,
                    cx, cy + 60 - i * 35);
        }

        font.setColor(Color.WHITE);
        String retryLabel = GameSession.get().hasLives() ? "REINTENTAR" : "SIN VIDAS";
        drawCentered(font, retryLabel, cx, btnRetryY + 44);

        font.setColor(new Color(0.8f, 0.8f, 1f, 1f));
        drawCentered(font, "MENÚ", cx, btnMenuY + 36);

        game.batch.end();

        handleInput();
    }

    private void drawLives(ShapeRenderer sr, float cx, float cy) {
        int lives = GameSession.get().getLives();
        for (int i = 0; i < 5; i++) {
            sr.setColor(i < lives ? new Color(1f, 0.2f, 0.2f, 1f)
                                  : new Color(0.4f, 0.4f, 0.4f, 1f));
            sr.circle(cx - 100 + i * 50, cy, 18, 24);
        }
    }

    private void drawCentered(BitmapFont f, String text, float cx, float y) {
        layout.setText(f, text);
        f.draw(game.batch, text, cx - layout.width / 2f, y);
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        float tx = Gdx.input.getX();
        float ty = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (tx >= btnRetryX && tx <= btnRetryX + btnRetryW
         && ty >= btnRetryY && ty <= btnRetryY + btnRetryH) {
            if (GameSession.get().hasLives()) {
                GameSession.get().restartLevel();
                game.setScreen(new Match3Screen(game));
            }
        }

        if (tx >= btnMenuX && tx <= btnMenuX + btnMenuW
         && ty >= btnMenuY && ty <= btnMenuY + btnMenuH) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        fontBig.dispose();
        font.dispose();
    }
}
