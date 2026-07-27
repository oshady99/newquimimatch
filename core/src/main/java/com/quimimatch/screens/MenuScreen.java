package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.managers.PlayerInventory;
import com.quimimatch.managers.AudioManager;
import com.quimimatch.managers.SaveManager;
import com.quimimatch.managers.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MenuScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer sr;
    private BitmapFont fontTitle;
    private BitmapFont fontSub;
    private BitmapFont fontBtn;
    private BitmapFont fontCoin;
    private GlyphLayout layout;

    private float SW, SH;

    public MenuScreen(QuimiMatchGame game) {
        this.game = game;
        SW = Gdx.graphics.getWidth();
        SH = Gdx.graphics.getHeight();

        sr       = new ShapeRenderer();
        fontTitle = new BitmapFont(); fontTitle.getData().setScale(4.2f);
        fontSub   = new BitmapFont(); fontSub.getData().setScale(2.2f);
        fontBtn   = new BitmapFont(); fontBtn.getData().setScale(2.6f);
        fontCoin  = new BitmapFont(); fontCoin.getData().setScale(1.8f);
        layout    = new GlyphLayout();
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        SW = Gdx.graphics.getWidth();
        SH = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.06f, 0.06f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        AssetLoader assets = AssetLoader.get();

        // ── Fondo ────────────────────────────────────────────────
        if (assets.hasLabBackground(1)) {
            game.batch.begin();
            game.batch.setColor(0.5f, 0.5f, 0.7f, 1f);
            game.batch.draw(assets.getLabBackground(1), 0, 0, SW, SH);
            game.batch.setColor(Color.WHITE);
            game.batch.end();
        }

        float cx = SW / 2f;

        // ── Medidas proporcionales ───────────────────────────────
        float logoH    = SH * 0.22f;
        float logoY    = SH - logoH - SH * 0.04f;

        float btnJugarH = SH * 0.14f;
        float btnJugarW = SW * 0.70f;
        float btnJugarY = logoY - btnJugarH - SH * 0.04f;
        float btnJugarX = cx - btnJugarW / 2f;

        float btn2H = SH * 0.11f;
        float btn2W = SW * 0.44f;
        float btn2Y = btnJugarY - btn2H - SH * 0.03f;
        float btnEncX = cx - btn2W - SW * 0.02f;
        float btnShopX = cx + SW * 0.02f;

        // ── ShapeRenderer ────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Botón JUGAR
        sr.setColor(0.12f, 0.68f, 0.34f, 1f);
        sr.rect(btnJugarX, btnJugarY, btnJugarW, btnJugarH);
        sr.setColor(0.18f, 0.85f, 0.42f, 0.25f);
        sr.rect(btnJugarX, btnJugarY + btnJugarH * 0.6f, btnJugarW, btnJugarH * 0.4f);

        // Botón ENCICLOPEDIA
        sr.setColor(0.18f, 0.35f, 0.72f, 1f);
        sr.rect(btnEncX, btn2Y, btn2W, btn2H);
        sr.setColor(0.28f, 0.50f, 0.90f, 0.3f);
        sr.rect(btnEncX, btn2Y + btn2H * 0.6f, btn2W, btn2H * 0.4f);

        // Botón TIENDA
        sr.setColor(0.70f, 0.48f, 0.05f, 1f);
        sr.rect(btnShopX, btn2Y, btn2W, btn2H);
        sr.setColor(0.90f, 0.65f, 0.10f, 0.3f);
        sr.rect(btnShopX, btn2Y + btn2H * 0.6f, btn2W, btn2H * 0.4f);

        // Monedas — esquina superior derecha
        sr.setColor(new Color(1f, 0.85f, 0.1f, 1f));
        sr.circle(SW - 180, SH - 42, 20, 24);

        // Botones audio (esquina superior izquierda)
        sr.setColor(AudioManager.get().isMusicEnabled() ? new Color(0.3f,0.7f,1f,1f) : new Color(0.3f,0.3f,0.3f,1f));
        sr.circle(40, SH - 40, 24, 20);
        sr.setColor(AudioManager.get().isSoundEnabled() ? new Color(0.3f,0.9f,0.5f,1f) : new Color(0.3f,0.3f,0.3f,1f));
        sr.circle(90, SH - 40, 24, 20);

        sr.end();

        // ── Textos ───────────────────────────────────────────────
        game.batch.begin();

        // Logo real si existe
        if (assets.logo != null) {
            float lw = assets.logo.getWidth();
            float lh = assets.logo.getHeight();
            float targetH = logoH * 1.1f;
            float targetW = lw * (targetH / lh);
            game.batch.draw(assets.logo, cx - targetW / 2f, logoY, targetW, targetH);
        } else {
            // Título QUIMIMATCH (Fallback)
            fontTitle.setColor(new Color(1f, 0.92f, 0.22f, 1f));
            drawC(fontTitle, "QUIMIMATCH", cx, logoY + logoH * 0.72f);

            // Subtítulo Academia Molecular — dentro del recuadro
            fontSub.setColor(new Color(0.72f, 0.84f, 1f, 1f));
            drawC(fontSub, "Academia Molecular", cx, logoY + logoH * 0.28f);
        }

        // JUGAR
        fontBtn.setColor(Color.WHITE);
        drawC(fontBtn, "JUGAR", cx, btnJugarY + btnJugarH * 0.62f);

        // ENCICLOPEDIA
        fontBtn.setColor(new Color(0.88f, 0.94f, 1f, 1f));
        drawC(fontBtn, "ENCICLOPEDIA", btnEncX + btn2W / 2f, btn2Y + btn2H * 0.62f);

        // TIENDA
        fontBtn.setColor(new Color(1f, 0.96f, 0.72f, 1f));
        drawC(fontBtn, "TIENDA", btnShopX + btn2W / 2f, btn2Y + btn2H * 0.62f);

        // Monedas
        fontCoin.setColor(new Color(1f, 0.9f, 0.2f, 1f));
        fontCoin.draw(game.batch, "x" + PlayerInventory.get().getCoins(), SW - 152, SH - 26);

        // Labels audio
        fontCoin.setColor(Color.WHITE);
        drawC(fontCoin, "M", 40, SH - 26);
        drawC(fontCoin, "S", 90, SH - 26);

        game.batch.end();

        handleInput(btnJugarX, btnJugarY, btnJugarW, btnJugarH,
                    btnEncX,   btn2Y,     btn2W,     btn2H,
                    btnShopX);
    }

    private void handleInput(float jx, float jy, float jw, float jh,
                              float ex, float ey, float ew, float eh,
                              float sx) {
        if (!Gdx.input.justTouched()) return;
        float tx = Gdx.input.getX();
        float ty = SH - Gdx.input.getY();

        // Audio toggles
        if (ty > SH - 70) {
            if (tx < 65)  { AudioManager.get().setMusicEnabled(!AudioManager.get().isMusicEnabled()); SaveManager.saveAudioSettings(); }
            if (tx < 115 && tx > 65) { AudioManager.get().setSoundEnabled(!AudioManager.get().isSoundEnabled()); SaveManager.saveAudioSettings(); }
        }

        if (tx >= jx && tx <= jx + jw && ty >= jy && ty <= jy + jh) {
            AudioManager.get().playMenuClick();
            game.setScreen(new WorldMapScreen(game));
        } else if (tx >= ex && tx <= ex + ew && ty >= ey && ty <= ey + eh) {
            AudioManager.get().playMenuClick();
            game.setScreen(new EncyclopediaScreen(game));
        } else if (tx >= sx && tx <= sx + ew && ty >= ey && ty <= ey + eh) {
            AudioManager.get().playMenuClick();
            game.setScreen(new ShopScreen(game));
        }
    }

    private void drawC(BitmapFont f, String text, float cx, float y) {
        layout.setText(f, text);
        f.draw(game.batch, text, cx - layout.width / 2f, y);
    }

    @Override public void resize(int w, int h) { SW = w; SH = h; }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        sr.dispose();
        fontTitle.dispose();
        fontSub.dispose();
        fontBtn.dispose();
        fontCoin.dispose();
    }
}
