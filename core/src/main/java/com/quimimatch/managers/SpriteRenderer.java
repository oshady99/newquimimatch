package com.quimimatch.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.quimimatch.board.AtomType;
import com.quimimatch.board.Tile;
import com.quimimatch.board.TileSpecial;

/**
 * Dibuja todos los elementos del juego usando sprites si existen,
 * o fallback a ShapeRenderer si el asset no está disponible.
 * Esto permite desarrollo incremental: el juego funciona sin assets
 * y se mejora visualmente cuando se agregan las imágenes.
 */
public class SpriteRenderer {

    // ── ÁTOMOS ──────────────────────────────────────────────────

    /**
     * Dibuja un átomo — sprite si existe, círculo de color si no.
     */
    public static void drawAtom(SpriteBatch batch, ShapeRenderer sr,
                                Tile tile, float x, float y, float size,
                                boolean selected, float animTime) {
        AssetLoader assets = AssetLoader.get();
        String sym = tile.getType().getSymbol();

        if (assets.hasAtom(sym)) {
            // ── Sprite mode ──────────────────────────────────────
            Texture tex = assets.getAtom(sym);
            if (selected) {
                // Pulso amarillo alrededor
                batch.setColor(1f, 1f, 0.2f, 0.6f + 0.4f * (float)Math.sin(animTime * 6));
                batch.draw(tex, x - 4, y - 4, size + 8, size + 8);
                batch.setColor(Color.WHITE);
            }
            batch.draw(tex, x, y, size, size);

            // Ficha especial — overlay encima del sprite
            if (tile.isSpecial()) {
                drawSpecialOverlay(batch, sr, tile.getSpecial(),
                    x + size / 2f, y + size / 2f, size / 2f, animTime);
            }

        } else {
            // ── ShapeRenderer fallback ───────────────────────────
            drawAtomShape(sr, tile, x, y, size, selected);
        }
    }

    private static void drawAtomShape(ShapeRenderer sr, Tile tile,
                                      float x, float y, float size, boolean selected) {
        float cx = x + size / 2f;
        float cy = y + size / 2f;
        float r  = size / 2f - 5;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Sombra
        sr.setColor(0f, 0f, 0f, 0.35f);
        sr.circle(cx + 2, cy - 2, r, 36);

        // Color del átomo (con skin)
        sr.setColor(SkinPalette.getColor(tile.getType()));
        sr.circle(cx, cy, r, 36);

        // Brillo
        sr.setColor(1f, 1f, 1f, 0.22f);
        sr.circle(cx - r * 0.28f, cy + r * 0.28f, r * 0.32f, 20);

        // Selección
        if (selected) {
            sr.setColor(1f, 1f, 0.2f, 1f);
            sr.rect(x, y, size, 3);
            sr.rect(x, y + size - 3, size, 3);
            sr.rect(x, y, 3, size);
            sr.rect(x + size - 3, y, 3, size);
        }

        sr.end();
    }

    private static void drawSpecialOverlay(SpriteBatch batch, ShapeRenderer sr,
                                           TileSpecial special,
                                           float cx, float cy, float r, float t) {
        // Si hay sprite de especial, úsalo
        AssetLoader assets = AssetLoader.get();
        String key = null;
        switch (special) {
            case LINE_H:   key = "line_h"; break;
            case LINE_V:   key = "line_v"; break;
            case AREA:     key = "area";   break;
            case WILDCARD: key = "wild";   break;
            default: break;
        }
        if (key != null) {
            Texture overlay = assets.getSpecial(key);
            if (overlay != null) {
                batch.draw(overlay, cx - r, cy - r, r * 2, r * 2);
                return;
            }
        }
        // Fallback ShapeRenderer para especiales
        sr.begin(ShapeRenderer.ShapeType.Filled);
        switch (special) {
            case LINE_H:
                sr.setColor(1f, 0.85f, 0f, 0.9f);
                sr.rectLine(cx - r, cy, cx + r, cy, 4f);
                break;
            case LINE_V:
                sr.setColor(1f, 0.85f, 0f, 0.9f);
                sr.rectLine(cx, cy - r, cx, cy + r, 4f);
                break;
            case AREA:
                sr.setColor(1f, 0.5f, 0f, 0.9f);
                sr.rectLine(cx - r, cy, cx + r, cy, 3f);
                sr.rectLine(cx, cy - r, cx, cy + r, 3f);
                break;
            case WILDCARD:
                sr.setColor(1f, 1f, 1f, 0.8f);
                sr.circle(cx, cy, r * 0.35f, 20);
                break;
            default: break;
        }
        sr.end();
    }

    // ── FONDO DE TABLERO ─────────────────────────────────────────

