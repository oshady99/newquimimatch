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
import com.quimimatch.managers.SaveManager;

public class VictoryScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontBig;
    private BitmapFont font;
    private GlyphLayout layout;

    private int stars;
    private float animTimer = 0f;

    // Botones
    private float btnNextX, btnNextY, btnNextW, btnNextH;
    private float btnMenuX, btnMenuY, btnMenuW, btnMenuH;

    public VictoryScreen(QuimiMatchGame game) {
        this.game     = game;
        shapeRenderer = new ShapeRenderer();
        fontBig       = new BitmapFont();
        fontBig.getData().setScale(3.8f);
        font          = new BitmapFont();
        font.getData().setScale(2.0f);
        layout        = new GlyphLayout();

        stars = GameSession.get().calculateStars();
        GameSession.get().saveStars();
        // Recompensar monedas
        com.quimimatch.managers.PlayerInventory.get().rewardLevel(stars);
        SaveManager.saveAll(); // Guardar progreso
        // Descubrir molécula
        com.quimimatch.managers.MoleculeDatabase.discover(
                GameSession.get().getConfig().moleculeName);

        float cx = Gdx.graphics.getWidth()  / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;

        btnNextW = 280; btnNextH = 70;
        btnNextX = cx - btnNextW / 2f;
        btnNextY = cy - 160;

        btnMenuW = 200; btnMenuH = 55;
        btnMenuX = cx - btnMenuW / 2f;
        btnMenuY = cy - 250;
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        animTimer += delta;

        Gdx.gl.glClearColor(0.05f, 0.1f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float cx = sw / 2f;
        float cy = sh / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Panel central
        shapeRenderer.setColor(0.1f, 0.2f, 0.1f, 1f);
        shapeRenderer.rect(cx - 320, cy - 280, 640, 500);

        // Estrellas (círculos de color)
        drawStars(shapeRenderer, cx, cy + 120, stars);

        // Botón SIGUIENTE NIVEL
        shapeRenderer.setColor(0.2f, 0.75f, 0.3f, 1f);
        shapeRenderer.rect(btnNextX, btnNextY, btnNextW, btnNextH);

        // Botón MENÚ
        shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
        shapeRenderer.rect(btnMenuX, btnMenuY, btnMenuW, btnMenuH);

        shapeRenderer.end();

        // Textos
        game.batch.begin();

        fontBig.setColor(new Color(1f, 0.9f, 0.2f, 1f));
        drawCentered(fontBig, "¡VICTORIA!", cx, cy + 230);

        font.setColor(Color.WHITE);
        drawCentered(font, "Molécula: " + GameSession.get().getConfig().moleculeName,
                cx, cy + 60);
        drawCentered(font, "Puntos: " + GameSession.get().getScore(), cx, cy + 20);

        font.setColor(Color.WHITE);
        drawCentered(font, "SIGUIENTE NIVEL", cx, btnNextY + 44);

        font.setColor(new Color(0.8f, 0.8f, 1f, 1f));
        drawCentered(font, "MENÚ", cx, btnMenuY + 36);

        game.batch.end();

        handleInput();
    }

    private void drawStars(ShapeRenderer sr, float cx, float cy, int count) {
        float[] offsets = { -90, 0, 90 };
        for (int i = 0; i < 3; i++) {
            if (i < count) {
                sr.setColor(1f, 0.85f, 0f, 1f);  // dorada
            } else {
                sr.setColor(0.3f, 0.3f, 0.3f, 1f); // gris
            }
            sr.circle(cx + offsets[i], cy, 30, 36);
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

        // Siguiente nivel
        if (tx >= btnNextX && tx <= btnNextX + btnNextW
         && ty >= btnNextY && ty <= btnNextY + btnNextH) {
            game.setScreen(new WorldMapScreen(game));
        }

        // Menú
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
