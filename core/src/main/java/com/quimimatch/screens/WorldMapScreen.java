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

public class WorldMapScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer sr;
    private BitmapFont fontTitle;
    private BitmapFont fontBig;
    private BitmapFont font;
    private BitmapFont fontSmall;
    private GlyphLayout layout;

    // Scroll
    private float scrollY     = 0f;
    private float lastTouchY  = 0f;
    private float dragStart   = 0f;
    private boolean dragging  = false;
    private boolean didDrag   = false;

    private static final int WORLDS           = 6;
    private static final int LEVELS_PER_WORLD = 4;

    // Tamaño del círculo de nivel
    private static final float NODE_R     = 70f;   // radio del nodo
    private static final float WORLD_H    = 420f;  // altura por mundo

    // Colores por mundo
    private static final Color[] WORLD_COLORS = {
        new Color(0.25f, 0.55f, 1.0f,  1f),
        new Color(0.15f, 0.80f, 0.45f, 1f),
        new Color(0.95f, 0.50f, 0.10f, 1f),
        new Color(0.85f, 0.20f, 0.55f, 1f),
        new Color(0.55f, 0.20f, 0.95f, 1f),
        new Color(0.95f, 0.80f, 0.10f, 1f),
    };

    private static final String[] WORLD_NAMES = {
        "Moleculas Basicas",
        "Sales",
        "Acidos",
        "Metales",
        "Organica",
        "Avanzada",
    };

    public WorldMapScreen(QuimiMatchGame game) {
        this.game  = game;
        sr         = new ShapeRenderer();
        fontTitle  = new BitmapFont(); fontTitle.getData().setScale(3.0f);
        fontBig    = new BitmapFont(); fontBig.getData().setScale(2.6f);
        font       = new BitmapFont(); font.getData().setScale(2.2f);
        fontSmall  = new BitmapFont(); fontSmall.getData().setScale(1.6f);
        layout     = new GlyphLayout();
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        drawMap();
    }

    // ── Input ────────────────────────────────────────────────────

    private void handleInput() {
        float sh = Gdx.graphics.getHeight();

        if (Gdx.input.isTouched()) {
            float ty = sh - Gdx.input.getY();
            if (!dragging) {
                dragging  = true;
                didDrag   = false;
                dragStart = ty;
                lastTouchY = ty;
            } else {
                float dy = ty - lastTouchY;
                if (Math.abs(ty - dragStart) > 12) didDrag = true;
                if (didDrag) {
                    scrollY   += dy;
                    float maxScroll = Math.max(0, WORLDS * WORLD_H - sh + 300);
                    scrollY = Math.max(0, Math.min(maxScroll, scrollY));
                }
                lastTouchY = ty;
            }
        } else if (dragging) {
            dragging = false;
            if (!didDrag) {
                // Fue un toque real, no scroll
                float tx = Gdx.input.getX();
                float ty = sh - Gdx.input.getY();
                checkTap(tx, ty);
            }
        }
    }

    private void checkTap(float tx, float ty) {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        for (int w = 0; w < WORLDS; w++) {
            for (int l = 0; l < LEVELS_PER_WORLD; l++) {
                float[] pos = nodePos(w, l, sw, sh);
                float cx = pos[0], cy = pos[1];
                float dist = (float) Math.sqrt((tx - cx) * (tx - cx) + (ty - cy) * (ty - cy));
                if (dist <= NODE_R + 10) {
                    if (isUnlocked(w, l)) {
                        GameSession.get().startLevel(w, l);
                        game.setScreen(new RunnerScreen(game));
                    }
                    return;
                }
            }
        }
    }

    // ── Posición de cada nodo (zigzag) ───────────────────────────

    /**
     * Devuelve {cx, cy} del nodo [world][level].
     * Zigzag: niveles alternados arriba/abajo dentro del bloque del mundo.
     */
    private float[] nodePos(int w, int l, float sw, float sh) {
        float worldTopY = sh - 120 + scrollY - w * WORLD_H;

        // 4 posiciones en zigzag
        float[] xPositions = {
            sw * 0.18f,
            sw * 0.42f,
            sw * 0.66f,
            sw * 0.88f,
        };
        float[] yOffsets = { -80f, -180f, -80f, -180f };

        float cx = xPositions[l];
        float cy = worldTopY + yOffsets[l];
        return new float[]{ cx, cy };
    }

    // ── Dibujo ──────────────────────────────────────────────────

    private void drawMap() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // ── Mundos disponibles ───────────────────────────────────
        for (int w = 0; w < WORLDS; w++) {
            drawWorld(w, sw, sh);
        }


        // ── Header encima de todo ────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.08f, 0.20f, 1f);
        sr.rect(0, sh - 110, sw, 110);
        sr.end();

        game.batch.begin();
        fontTitle.setColor(new Color(1f, 0.9f, 0.3f, 1f));
        drawCentered(fontTitle, "QUIMIMATCH", sw / 2f, sh - 18);
        fontSmall.setColor(new Color(0.6f, 0.7f, 1f, 1f));
        drawCentered(fontSmall, "Elige tu nivel", sw / 2f, sh - 76);
        game.batch.end();

        // ── Vidas ────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        int lives = GameSession.get().getLives();
        for (int i = 0; i < 5; i++) {
            sr.setColor(i < lives
                    ? new Color(1f, 0.2f, 0.3f, 1f)
                    : new Color(0.4f, 0.4f, 0.4f, 1f));
            sr.circle(sw - 220 + i * 40, sh - 55, 15, 24);
        }
        sr.end();
    }

    private void drawWorld(int w, float sw, float sh) {
        Color wColor = WORLD_COLORS[w];
        float worldTopY = sh - 120 + scrollY - w * WORLD_H;

        // Banner del mundo
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(wColor.r * 0.25f, wColor.g * 0.25f, wColor.b * 0.25f, 1f);
        sr.rect(0, worldTopY - 50, sw, 50);
        sr.setColor(wColor);
        sr.rect(0, worldTopY - 50, 10, 50);
        sr.end();

        game.batch.begin();
        fontBig.setColor(wColor);
        font.draw(game.batch,
                "MUNDO " + (w + 1) + " — " + WORLD_NAMES[w],
                20, worldTopY - 8);
        game.batch.end();

        // Línea conectora entre nodos
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(wColor.r * 0.4f, wColor.g * 0.4f, wColor.b * 0.4f, 1f);
        for (int l = 0; l < LEVELS_PER_WORLD - 1; l++) {
            float[] a = nodePos(w, l,     sw, sh);
            float[] b = nodePos(w, l + 1, sw, sh);
            sr.rectLine(a[0], a[1], b[0], b[1], 6f);
        }
        sr.end();

        // Nodos de nivel
        for (int l = 0; l < LEVELS_PER_WORLD; l++) {
            drawNode(w, l, sw, sh, wColor);
        }
    }

    private void drawNode(int w, int l, float sw, float sh, Color wColor) {
        float[] pos = nodePos(w, l, sw, sh);
        float cx = pos[0], cy = pos[1];

        boolean unlocked  = isUnlocked(w, l);
        boolean completed = isCompleted(w, l);
        int     stars     = GameSession.get().getStars(w, l);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Sombra del nodo
        sr.setColor(0f, 0f, 0f, 0.4f);
        sr.circle(cx + 4, cy - 4, NODE_R, 40);

        // Fondo del nodo
        if (completed) {
            sr.setColor(wColor);
        } else if (unlocked) {
            sr.setColor(wColor.r * 0.5f, wColor.g * 0.5f, wColor.b * 0.5f, 1f);
        } else {
            sr.setColor(0.22f, 0.22f, 0.22f, 1f);
        }
        sr.circle(cx, cy, NODE_R, 40);

        // Borde blanco
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(unlocked ? 4f : 2f);
        sr.setColor(unlocked ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        sr.circle(cx, cy, NODE_R, 40);
        sr.end();
        Gdx.gl.glLineWidth(1f);

        // Estrellitas sobre el nodo completado
        if (completed) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < 3; i++) {
                sr.setColor(i < stars
                        ? new Color(1f, 0.9f, 0f, 1f)
                        : new Color(0.3f, 0.3f, 0.3f, 1f));
                sr.circle(cx - 28 + i * 28, cy - NODE_R - 20, 12, 20);
            }
            sr.end();
        }

        // Número o candado
        game.batch.begin();
        if (unlocked) {
            fontBig.setColor(Color.WHITE);
            drawCentered(fontBig, String.valueOf(l + 1), cx, cy + 18);
        } else {
            font.setColor(new Color(0.55f, 0.55f, 0.55f, 1f));
            drawCentered(font, "X", cx, cy + 14);
        }
        game.batch.end();
    }

    private void drawLockedWorld(int w, float sw, float sh) {
        float worldTopY = sh - 120 + scrollY - w * WORLD_H;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.13f, 0.13f, 0.13f, 1f);
        sr.rect(0, worldTopY - 50, sw, 50);
        sr.end();

        game.batch.begin();
        font.setColor(new Color(0.45f, 0.45f, 0.45f, 1f));
        font.draw(game.batch,
                "MUNDO " + (w + 1) + " — " + WORLD_NAMES[w] + "  [BLOQUEADO]",
                20, worldTopY - 8);
        game.batch.end();
    }

    // ── Desbloqueo ───────────────────────────────────────────────

    private boolean isUnlocked(int w, int l) {
        if (w == 0 && l == 0) return true;
        if (l > 0) return isCompleted(w, l - 1);
        return isCompleted(w - 1, LEVELS_PER_WORLD - 1);
    }

    private boolean isCompleted(int w, int l) {
        return GameSession.get().getStars(w, l) > 0;
    }

    // ── Util ─────────────────────────────────────────────────────

    private void drawCentered(BitmapFont f, String text, float cx, float y) {
        layout.setText(f, text);
        f.draw(game.batch, text, cx - layout.width / 2f, y);
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        sr.dispose();
        fontTitle.dispose();
        fontBig.dispose();
        font.dispose();
        fontSmall.dispose();
    }
}
