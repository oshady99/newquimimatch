package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.quimimatch.managers.AssetLoader;
import com.quimimatch.managers.MoleculeInfo;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Panel lateral con info de molécula — incluye Byte animado y letras grandes.
 */
public class MoleculePanel {

    private MoleculeInfo current  = null;
    private float showTimer       = 0f;
    private float slideX          = 0f;
    private float targetX         = 0f;
    private boolean visible       = false;
    private float byteAnim        = 0f;

    private static final float SHOW_DURATION = 6f;
    private static final float SLIDE_SPEED   = 10f;

    // Layout
    private float panelX, panelY, panelW, panelH;

    // Fuentes grandes
    private BitmapFont fontFormula;
    private BitmapFont fontName;
    private BitmapFont fontBody;
    private GlyphLayout layout;

    private static final Color BG_COLOR     = new Color(0.07f, 0.15f, 0.28f, 0.97f);
    private static final Color BORDER_COLOR = new Color(0.3f,  0.75f, 1.0f,  1f);
    private static final Color TITLE_COLOR  = new Color(1f,    0.92f, 0.3f,  1f);
    private static final Color BYTE_BLUE    = new Color(0.25f, 0.65f, 1.0f,  1f);
    private static final Color BYTE_GREEN   = new Color(0.1f,  1.0f,  0.6f,  1f);

    public MoleculePanel() {
        fontFormula = new BitmapFont(); fontFormula.getData().setScale(4.2f); // Aumentado
        fontName    = new BitmapFont(); fontName.getData().setScale(3.8f); // Aumentado
        fontBody    = new BitmapFont(); fontBody.getData().setScale(3.0f); // Aumentado
        layout      = new GlyphLayout();
        recalc();
    }

    private void recalc() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        panelW  = Math.min(sw * 0.75f, 750); // Aumentado
        panelH  = 750f; // Aumentado
        panelY  = sh / 2f - panelH / 2f;
        targetX = sw - panelW - 20;
        slideX  = sw;
        panelX  = slideX;
    }

    public void show(String formula) {
        MoleculeInfo info = MoleculeInfo.get(formula);
        if (info == null) return;
        current   = info;
        showTimer = SHOW_DURATION;
        visible   = true;
        slideX    = Gdx.graphics.getWidth();
        targetX   = Gdx.graphics.getWidth() - panelW - 10;
    }

    public void update(float delta) {
        if (!visible) return;
        byteAnim += delta * 3f;

        // Slide in
        slideX += (targetX - slideX) * SLIDE_SPEED * delta;
        panelX  = slideX;

        showTimer -= delta;
        if (showTimer <= 0) {
            // Slide out
            targetX = Gdx.graphics.getWidth();
            if (slideX >= Gdx.graphics.getWidth() - 5) {
                visible   = false;
                current   = null;
                targetX   = Gdx.graphics.getWidth() - panelW - 10;
                slideX    = Gdx.graphics.getWidth();
            }
        }
    }

    public void draw(ShapeRenderer sr, SpriteBatch batch) {
        if (!visible || current == null) return;

        float x = panelX, y = panelY, w = panelW, h = panelH;

        // ── Fondo ────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(BG_COLOR);
        sr.rect(x, y, w, h);

        // Acento izquierdo
        sr.setColor(BORDER_COLOR);
        sr.rect(x, y, 5, h);

        // Barra de tiempo
        float prog = Math.max(showTimer / SHOW_DURATION, 0f);
        sr.setColor(new Color(0.25f, 0.75f, 1f, 0.5f));
        sr.rect(x + 5, y, (w - 5) * prog, 5);

        // ── Byte pequeño ─────────────────────────────────────────
        float byteX = x + w - 100; // Ajustado posición
        float byteY = y + h - 180 + (float)Math.sin(byteAnim) * 5f;

        AssetLoader assets = AssetLoader.get();
        if (assets.byteIdle != null) {
            sr.end();
            batch.begin();
            TextureRegion frame = assets.byteIdle.getKeyFrame(byteAnim, true);
            batch.draw(frame, byteX - 90, byteY, 180, 180); // Aumentado 3 veces (era 60)
            batch.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);
        } else {
            drawByte(sr, byteX, byteY, 1.35f); // Aumentado 3 veces (era 0.45)
        }

        sr.end();

        // ── Borde ────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(BORDER_COLOR);
        sr.rect(x, y, w, h);
        sr.end();

        // ── Textos ───────────────────────────────────────────────
        batch.begin();

        float tx = x + 14;
        float ty = y + h - 16;

        // Fórmula
        fontFormula.setColor(new Color(0.5f, 0.9f, 1f, 1f));
        fontFormula.draw(batch, current.formula, tx, ty);

        // Nombre
        ty -= 90; // Aumentado espacio (era 70)
        fontName.setColor(TITLE_COLOR);
        fontName.draw(batch, current.name, tx, ty);

        // Separador
        ty -= 60; // Aumentado espacio (era 30)

        // Byte dice
        fontBody.setColor(BYTE_GREEN);
        fontBody.draw(batch, "Byte dice:", tx, ty);
        ty -= 70; // Aumentado espacio (era 50)

        // Dato curioso (con wrap)
        fontBody.setColor(Color.WHITE);
        ty = drawWrapped(batch, current.fact, tx, ty, w - 100);

        batch.end();
    }

    private void drawByte(ShapeRenderer sr, float cx, float baseY, float s) {
        // Cuerpo
        sr.setColor(BYTE_BLUE);
        sr.rect(cx - 38*s, baseY, 76*s, 62*s);
        // Cabeza
        sr.setColor(new Color(0.18f, 0.50f, 0.85f, 1f));
        sr.rect(cx - 30*s, baseY + 62*s, 60*s, 52*s);
        // Ojos parpadeantes
        sr.setColor(BYTE_GREEN);
        float blink = (float)Math.sin(byteAnim * 0.5f);
        float eyeH  = blink > 0.9f ? 2*s : 13*s;
        sr.rect(cx - 20*s, baseY + 88*s, 16*s, eyeH);
        sr.rect(cx + 4*s,  baseY + 88*s, 16*s, eyeH);
        // LEDs boca
        for (int i = 0; i < 4; i++)
            sr.rect(cx - 15*s + i*10*s, baseY + 70*s, 6*s, 6*s);
        // Antena
        sr.setColor(BYTE_BLUE);
        sr.rect(cx - 3*s, baseY + 114*s, 6*s, 22*s);
        sr.setColor(BYTE_GREEN);
        sr.circle(cx, baseY + 140*s, 7*s, 14);
        // Brazos y piernas
        sr.setColor(new Color(0.2f, 0.55f, 0.88f, 1f));
        sr.rect(cx - 54*s, baseY + 34*s, 16*s, 10*s);
        sr.rect(cx + 38*s, baseY + 34*s, 16*s, 10*s);
        sr.rect(cx - 24*s, baseY - 26*s, 16*s, 28*s);
        sr.rect(cx + 8*s,  baseY - 26*s, 16*s, 28*s);
    }

    private float drawWrapped(SpriteBatch batch, String text, float x, float y, float maxW) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            layout.setText(fontBody, test);
            if (layout.width > maxW && line.length() > 0) {
                fontBody.draw(batch, line.toString(), x, lineY);
                lineY -= fontBody.getLineHeight() + 20; // Aumentado interlineado (era +4)
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) fontBody.draw(batch, line.toString(), x, lineY);
        return lineY;
    }

    public boolean isVisible() { return visible; }
    public void resize() { recalc(); }

    public void dispose() {
        fontFormula.dispose();
        fontName.dispose();
        fontBody.dispose();
    }
}
