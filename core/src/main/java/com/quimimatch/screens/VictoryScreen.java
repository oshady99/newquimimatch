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
    private boolean finale;
    private final float[][] confetti = new float[80][3];
    private float animTimer = 0f;

    // Botones
    private float btnNextX, btnNextY, btnNextW, btnNextH;
    private float btnMenuX, btnMenuY, btnMenuW, btnMenuH;

    public VictoryScreen(QuimiMatchGame game) {
        this.game     = game;
        shapeRenderer = new ShapeRenderer();
        fontBig       = new BitmapFont();
        fontBig.getData().setScale(3.1f);
        font          = new BitmapFont();
        font.getData().setScale(1.7f);
        layout        = new GlyphLayout();

        stars = GameSession.get().calculateStars();
        finale = GameSession.get().getCurrentWorld() == 5 && GameSession.get().getCurrentLevel() == 3;
        for (float[] p : confetti) {
            p[0] = com.badlogic.gdx.math.MathUtils.random();
            p[1] = com.badlogic.gdx.math.MathUtils.random();
            p[2] = com.badlogic.gdx.math.MathUtils.random(0.1f, 0.3f);
        }
        GameSession.get().saveStars();
        // Recompensar monedas
        com.quimimatch.managers.PlayerInventory.get().rewardLevel(stars);

        // Descubrir molécula
        com.quimimatch.managers.MoleculeDatabase.discover(
                GameSession.get().getConfig().moleculeName);
        SaveManager.saveAll();

        float cx = 450f;
        float cy = 300f;

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

        Gdx.gl.glClearColor(0.025f, 0.045f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float sw = 900f;
        float sh = 600f;
        float cx = sw / 2f;
        float cy = sh / 2f;

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Panel central
        shapeRenderer.setColor(0.07f, 0.14f, 0.24f, 1f);
        shapeRenderer.rect(cx - 320, cy - 280, 640, 500);

        // Estrellas (círculos de color)
        drawStars(shapeRenderer, cx, cy + 120, stars);

        // Botón SIGUIENTE NIVEL
        shapeRenderer.setColor(0.2f, 0.75f, 0.3f, 1f);
        shapeRenderer.rect(btnNextX, btnNextY, btnNextW, btnNextH);

        // Botón MENÚ
        shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
        shapeRenderer.rect(btnMenuX, btnMenuY, btnMenuW, btnMenuH);

        for (int i = 0; i < confetti.length; i++) {
            float[] p = confetti[i];
            p[1] -= Math.min(delta, 0.05f) * p[2];
            if (p[1] < 0) p[1] += 1f;
            shapeRenderer.setColor(i % 2 == 0 ? Color.GOLD : Color.CYAN);
            shapeRenderer.rect(p[0] * sw, p[1] * sh, 4, 8);
        }
        shapeRenderer.end();

        // Textos
        game.batch.begin();

        fontBig.setColor(new Color(1f, 0.9f, 0.2f, 1f));
        drawCentered(fontBig, finale ? "¡GRAN FINAL!" : "¡SINTESIS COMPLETA!", cx, cy + 230);

        font.setColor(Color.WHITE);
        drawCentered(font, "Molécula: " + GameSession.get().getConfig().moleculeName,
                cx, cy + 60);
        drawCentered(font, "Puntos: " + Math.round(GameSession.get().getScore() * Math.min(1f, animTimer / 1.2f)), cx, cy + 20);

        fontSmallFinal(cx, cy - 35);
        font.setColor(Color.WHITE);
        drawCentered(font, "MAPA DE MUNDOS", cx, btnNextY + 44);

        font.setColor(new Color(0.8f, 0.8f, 1f, 1f));
        drawCentered(font, "MENÚ", cx, btnMenuY + 36);

        game.batch.end();

        handleInput();
    }

    private void fontSmallFinal(float cx, float y) {
        font.setColor(Color.CYAN);
        drawCentered(font, finale ? "6 mundos · 24 niveles · ¡Expedicion completada!"
            : "Reaccion estable · Molecula descubierta", cx, y);
    }

    private void drawStars(ShapeRenderer sr, float cx, float cy, int count) {
        float[] offsets = { -90, 0, 90 };
        for (int i = 0; i < 3; i++) {
            if (i < count) {
                sr.setColor(1f, 0.85f, 0f, 1f);  // dorada
            } else {
                sr.setColor(0.3f, 0.3f, 0.3f, 1f); // gris
            }
            float reveal = Math.min(1f, Math.max(0f, (animTimer - i * 0.22f) / 0.3f));
            float x = cx + offsets[i];
            for (int tip = 0; tip < 10; tip++) {
                double a = Math.PI / 2 + tip * Math.PI / 5;
                double b = a + Math.PI / 5;
                float ra = (tip % 2 == 0 ? 34 : 15) * reveal;
                float rb = (tip % 2 == 0 ? 15 : 34) * reveal;
                sr.triangle(x, cy, x + (float)Math.cos(a) * ra, cy + (float)Math.sin(a) * ra,
                    x + (float)Math.cos(b) * rb, cy + (float)Math.sin(b) * rb);
            }
        }
    }

    private void drawCentered(BitmapFont f, String text, float cx, float y) {
        layout.setText(f, text);
        f.draw(game.batch, text, cx - layout.width / 2f, y);
    }

    private void handleInput() {
        if (animTimer < 0.6f || !Gdx.input.justTouched()) return;
        float tx = Gdx.input.getX() * 900f / Gdx.graphics.getWidth();
        float ty = 600f - Gdx.input.getY() * 600f / Gdx.graphics.getHeight();

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
    @Override public void hide() {
        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        dispose();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        fontBig.dispose();
        font.dispose();
    }
}