    /**
     * Dibuja el fondo del laboratorio — sprite si existe, degradado si no.
     */
    public static void drawLabBackground(SpriteBatch batch, ShapeRenderer sr,
                                         int world, float sw, float sh) {
        AssetLoader assets = AssetLoader.get();

        if (assets.hasLabBackground(world)) {
            batch.begin();
            batch.draw(assets.getLabBackground(world), 0, 0, sw, sh);
            batch.end();
        } else {
            // Degradado por mundo
            Color[] worldColors = {
                new Color(0.08f, 0.08f, 0.18f, 1f),  // Mundo 1 — azul oscuro
                new Color(0.06f, 0.14f, 0.10f, 1f),  // Mundo 2 — verde oscuro
                new Color(0.14f, 0.06f, 0.06f, 1f),  // Mundo 3 — rojo oscuro
                new Color(0.12f, 0.08f, 0.04f, 1f),  // Mundo 4 — café oscuro
                new Color(0.06f, 0.12f, 0.06f, 1f),  // Mundo 5 — verde bosque
                new Color(0.04f, 0.04f, 0.16f, 1f),  // Mundo 6 — morado oscuro
            };
            int idx = Math.max(0, Math.min(world - 1, 5));
            Color bg = worldColors[idx];
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(bg);
            sr.rect(0, 0, sw, sh);
            sr.end();
        }
    }

    // ── MAX (PERSONAJE RUNNER) ───────────────────────────────────

    /**
     * Dibuja a Max en el runner — sprite animado si existe, forma si no.
     */
    public static void drawMax(SpriteBatch batch, ShapeRenderer sr,
                               float cx, float baseY, float stateTime,
                               boolean jumping, boolean crouching,
                               float jumpOff, float bob) {
        AssetLoader assets = AssetLoader.get();

        float bodyW = 80f;
        float bodyH = crouching ? 55f : 95f;
        float headR = 32f;
        float drawY = baseY + jumpOff + bob;

        if (assets.hasMaxRun()) {
            // ── Sprite mode ──────────────────────────────────────
            Animation<TextureRegion> anim;
            if (jumping)       anim = assets.maxJump;
            else if (crouching) anim = assets.maxCrouch != null ? assets.maxCrouch : assets.maxRun;
            else               anim = assets.maxRun;

            if (anim == null) anim = assets.maxRun;

            TextureRegion frame = anim.getKeyFrame(stateTime, true);
            float spriteW = 560f;
            float spriteH = crouching ? 480f : 720f;

            batch.begin();
            batch.draw(frame, cx - spriteW / 2f, drawY + jumpOff + bob, spriteW, spriteH);
            batch.end();

        } else {
            // ── ShapeRenderer fallback ───────────────────────────
            drawMaxShape(sr, cx, drawY, bodyW, bodyH, headR, stateTime, crouching);
        }
    }

    private static void drawMaxShape(ShapeRenderer sr, float cx, float baseY,
                                     float bodyW, float bodyH, float headR,
                                     float t, boolean crouching) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Sombra
        sr.setColor(0f, 0f, 0f, 0.35f);
        sr.ellipse(cx - 45, baseY - 8, 90, 22);

        // Piernas
        float legPhase = (float)Math.sin(t * 8f);
        sr.setColor(new Color(0.15f, 0.45f, 0.8f, 1f));
        sr.rect(cx - bodyW / 2f + 8, baseY - 30, 26, 35 + legPhase * 10);
        sr.rect(cx + bodyW / 2f - 34, baseY - 30, 26, 35 - legPhase * 10);

        // Cuerpo
        sr.setColor(new Color(0.25f, 0.65f, 1.0f, 1f));
        sr.rect(cx - bodyW / 2f, baseY, bodyW, bodyH);

        // Mochila
        sr.setColor(new Color(0.15f, 0.45f, 0.75f, 1f));
        sr.rect(cx + bodyW / 2f - 16, baseY + bodyH * 0.3f, 18, bodyH * 0.45f);

        // Brazos
        float armSwing = (float)Math.sin(t * 8f) * 18f;
        sr.setColor(new Color(0.25f, 0.65f, 1.0f, 1f));
        sr.rect(cx - bodyW / 2f - 18, baseY + bodyH * 0.55f + armSwing, 18, 14);
        sr.rect(cx + bodyW / 2f,      baseY + bodyH * 0.55f - armSwing, 18, 14);

        // Cabeza
        sr.setColor(new Color(0.88f, 0.72f, 0.58f, 1f));
        sr.circle(cx, baseY + bodyH + headR * 0.8f, headR, 28);

        // Pelo
        sr.setColor(new Color(0.25f, 0.15f, 0.08f, 1f));
        sr.rect(cx - headR, baseY + bodyH + headR, headR * 2f, headR * 0.5f);

