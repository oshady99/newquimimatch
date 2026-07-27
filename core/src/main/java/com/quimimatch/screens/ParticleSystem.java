package com.quimimatch.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Sprint 3D — Sistema de partículas liviano:
 * - Texto flotante de puntos sobre fichas eliminadas
 * - Partículas de explosión al eliminar matches
 * - Flash de destello al crear ficha especial
 */
public class ParticleSystem {

    // ── Texto flotante ───────────────────────────────────────────
    private static class FloatText {
        float x, y, vy;
        float life, maxLife;
        String text;
        Color color;

        FloatText(float x, float y, String text, Color color) {
            this.x       = x;
            this.y       = y;
            this.vy      = 180f;
            this.text    = text;
            this.color   = color.cpy();
            this.maxLife = 1.2f;
            this.life    = maxLife;
        }
    }

    // ── Partícula de explosión ───────────────────────────────────
    private static class Particle {
        float x, y, vx, vy;
        float life, maxLife;
        float size;
        Color color;

        Particle(float x, float y, float vx, float vy, float size, Color color) {
            this.x       = x;
            this.y       = y;
            this.vx      = vx;
            this.vy      = vy;
            this.size    = size;
            this.color   = color.cpy();
            this.maxLife = 0.6f;
            this.life    = maxLife;
        }
    }

    // ── Flash ────────────────────────────────────────────────────
    private static class Flash {
        float x, y, r;
        float life = 0.25f;
        Color color;

        Flash(float x, float y, float r, Color color) {
            this.x = x; this.y = y; this.r = r;
            this.color = color.cpy();
        }
    }

    // ── Listas activas ───────────────────────────────────────────
    private final List<FloatText> floatTexts = new ArrayList<>();
    private final List<Particle>  particles  = new ArrayList<>();
    private final List<Flash>     flashes    = new ArrayList<>();

    private final Random random = new Random();
    private BitmapFont floatFont;
    private GlyphLayout layout;

    public ParticleSystem() {
        floatFont = new BitmapFont();
        floatFont.getData().setScale(1.8f);
        layout = new GlyphLayout();
    }

    // ── API pública ──────────────────────────────────────────────

    /** Texto de puntos flotante sobre una ficha eliminada */
    public void addScoreText(float x, float y, int pts, Color color) {
        floatTexts.add(new FloatText(x, y, "+" + pts, color));
    }

    /** Explosión de partículas en una posición (al eliminar ficha) */
    public void addExplosion(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            float angle = (float)(Math.PI * 2 * i / count);
            float speed = 120f + random.nextFloat() * 160f;
            float vx    = (float) Math.cos(angle) * speed;
            float vy    = (float) Math.sin(angle) * speed;
            float size  = 4f + random.nextFloat() * 6f;
            particles.add(new Particle(x, y, vx, vy, size, color));
        }
    }

    /** Flash de destello al crear ficha especial */
    public void addFlash(float x, float y, float r, Color color) {
        flashes.add(new Flash(x, y, r, color));
    }

    /** Combo text grande al centro */
    public void addComboText(float x, float y, int combo) {
        String text = "COMBO x" + combo + "!";
        Color c = combo >= 3
                ? new Color(1f, 0.4f, 0.1f, 1f)
                : new Color(1f, 0.9f, 0.2f, 1f);
        FloatText ft = new FloatText(x, y, text, c);
        ft.vy      = 80f;
        ft.maxLife = 1.8f;
        ft.life    = ft.maxLife;
        floatTexts.add(ft);
    }

    // ── Update ───────────────────────────────────────────────────

    public void update(float delta) {
        // Textos flotantes
        Iterator<FloatText> itF = floatTexts.iterator();
        while (itF.hasNext()) {
            FloatText ft = itF.next();
            ft.y    += ft.vy * delta;
            ft.life -= delta;
            if (ft.life <= 0) itF.remove();
        }

        // Partículas
        Iterator<Particle> itP = particles.iterator();
        while (itP.hasNext()) {
            Particle p = itP.next();
            p.x    += p.vx * delta;
            p.y    += p.vy * delta;
            p.vy   -= 300f * delta; // gravedad leve
            p.life -= delta;
            if (p.life <= 0) itP.remove();
        }

        // Flashes
        Iterator<Flash> itFl = flashes.iterator();
        while (itFl.hasNext()) {
            Flash f = itFl.next();
            f.life -= delta;
            if (f.life <= 0) itFl.remove();
        }
    }

    // ── Draw ─────────────────────────────────────────────────────

    public void draw(ShapeRenderer sr, SpriteBatch batch) {
        // Partículas
        if (!particles.isEmpty() || !flashes.isEmpty()) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            for (Particle p : particles) {
                float alpha = p.life / p.maxLife;
                sr.setColor(p.color.r, p.color.g, p.color.b, alpha);
                sr.circle(p.x, p.y, p.size * alpha, 12);
            }
            for (Flash f : flashes) {
                float alpha = (f.life / 0.25f) * 0.6f;
                float r     = f.r * (1f - f.life / 0.25f) * 2.5f;
                sr.setColor(f.color.r, f.color.g, f.color.b, alpha);
                sr.circle(f.x, f.y, r, 24);
            }
            sr.end();
        }

        // Textos flotantes
        if (!floatTexts.isEmpty()) {
            batch.begin();
            for (FloatText ft : floatTexts) {
                float alpha = Math.min(ft.life / ft.maxLife * 2f, 1f);
                floatFont.setColor(ft.color.r, ft.color.g, ft.color.b, alpha);
                layout.setText(floatFont, ft.text);
                floatFont.draw(batch, ft.text,
                        ft.x - layout.width / 2f, ft.y);
            }
            batch.end();
        }
    }

    public void dispose() {
        floatFont.dispose();
    }
}
