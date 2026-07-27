package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.board.AtomType;
import com.quimimatch.managers.PlayerInventory;
import com.quimimatch.managers.SkinPalette;
import com.quimimatch.managers.SaveManager;

public class ShopScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer sr;
    private BitmapFont fontTitle;
    private BitmapFont fontBig;
    private BitmapFont font;
    private BitmapFont fontSmall;
    private GlyphLayout layout;

    private float SW, SH;

    // Tabs
    private enum Tab { POWERUPS, SKINS }
    private Tab activeTab = Tab.POWERUPS;

    // Byte
    private float byteFloat = 0f;
    private String byteMsg  = "Bienvenido a la tienda!";

    // Botón volver
    private float btnBackX, btnBackY, btnBackW, btnBackH;

    // Feedback mensaje
    private String feedback     = "";
    private float  feedbackTime = 0f;

    // ── Skins disponibles ────────────────────────────────────────
    private static final String[] SKIN_IDS    = { "default", "neon", "pastel", "metallic", "galaxy" };
    private static final String[] SKIN_NAMES  = { "Clasico", "Neon", "Pastel", "Metalico", "Galaxia" };
    private static final int[]    SKIN_PRICES = { 0, 80, 60, 100, 150 };
    private static final AtomType[] PREVIEW_ATOMS = { AtomType.H, AtomType.O, AtomType.C, AtomType.N };

    // ── Power-ups ────────────────────────────────────────────────
    private static final String[] PUP_NAMES  = { "+5 Movimientos", "Bomba 3x3", "Comodin" };
    private static final String[] PUP_DESC   = {
        "Agrega 5 movimientos extra al nivel actual",
        "Elimina todas las fichas en area 3x3",
        "Elimina todos los atomos del mismo tipo"
    };
    private static final int[]    PUP_PRICES = { 30, 50, 75 };
    private static final Color[]  PUP_COLORS = {
        new Color(0.3f, 0.8f, 0.4f, 1f),
        new Color(1.0f, 0.45f, 0.1f, 1f),
        new Color(0.7f, 0.3f, 1.0f, 1f),
    };

    public ShopScreen(QuimiMatchGame game) {
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
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        byteFloat += delta * 2.0f;
        if (feedbackTime > 0) feedbackTime -= delta;

        handleInput();
        drawHeader();
        drawTabs();
        if (activeTab == Tab.POWERUPS) drawPowerups();
        else                           drawSkins();
        drawBytePanel();
        drawBackButton();
        drawFeedback();
    }

    // ── Input ────────────────────────────────────────────────────

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        float tx = Gdx.input.getX();
        float ty = SH - Gdx.input.getY();

        // Botón volver
        if (tx >= btnBackX && tx <= btnBackX + btnBackW
         && ty >= btnBackY && ty <= btnBackY + btnBackH) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        // Tabs
        float tabY = SH - 115;
        float tabW = SW / 2f;
        if (ty >= tabY && ty <= tabY + 55) {
            if (tx < tabW) activeTab = Tab.POWERUPS;
            else           activeTab = Tab.SKINS;
            return;
        }

        // Power-ups
        if (activeTab == Tab.POWERUPS) {
            for (int i = 0; i < PUP_NAMES.length; i++) {
                float[] btn = pupBuyBtn(i);
                if (tx >= btn[0] && tx <= btn[0] + btn[2]
                 && ty >= btn[1] && ty <= btn[1] + btn[3]) {
                    buyPowerup(i);
                    return;
                }
            }
        }

        // Skins
        if (activeTab == Tab.SKINS) {
            for (int i = 0; i < SKIN_IDS.length; i++) {
                float[] card = skinCard(i);
                if (tx >= card[0] && tx <= card[0] + card[2]
                 && ty >= card[1] && ty <= card[1] + card[3]) {
                    buySkin(i);
                    return;
                }
            }
        }
    }

    // ── Compras ──────────────────────────────────────────────────

    private void buyPowerup(int idx) {
        PlayerInventory inv = PlayerInventory.get();
        boolean ok = false;

        switch (idx) {
            case 0: ok = inv.buyMoves(PUP_PRICES[0]);    break;
            case 1: ok = inv.buyBomb(PUP_PRICES[1]);     break;
            case 2: ok = inv.buyWildcard(PUP_PRICES[2]); break;
        }
        if (ok) {
            SaveManager.saveInventory();
            feedback     = "Comprado! " + PUP_NAMES[idx];
            byteMsg      = "Excelente eleccion!";
        } else {
            feedback     = "Monedas insuficientes!";
            byteMsg      = "Te faltan monedas...";
        }
        feedbackTime = 2f;
    }

    private void buySkin(int idx) {
        PlayerInventory inv = PlayerInventory.get();
        String id = SKIN_IDS[idx];
        if (inv.isSkinUnlocked(id)) {
            inv.activateSkin(id);
            feedback = "Skin activado: " + SKIN_NAMES[idx];
            byteMsg  = "Te ves genial!";
        } else {
            boolean ok = inv.buySkin(id, SKIN_PRICES[idx]);
            if (ok) {
                feedback = "Desbloqueado: " + SKIN_NAMES[idx];
                byteMsg  = "Nueva apariencia!";
            } else {
                feedback = "Monedas insuficientes!";
                byteMsg  = "Te faltan monedas...";
            }
        }
        feedbackTime = 2f;
    }

    // ── Dibujo ───────────────────────────────────────────────────

    private void drawHeader() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.08f, 0.22f, 1f);
        sr.rect(0, SH - 110, SW, 110);
        sr.setColor(1f, 0.8f, 0.1f, 1f);
        sr.rect(0, SH - 112, SW, 3);
        sr.end();

        game.batch.begin();
        fontTitle.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        drawC(fontTitle, "TIENDA", SW / 2f, SH - 18);

        // Monedas
        font.setColor(new Color(1f, 0.9f, 0.2f, 1f));
        int coins = PlayerInventory.get().getCoins();
        drawC(font, "Monedas: " + coins, SW / 2f, SH - 72);
        game.batch.end();

        // Círculo moneda
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(1f, 0.85f, 0.1f, 1f));
        layout.setText(font, "Monedas: " + coins);
        float coinX = SW / 2f - layout.width / 2f - 28;
        sr.circle(coinX, SH - 62, 14, 20);
        sr.end();
    }

    private void drawTabs() {
        float tabY = SH - 115;
        float tabW = SW / 2f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Tab Power-ups
        sr.setColor(activeTab == Tab.POWERUPS
                ? new Color(0.2f, 0.45f, 0.8f, 1f)
                : new Color(0.12f, 0.12f, 0.28f, 1f));
        sr.rect(0, tabY, tabW, 55);

        // Tab Skins
        sr.setColor(activeTab == Tab.SKINS
                ? new Color(0.2f, 0.45f, 0.8f, 1f)
                : new Color(0.12f, 0.12f, 0.28f, 1f));
        sr.rect(tabW, tabY, tabW, 55);

        // Línea activa
        sr.setColor(new Color(0.4f, 0.75f, 1f, 1f));
        if (activeTab == Tab.POWERUPS) sr.rect(0,    tabY, tabW, 4);
        else                           sr.rect(tabW, tabY, tabW, 4);
        sr.end();

        game.batch.begin();
        font.setColor(activeTab == Tab.POWERUPS ? Color.WHITE : new Color(0.6f, 0.6f, 0.8f, 1f));
        drawC(font, "POWER-UPS", tabW / 2f, tabY + 38);
        font.setColor(activeTab == Tab.SKINS ? Color.WHITE : new Color(0.6f, 0.6f, 0.8f, 1f));
        drawC(font, "SKINS", tabW + tabW / 2f, tabY + 38);
        game.batch.end();
    }

    private void drawPowerups() {
        PlayerInventory inv = PlayerInventory.get();
        float contentW = SW * 0.62f;

        for (int i = 0; i < PUP_NAMES.length; i++) {
            float cardY = SH - 210 - i * 170f;
            float cardH = 150f;

            // Fondo tarjeta
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.12f, 0.12f, 0.26f, 1f);
            sr.rect(20, cardY, contentW - 20, cardH);
            // Acento color
            sr.setColor(PUP_COLORS[i]);
            sr.rect(20, cardY, 8, cardH);

            // Ícono
            sr.setColor(PUP_COLORS[i]);
            sr.circle(80, cardY + cardH / 2f, 38, 28);

            // Botón comprar
            float[] btn = pupBuyBtn(i);
            sr.setColor(new Color(0.2f, 0.65f, 0.35f, 1f));
            sr.rect(btn[0], btn[1], btn[2], btn[3]);
            sr.end();

            game.batch.begin();
            // Ícono texto
            fontBig.setColor(Color.WHITE);
            String icon = i == 0 ? "+" : i == 1 ? "X" : "*";
            drawC(fontBig, icon, 80, cardY + cardH / 2f + 16);

            // Nombre y descripción
            font.setColor(Color.WHITE);
            font.draw(game.batch, PUP_NAMES[i], 130, cardY + cardH - 20);
            fontSmall.setColor(new Color(0.7f, 0.7f, 0.9f, 1f));
            fontSmall.draw(game.batch, PUP_DESC[i], 130, cardY + cardH - 56);

            // Stock
            int stock = i == 0 ? inv.getMoves() : i == 1 ? inv.getBombs() : inv.getWildcards();
            fontSmall.setColor(new Color(0.5f, 0.9f, 0.7f, 1f));
            fontSmall.draw(game.batch, "Tienes: " + stock, 130, cardY + 28);

            // Precio
            font.setColor(Color.WHITE);
            drawC(font, PUP_PRICES[i] + " monedas", btn[0] + btn[2] / 2f, btn[1] + 44);
            game.batch.end();
        }
    }

    private float[] pupBuyBtn(int i) {
        float contentW = SW * 0.62f;
        float cardY = SH - 210 - i * 170f;
        float bw = 240, bh = 58;
        float bx = contentW - bw - 10;
        float by = cardY + 46;
        return new float[]{ bx, by, bw, bh };
    }

    private void drawSkins() {
        PlayerInventory inv = PlayerInventory.get();
        float cardW = (SW * 0.62f - 20) / 2f - 10;
        float cardH = 220f;

        for (int i = 0; i < SKIN_IDS.length; i++) {
            int col = i % 2;
            int row = i / 2;
            float cx = 20 + col * (cardW + 10) + cardW / 2f;
            float cy = SH - 220 - row * (cardH + 16);
            float[] card = skinCard(i);

            boolean unlocked = inv.isSkinUnlocked(SKIN_IDS[i]);
            boolean active   = inv.getActiveSkin().equals(SKIN_IDS[i]);

            sr.begin(ShapeRenderer.ShapeType.Filled);
            // Fondo tarjeta
            sr.setColor(active
                    ? new Color(0.18f, 0.32f, 0.60f, 1f)
                    : new Color(0.12f, 0.12f, 0.26f, 1f));
            sr.rect(card[0], card[1], card[2], card[3]);

            // Preview átomos
            for (int a = 0; a < PREVIEW_ATOMS.length; a++) {
                Color atomCol = getSkinColor(SKIN_IDS[i], PREVIEW_ATOMS[a]);
                sr.setColor(atomCol);
                float ax = cx - 48 + a * 32;
                float ay = cy + cardH / 2f + 20;
                sr.circle(ax, ay, 13, 20);
            }

            // Candado si bloqueado
            if (!unlocked) {
                sr.setColor(0f, 0f, 0f, 0.45f);
                sr.rect(card[0], card[1], card[2], card[3]);
            }
            sr.end();

            game.batch.begin();
            // Nombre
            font.setColor(active ? new Color(1f, 0.9f, 0.3f, 1f) : Color.WHITE);
            drawC(font, SKIN_NAMES[i], cx, cy + cardH - 24);

            // Activo / precio
            if (active) {
                fontSmall.setColor(new Color(0.4f, 1f, 0.6f, 1f));
                drawC(fontSmall, "ACTIVO", cx, cy + 14);
            } else if (unlocked) {
                fontSmall.setColor(new Color(0.6f, 0.8f, 1f, 1f));
                drawC(fontSmall, "ACTIVAR", cx, cy + 14);
            } else {
                fontSmall.setColor(new Color(1f, 0.85f, 0.2f, 1f));
                drawC(fontSmall, SKIN_PRICES[i] + " monedas", cx, cy + 14);
            }
            game.batch.end();
        }
    }

    private float[] skinCard(int i) {
        float cardW = (SW * 0.62f - 20) / 2f - 10;
        float cardH = 220f;
        int col = i % 2, row = i / 2;
        float x = 20 + col * (cardW + 10);
        float y = SH - 220 - row * (cardH + 16) - cardH;
        return new float[]{ x, y, cardW, cardH };
    }

    private Color getSkinColor(String skinId, AtomType type) {
        String prev = PlayerInventory.get().getActiveSkin();
        // Temporalmente activa el skin para obtener el color
        PlayerInventory.get().activateSkin(skinId);
        // Pero si no está desbloqueado, simular directamente
        if (!PlayerInventory.get().isSkinUnlocked(skinId)) {
            PlayerInventory.get().activateSkin(prev);
            return new Color(0.35f, 0.35f, 0.35f, 1f);
        }
        Color c = SkinPalette.getColor(type);
        PlayerInventory.get().activateSkin(prev);
        return c;
    }

    // ── Byte Panel ───────────────────────────────────────────────

    private void drawBytePanel() {
        float panelX = SW * 0.65f;
        float panelW = SW - panelX - 10;
        float panelY = 100f;
        float panelH = SH - 200;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.09f, 0.09f, 0.22f, 1f);
        sr.rect(panelX, panelY, panelW, panelH);
        sr.setColor(0.25f, 0.55f, 1f, 1f);
        sr.rect(panelX, panelY + panelH - 4, panelW, 4);
        sr.end();

        float bx = panelX + panelW / 2f;
        float byteY = panelY + panelH * 0.45f + (float)Math.sin(byteFloat) * 7f;
        drawByte(bx, byteY);

        game.batch.begin();
        fontSmall.setColor(new Color(0.5f, 0.9f, 0.7f, 1f));
        drawC(fontSmall, "BYTE", bx, byteY - 20);

        // Inventario resumen
        PlayerInventory inv = PlayerInventory.get();
        font.setColor(new Color(0.7f, 0.8f, 1f, 1f));
        drawC(font, "Inventario", bx, panelY + 160);
        fontSmall.setColor(Color.WHITE);
        drawC(fontSmall, "+Mov x" + inv.getMoves(),    bx, panelY + 120);
        drawC(fontSmall, "Bomba x" + inv.getBombs(),   bx, panelY + 90);
        drawC(fontSmall, "Comodin x" + inv.getWildcards(), bx, panelY + 60);

        // Mensaje de Byte
        fontSmall.setColor(new Color(0.4f, 1f, 0.7f, 1f));
        drawC(fontSmall, byteMsg, bx, panelY + panelH - 30);
        game.batch.end();
    }

    private void drawByte(float cx, float baseY) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.25f, 0.65f, 1f, 1f));
        sr.rect(cx - 38, baseY, 76, 62);
        sr.setColor(new Color(0.18f, 0.50f, 0.85f, 1f));
        sr.rect(cx - 30, baseY + 62, 60, 52);
        sr.setColor(new Color(0.1f, 1f, 0.6f, 1f));
        sr.rect(cx - 20, baseY + 88, 16, 12);
        sr.rect(cx + 4,  baseY + 88, 16, 12);
        for (int i = 0; i < 4; i++) sr.rect(cx - 18 + i * 10, baseY + 70, 7, 7);
        sr.setColor(new Color(0.25f, 0.65f, 1f, 1f));
        sr.rect(cx - 3, baseY + 114, 6, 22);
        sr.setColor(new Color(0.1f, 1f, 0.6f, 1f));
        sr.circle(cx, baseY + 140, 8, 16);
        sr.setColor(new Color(0.2f, 0.55f, 0.88f, 1f));
        sr.rect(cx - 54, baseY + 34, 16, 10);
        sr.rect(cx + 38, baseY + 34, 16, 10);
        sr.rect(cx - 24, baseY - 26, 16, 28);
        sr.rect(cx + 8,  baseY - 26, 16, 28);
        sr.end();
    }

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

    private void drawFeedback() {
        if (feedbackTime <= 0) return;
        game.batch.begin();
        float alpha = Math.min(feedbackTime, 1f);
        fontBig.setColor(1f, 1f, 0.3f, alpha);
        drawC(fontBig, feedback, SW / 2f, SH / 2f + 20);
        game.batch.end();
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