        sr.end();
    }

    // ── BYTE (ROBOT) ─────────────────────────────────────────────

    /**
     * Dibuja a Byte — sprite si existe, forma si no.
     */
    public static void drawByte(SpriteBatch batch, ShapeRenderer sr,
                                float cx, float baseY, float scale,
                                float animTime, boolean talking) {
        AssetLoader assets = AssetLoader.get();

        if (assets.hasByteIdle()) {
            Animation<TextureRegion> anim = assets.byteIdle;
            TextureRegion frame = talking && assets.byteTalk != null
                ? new TextureRegion(assets.byteTalk)
                : anim.getKeyFrame(animTime, true);

            float w = 120 * scale, h = 180 * scale;
            batch.begin();
            batch.draw(frame, cx - w / 2f, baseY, w, h);
            batch.end();
        } else {
            drawByteShape(sr, cx, baseY, scale, animTime);
        }
    }

    private static void drawByteShape(ShapeRenderer sr, float cx, float baseY,
                                      float s, float t) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        Color byteBlue  = new Color(0.25f, 0.65f, 1.0f, 1f);
        Color byteGreen = new Color(0.1f,  1.0f,  0.6f, 1f);

        // Cuerpo
        sr.setColor(byteBlue);
        sr.rect(cx - 50*s, baseY, 100*s, 80*s);
        // Cabeza
        sr.setColor(new Color(0.18f, 0.50f, 0.85f, 1f));
        sr.rect(cx - 38*s, baseY + 80*s, 76*s, 65*s);
        // Ojos parpadeantes
        sr.setColor(byteGreen);
        float blink = (float)Math.sin(t * 0.8f);
        float eyeH  = blink > 0.95f ? 2*s : 13*s;
        sr.rect(cx - 26*s, baseY + 108*s, 20*s, eyeH);
        sr.rect(cx + 6*s,  baseY + 108*s, 20*s, eyeH);
        // Boca LEDs
        for (int i = 0; i < 5; i++)
            sr.rect(cx - 24*s + i*11*s, baseY + 88*s, 7*s, 7*s);
        // Antena
        sr.setColor(byteBlue);
        sr.rect(cx - 4*s, baseY + 145*s, 8*s, 28*s);
        sr.setColor(byteGreen);
        float pulse = 0.8f + 0.2f * (float)Math.sin(t * 3f);
        sr.circle(cx, baseY + 178*s, 9*s * pulse, 16);
        // Brazos
        sr.setColor(new Color(0.2f, 0.55f, 0.88f, 1f));
        sr.rect(cx - 68*s, baseY + 44*s, 20*s, 12*s);
        sr.rect(cx + 48*s, baseY + 44*s, 20*s, 12*s);
        // Piernas
        sr.rect(cx - 30*s, baseY - 32*s, 20*s, 34*s);
        sr.rect(cx + 10*s, baseY - 32*s, 20*s, 34*s);

        sr.end();
    }

    // ── OBSTÁCULOS RUNNER ────────────────────────────────────────

    public static void drawObstacleLow(SpriteBatch batch, ShapeRenderer sr,
                                       float cx, float y, float scale) {
        AssetLoader assets = AssetLoader.get();
        float w = 55f * scale, h = 55f * scale;

        if (assets.obsLow != null) {
            batch.begin();
            batch.draw(assets.obsLow, cx - w / 2f, y, w, h);
            batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(0.95f, 0.25f, 0.25f, 1f));
            sr.rect(cx - w / 2f, y, w, h);
            sr.end();
        }
    }

    public static void drawObstacleHigh(SpriteBatch batch, ShapeRenderer sr,
                                        float cx, float y, float scale) {
        AssetLoader assets = AssetLoader.get();
        float w = 55f * scale, h = 85f * scale;

        if (assets.obsHigh != null) {
            batch.begin();
            batch.draw(assets.obsHigh, cx - w / 2f, y, w, h);
            batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(1.0f, 0.55f, 0.05f, 1f));
            sr.rect(cx - w / 2f, y, w, h);
            sr.end();
        }
    }

    // ── UI ───────────────────────────────────────────────────────

    public static void drawStar(SpriteBatch batch, ShapeRenderer sr,
                                float cx, float cy, float r, boolean filled) {
        AssetLoader assets = AssetLoader.get();
        if (assets.hasStars()) {
            Texture t = filled ? assets.starOn : assets.starOff;
            batch.begin();
            batch.draw(t, cx - r, cy - r, r * 2, r * 2);
            batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(filled ? new Color(1f, 0.85f, 0f, 1f) : new Color(0.3f, 0.3f, 0.3f, 1f));
            sr.circle(cx, cy, r, 24);
            sr.end();
        }
    }

    public static void drawHeart(SpriteBatch batch, ShapeRenderer sr,
                                 float cx, float cy, float r, boolean filled) {
        AssetLoader assets = AssetLoader.get();
        if (assets.hasHearts()) {
            Texture t = filled ? assets.heartOn : assets.heartOff;
            batch.begin();
            batch.draw(t, cx - r, cy - r, r * 2, r * 2);
            batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(filled ? new Color(1f, 0.2f, 0.3f, 1f) : new Color(0.35f, 0.35f, 0.35f, 1f));
            sr.circle(cx, cy, r, 24);
            sr.end();
        }
    }

    public static void drawCoin(SpriteBatch batch, ShapeRenderer sr,
                                float cx, float cy, float r) {
        AssetLoader assets = AssetLoader.get();
        if (assets.coin != null) {
            batch.begin();
            batch.draw(assets.coin, cx - r, cy - r, r * 2, r * 2);
            batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(1f, 0.85f, 0.1f, 1f));
            sr.circle(cx, cy, r, 20);
            sr.end();
        }
    }
}
