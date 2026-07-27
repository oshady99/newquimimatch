package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.managers.MoleculeDatabase;
import com.quimimatch.managers.MoleculeDatabase.MoleculeEntry;

import java.util.List;

public class EncyclopediaScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer sr;
    private BitmapFont fontTitle;
    private BitmapFont fontBig;
    private BitmapFont font;
    private BitmapFont fontSmall;
    private GlyphLayout layout;

    private float SW, SH;

    // Molécula seleccionada actualmente
    private int selectedIdx = -1;
    private MoleculeEntry selectedEntry = null;

    // Scroll de la lista
    private float scrollY     = 0f;
    private float lastTouchY  = 0f;
    private boolean dragging  = false;
    private boolean didDrag   = false;
    private float   dragStart = 0f;

    // Animación Byte
    private float byteFloat = 0f;
    private float byteTimer = 0f;

    // Colores por tipo
    private static final java.util.Map<String, Color> TYPE_COLORS = new java.util.HashMap<>();
    static {
        TYPE_COLORS.put("Oxido",   new Color(0.3f,  0.7f,  1.0f,  1f));
        TYPE_COLORS.put("Sal",     new Color(1.0f,  0.85f, 0.2f,  1f));
        TYPE_COLORS.put("Base",    new Color(0.3f,  0.85f, 0.5f,  1f));
        TYPE_COLORS.put("Acido",   new Color(1.0f,  0.35f, 0.35f, 1f));
        TYPE_COLORS.put("Organica",new Color(0.85f, 0.5f,  0.2f,  1f));
    }

    // Layout
    private static final float CARD_H    = 110f;
    private static final float CARD_GAP  = 14f;
    private static final float LIST_W    = 0.38f; // fracción SW
    private static final float DETAIL_X  = 0.42f; // fracción SW

    // Botón volver
    private float btnBackX, btnBackY, btnBackW, btnBackH;

    public EncyclopediaScreen(QuimiMatchGame game) {
        this.game = game;
        SW = Gdx.graphics.getWidth();
        SH = Gdx.graphics.getHeight();

        sr        = new ShapeRenderer();
        fontTitle = new BitmapFont(); fontTitle.getData().setScale(2.8f);
        fontBig   = new BitmapFont(); fontBig.getData().setScale(2.2f);
        font      = new BitmapFont(); font.getData().setScale(1.7f);
        fontSmall = new BitmapFont(); fontSmall.getData().setScale(1.3f);
        layout    = new GlyphLayout();

        btnBackW = 180; btnBackH = 65;
        btnBackX = 20;  btnBackY = 20;

        // Seleccionar primera molécula descubierta automáticamente
        List<MoleculeEntry> all = MoleculeDatabase.getAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).discovered) {
                selectedIdx   = i;
                selectedEntry = all.get(i);
                break;
            }
        }
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        byteFloat += delta * 2.2f;
        byteTimer += delta;

        handleInput();
        drawHeader();
        drawMoleculeList();
        drawDetailPanel();
        drawBackButton();
    }

    // ── Input ────────────────────────────────────────────────────

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            float tx = Gdx.input.getX();
            float ty = SH - Gdx.input.getY();

            if (!dragging) {
                dragging  = true;
                didDrag   = false;
                dragStart = ty;
                lastTouchY = ty;
            } else {
                float dy = ty - lastTouchY;
                if (Math.abs(ty - dragStart) > 10) didDrag = true;
                if (didDrag && tx < SW * LIST_W + 10) {
                    scrollY += dy;
                    List<MoleculeEntry> all = MoleculeDatabase.getAll();
                    float maxScroll = Math.max(0, all.size() * (CARD_H + CARD_GAP) - (SH - 130));
                    scrollY = Math.max(-maxScroll, Math.min(0, scrollY));
                }
                lastTouchY = ty;
            }
        } else if (dragging) {
            dragging = false;
            if (!didDrag) {
                float tx = Gdx.input.getX();
                float ty = SH - Gdx.input.getY();
                checkTap(tx, ty);
            }
        }
    }

    private void checkTap(float tx, float ty) {
        // Botón volver
        if (tx >= btnBackX && tx <= btnBackX + btnBackW
         && ty >= btnBackY && ty <= btnBackY + btnBackH) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        // Tap en lista
        if (tx < SW * LIST_W) {
            List<MoleculeEntry> all = MoleculeDatabase.getAll();
            float startY = SH - 130 + scrollY;
            for (int i = 0; i < all.size(); i++) {
                float cardY = startY - i * (CARD_H + CARD_GAP) - CARD_H;
                if (ty >= cardY && ty <= cardY + CARD_H) {
                    if (all.get(i).discovered) {
                        selectedIdx   = i;
                        selectedEntry = all.get(i);
                    }
                    return;
                }
            }
        }
    }

    // ── Header ───────────────────────────────────────────────────

    private void drawHeader() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.08f, 0.22f, 1f);
        sr.rect(0, SH - 110, SW, 110);
        sr.setColor(0.25f, 0.55f, 1f, 1f);
        sr.rect(0, SH - 112, SW, 3);
        sr.end();

        game.batch.begin();
        fontTitle.setColor(new Color(1f, 0.9f, 0.3f, 1f));
        drawC(fontTitle, "ENCICLOPEDIA QUIMICA", SW / 2f, SH - 18);

        int disc  = MoleculeDatabase.discoveredCount();
        int total = MoleculeDatabase.getAll().size();
        fontSmall.setColor(new Color(0.6f, 0.7f, 1f, 1f));
        drawC(fontSmall, "Descubiertas: " + disc + " / " + total, SW / 2f, SH - 76);
        game.batch.end();
    }

    // ── Lista de moléculas ───────────────────────────────────────

    private void drawMoleculeList() {
        List<MoleculeEntry> all = MoleculeDatabase.getAll();
        float listW  = SW * LIST_W;
        float startY = SH - 130 + scrollY;

        // Fondo lista
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.09f, 0.09f, 0.20f, 1f);
        sr.rect(0, 0, listW, SH - 110);

        for (int i = 0; i < all.size(); i++) {
            MoleculeEntry e = all.get(i);
            float cardY = startY - i * (CARD_H + CARD_GAP) - CARD_H;

            // Clip aproximado
            if (cardY + CARD_H < 0 || cardY > SH - 110) continue;

            boolean isSelected = (i == selectedIdx);

            // Fondo tarjeta
            if (isSelected) {
                sr.setColor(0.2f, 0.35f, 0.65f, 1f);
            } else {
                sr.setColor(0.13f, 0.13f, 0.28f, 1f);
            }
            sr.rect(10, cardY, listW - 20, CARD_H);

            if (e.discovered) {
                // Acento de color tipo molécula
                Color tc = TYPE_COLORS.getOrDefault(e.type, Color.WHITE);
                sr.setColor(tc);
                sr.rect(10, cardY, 6, CARD_H);

                // Círculo fórmula
                sr.setColor(tc.r * 0.4f, tc.g * 0.4f, tc.b * 0.4f, 1f);
                sr.circle(55, cardY + CARD_H / 2f, 32, 24);
            } else {
                sr.setColor(0.3f, 0.3f, 0.3f, 1f);
                sr.rect(10, cardY, 6, CARD_H);
                sr.setColor(0.2f, 0.2f, 0.2f, 1f);
                sr.circle(55, cardY + CARD_H / 2f, 32, 24);
            }
        }
        sr.end();

        // Textos de la lista
        game.batch.begin();
        for (int i = 0; i < all.size(); i++) {
            MoleculeEntry e = all.get(i);
            float cardY = startY - i * (CARD_H + CARD_GAP) - CARD_H;
            if (cardY + CARD_H < 0 || cardY > SH - 110) continue;

            if (e.discovered) {
                Color tc = TYPE_COLORS.getOrDefault(e.type, Color.WHITE);
                fontSmall.setColor(tc);
                drawC(fontSmall, e.formula, 55, cardY + CARD_H / 2f + 8);
                font.setColor(Color.WHITE);
                font.draw(game.batch, e.name, 96, cardY + CARD_H / 2f + 14);
                fontSmall.setColor(new Color(0.6f, 0.6f, 0.8f, 1f));
                fontSmall.draw(game.batch, e.type, 96, cardY + CARD_H / 2f - 22);
            } else {
                font.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
                drawC(font, "?", 55, cardY + CARD_H / 2f + 10);
                font.setColor(new Color(0.35f, 0.35f, 0.35f, 1f));
                font.draw(game.batch, "Bloqueada", 96, cardY + CARD_H / 2f + 10);
            }
        }
        game.batch.end();
    }

    // ── Panel de detalle ─────────────────────────────────────────

    private void drawDetailPanel() {
        float detX = SW * DETAIL_X;
        float detW = SW - detX - 10;
        float detY = 100f;
        float detH = SH - 130 - detY;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.10f, 0.10f, 0.24f, 1f);
        sr.rect(detX, detY, detW, detH);
        sr.end();

        if (selectedEntry == null) {
            // Byte esperando selección
            float bx = detX + detW / 2f;
            float by = detY + detH / 2f - 100 + (float)Math.sin(byteFloat) * 8f;
            drawByte(bx, by);

            game.batch.begin();
            font.setColor(new Color(0.5f, 0.7f, 1f, 1f));
            drawC(font, "Selecciona una", bx, by - 20);
            drawC(font, "molecula para", bx, by - 55);
            drawC(font, "ver sus datos!", bx, by - 90);
            game.batch.end();
            return;
        }

        MoleculeEntry e = selectedEntry;
        Color tc = TYPE_COLORS.getOrDefault(e.type, Color.WHITE);
        float cx = detX + detW / 2f;
        float byteY = detY + detH - 220 + (float)Math.sin(byteFloat) * 6f;

        // Barra tipo
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(tc.r * 0.3f, tc.g * 0.3f, tc.b * 0.3f, 1f);
        sr.rect(detX, detY + detH - 70, detW, 70);
        sr.setColor(tc);
        sr.rect(detX, detY + detH - 70, 8, 70);

        // Byte pequeño a la derecha del panel
        sr.end();
        drawByteSmall(detX + detW - 80, byteY + 40);

        // Separadores
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(tc.r * 0.4f, tc.g * 0.4f, tc.b * 0.4f, 1f);
        float[] sepYs = { detY + detH - 200, detY + detH - 320, detY + detH - 440 };
        for (float sy : sepYs) sr.rect(detX + 20, sy, detW - 40, 2);
        sr.end();

        game.batch.begin();

        // Fórmula + nombre
        fontBig.setColor(tc);
        drawC(fontBig, e.formula, cx - 60, detY + detH - 12);
        fontSmall.setColor(new Color(0.7f, 0.8f, 1f, 1f));
        font.draw(game.batch, e.name, detX + 20, detY + detH - 12);

        // Tipo
        fontSmall.setColor(tc);
        fontSmall.draw(game.batch, e.emoji + " " + e.type, detX + 20, detY + detH - 52);

        // Dato curioso — Byte lo "dice"
        fontSmall.setColor(new Color(0.5f, 0.9f, 0.7f, 1f));
        fontSmall.draw(game.batch, "Byte dice:", detX + 20, detY + detH - 95);
        font.setColor(Color.WHITE);
        drawWrapped(font, e.fact, detX + 20, detY + detH - 130, detW - 40);

        // Propiedades
        fontSmall.setColor(new Color(1f, 0.85f, 0.3f, 1f));
        fontSmall.draw(game.batch, "PROPIEDADES", detX + 20, detY + detH - 215);
        font.setColor(new Color(0.85f, 0.85f, 1f, 1f));
        font.draw(game.batch, "Fisica:  " + e.property1, detX + 20, detY + detH - 252);
        font.draw(game.batch, "Quimica: " + e.property2, detX + 20, detY + detH - 290);

        // Usos
        fontSmall.setColor(new Color(1f, 0.85f, 0.3f, 1f));
        fontSmall.draw(game.batch, "USOS", detX + 20, detY + detH - 335);
        font.setColor(new Color(0.85f, 1f, 0.85f, 1f));
        drawWrapped(font, e.uses, detX + 20, detY + detH - 372, detW - 40);

        game.batch.end();
    }

    // ── Byte completo ────────────────────────────────────────────

    private void drawByte(float cx, float baseY) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Cuerpo
        sr.setColor(new Color(0.25f, 0.65f, 1f, 1f));
        sr.rect(cx - 50, baseY, 100, 80);
        // Cabeza
        sr.setColor(new Color(0.18f, 0.50f, 0.85f, 1f));
        sr.rect(cx - 38, baseY + 80, 76, 65);
        // Ojos
        sr.setColor(new Color(0.1f, 1f, 0.6f, 1f));
        float blink = (float)Math.sin(byteTimer * 0.8f);
        float eyeH  = blink > 0.95f ? 2f : 13f;
        sr.rect(cx - 26, baseY + 118, 20, eyeH);
        sr.rect(cx + 6,  baseY + 118, 20, eyeH);
        // Boca LEDs
        for (int i = 0; i < 5; i++) sr.rect(cx - 24 + i * 11, baseY + 90, 7, 7);
        // Antena
        sr.setColor(new Color(0.25f, 0.65f, 1f, 1f));
        sr.rect(cx - 4, baseY + 145, 8, 28);
        sr.setColor(new Color(0.1f, 1f, 0.6f, 1f));
        sr.circle(cx, baseY + 178, 9, 16);
        // Brazos y piernas
        sr.setColor(new Color(0.2f, 0.55f, 0.88f, 1f));
        sr.rect(cx - 68, baseY + 44, 20, 11);
        sr.rect(cx + 48, baseY + 44, 20, 11);
        sr.rect(cx - 30, baseY - 32, 20, 34);
        sr.rect(cx + 10, baseY - 32, 20, 34);
        sr.end();
    }

    // Byte pequeño para el panel detalle
    private void drawByteSmall(float cx, float baseY) {
        float s = 0.55f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.25f, 0.65f, 1f, 1f));
        sr.rect(cx - 50*s, baseY, 100*s, 80*s);
        sr.setColor(new Color(0.18f, 0.50f, 0.85f, 1f));
        sr.rect(cx - 38*s, baseY + 80*s, 76*s, 65*s);
        sr.setColor(new Color(0.1f, 1f, 0.6f, 1f));
        sr.rect(cx - 26*s, baseY + 118*s, 20*s, 13*s);
        sr.rect(cx + 6*s,  baseY + 118*s, 20*s, 13*s);
        sr.rect(cx - 4*s, baseY + 145*s, 8*s, 28*s);
        sr.circle(cx, baseY + 178*s, 9*s, 12);
        sr.end();
    }

    // ── Botón volver ─────────────────────────────────────────────

    private void drawBackButton() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.2f, 0.45f, 1f);
        sr.rect(btnBackX, btnBackY, btnBackW, btnBackH);
        sr.end();

        game.batch.begin();
        font.setColor(Color.WHITE);
        drawC(font, "< VOLVER", btnBackX + btnBackW / 2f, btnBackY + 42);
        game.batch.end();
    }

    // ── Utilidades ───────────────────────────────────────────────

    private void drawWrapped(BitmapFont f, String text, float x, float y, float maxW) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            layout.setText(f, test);
            if (layout.width > maxW && line.length() > 0) {
                f.draw(game.batch, line.toString(), x, lineY);
                lineY -= f.getLineHeight() + 4;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) f.draw(game.batch, line.toString(), x, lineY);
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
        fontBig.dispose();
        font.dispose();
        fontSmall.dispose();
    }
}
