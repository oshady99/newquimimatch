package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.board.AtomType;
import com.quimimatch.managers.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** The existing level's second phase: protect the reactor while synthesizing its goals. */
public class MolecularLabScreen implements Screen {
    private enum Phase { INTRO, BATTLE, WON, LOST }
    private static final float W = 1280, H = 720, RX = 650, RY = 350;
    private final QuimiMatchGame game;
    private final GameSession session = GameSession.get();
    private final LevelConfig cfg;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(W, H, camera);
    private final Vector2 touch = new Vector2();
    private final ParticleSystem particles = new ParticleSystem();
    private final List<Projectile> projectiles = new ArrayList<>();
    private Phase phase = Phase.INTRO;
    private AtomType selected;
    private float time, attackTimer = 2.8f, fireCooldown, shieldTime, shieldCooldown, impact, endTimer;
    private int stability = 100;
    private boolean paused, disposed;
    private String message = "Byte: protege el reactor y completa los objetivos.";
    private float messageTime = 3;
    private final Color enemyColor;

    private static class Projectile {
        float x, y, fromX, fromY, t, duration;
        AtomType atom;
        boolean hostile;
        Projectile(float x, float y, boolean hostile, AtomType atom, float duration) {
            this.x = fromX = x; this.y = fromY = y;
            this.hostile = hostile; this.atom = atom; this.duration = duration;
        }
    }

    public MolecularLabScreen(QuimiMatchGame game, Map<AtomType, Integer> runnerAtoms) {
        this.game = game;
        cfg = session.getConfig();
        selected = cfg.atoms[0];
        enemyColor = new Color(0.85f, 0.16f + cfg.world * 0.025f, 0.55f, 1);
        // Runner rewards are a bounded head start, never enough to skip the battle.
        if (runnerAtoms != null) {
            for (int i = 0; i < cfg.goalAtoms.length; i++) {
                int bonus = Math.min(runnerAtoms.getOrDefault(cfg.goalAtoms[i], 0), cfg.goalAmounts[i] / 4);
                for (int n = session.getCollected()[i]; n < bonus; n++) session.collectAtom(cfg.goalAtoms[i]);
            }
        }
        AudioManager.get().muteMusic();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    public MolecularLabScreen(QuimiMatchGame game) { this(game, new EnumMap<AtomType, Integer>(AtomType.class)); }
    @Override public void show() {}
    @Override public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        if (!paused) update(delta);
        if ((phase == Phase.WON || phase == Phase.LOST) && endTimer > 2.2f) {
            restoreViewport();
            if (phase == Phase.WON) game.setScreen(new VictoryScreen(game));
            else game.setScreen(new DefeatScreen(game));
            return;
        }
        Gdx.gl.glClearColor(0.015f, 0.025f, 0.07f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        drawArena();
        drawSprites();
        drawText();
        if (!paused) particles.update(delta);
        particles.draw(shapes, game.batch);
        if (phase == Phase.INTRO || paused) drawOverlay();
        handleInput();
    }
    private void update(float dt) {
        time += dt;
        messageTime -= dt;
        impact = Math.max(0, impact - dt);
        fireCooldown = Math.max(0, fireCooldown - dt);
        shieldTime = Math.max(0, shieldTime - dt);
        shieldCooldown = Math.max(0, shieldCooldown - dt);
        if (phase == Phase.WON || phase == Phase.LOST) { endTimer += dt; return; }
        if (phase != Phase.BATTLE) return;
        attackTimer -= dt;
        if (attackTimer <= 0) {
            projectiles.add(new Projectile(1090, 390, true, null, 2.5f - cfg.world * 0.1f));
            attackTimer = 3.8f - cfg.world * 0.15f;
            say("¡Carga contaminante! Prepara el escudo.");
        }
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.t += dt / p.duration;
            p.x = MathUtils.lerp(p.fromX, RX, Math.min(1, p.t));
            p.y = MathUtils.lerp(p.fromY, RY, Math.min(1, p.t)) + MathUtils.sin(p.t * MathUtils.PI) * 55;
            if (p.t < 1) continue;
            it.remove();
            if (p.hostile) {
                if (shieldTime > 0) {
                    AudioManager.get().playSpecial();
                    burst(RX + 90, RY, Color.CYAN);
                    say("¡Ataque bloqueado!");
                } else damage(20, "¡Impacto! Estabilidad -20");
            } else {
                int added = 0;
                for (int i = 0; i < 3; i++) if (session.collectAtom(p.atom)) added++;
                if (added > 0) {
                    session.registerCorrectAction(180 * added);
                    AudioManager.get().playCollect();
                    burst(RX, RY, SkinPalette.getColor(p.atom));
                    say("+" + added + " " + p.atom.getSymbol() + " al reactor");
                } else {
                    session.registerIncorrectAction();
                    damage(10, "Ese atomo ya no hace falta. Estabilidad -10");
                }
            }
        }
        if (session.isVictory()) {
            phase = Phase.WON;
            // Existing star thresholds remain meaningful in the new game mode.
            int targetScore = stability >= 80 ? cfg.starScore3 : stability >= 50 ? cfg.starScore2 : 0;
            session.addScore(Math.max(0, targetScore - session.getScore()));
            projectiles.clear();
            AudioManager.get().playVictory();
            for (int i = 0; i < 7; i++) burst(400 + i * 100, 350, i % 2 == 0 ? Color.CYAN : Color.GOLD);
        } else if (stability <= 0 || (session.getMovesLeft() == 0 && !hasFriendlyShots())) {
            phase = Phase.LOST;
            projectiles.clear();
            AudioManager.get().playDefeat();
        }
    }
    private boolean hasFriendlyShots() {
        for (Projectile p : projectiles) if (!p.hostile) return true;
        return false;
    }
    private void say(String value) { message = value; messageTime = 2.3f; }
    private void burst(float x, float y, Color c) {
        particles.addExplosion(x, y, c, 20);
        particles.addFlash(x, y, 75, c);
    }
    private void damage(int amount, String text) {
        stability = Math.max(0, stability - amount);
        impact = 0.35f;
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) Gdx.input.vibrate(120);
        AudioManager.get().playObstacle(); burst(RX, RY, Color.RED); say(text);
    }
    private void fire() {
        if (fireCooldown > 0 || session.getMovesLeft() <= 0 || hasFriendlyShots()) return;
        session.useMove();
        projectiles.add(new Projectile(270, 230, false, selected, 0.45f));
        fireCooldown = 0.3f;
        AudioManager.get().playSwap();
    }
    private void activateShield() {
        if (shieldCooldown > 0) { say("Escudo recargando..."); return; }
        shieldTime = 1.25f; shieldCooldown = 3f;
        AudioManager.get().playPowerup();
    }
    private void drawArena() {
        Texture bg = AssetLoader.get().getLabBackground(cfg.world);
        if (bg != null) {
            game.batch.begin(); game.batch.setColor(0.33f, 0.38f, 0.48f, 1);
            game.batch.draw(bg, 0, 0, W, H); game.batch.setColor(Color.WHITE); game.batch.end();
        }
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.035f, 0.07f, 0.14f, 1); shapes.rect(0, 610, W, 110); shapes.rect(0, 0, W, 150);
        shapes.setColor(0.08f, 0.2f, 0.29f, 1); shapes.rect(500, 180, 300, 30);
        shapes.setColor(impact > 0 ? Color.SCARLET : new Color(0.08f, 0.25f, 0.32f, 1));
        shapes.circle(RX, RY, 102, 64);
        shapes.setColor(Color.CYAN); shapes.circle(RX, RY, 37 + MathUtils.sin(time * 3) * 5, 40);
        // Goal groups orbit the reactor and light up as each target is completed.
        for (int i = 0; i < cfg.goalAtoms.length; i++) {
            float a = time * 0.35f + i * MathUtils.PI2 / cfg.goalAtoms.length;
            float x = RX + MathUtils.cos(a) * 80, y = RY + MathUtils.sin(a) * 80;
            shapes.setColor(SkinPalette.getColor(cfg.goalAtoms[i]));
            shapes.rectLine(RX, RY, x, y, 4);
            shapes.circle(x, y, 12 + session.getGoalProgress(i) * 12, 32);
        }
        if (phase == Phase.WON) {
            shapes.setColor(Color.CYAN);
            float r = endTimer * 700;
            shapes.circle(1090, 390, Math.max(0, 65 - endTimer * 45), 48);
            shapes.end(); shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.circle(RX, RY, r, 80);
            shapes.end(); shapes.begin(ShapeRenderer.ShapeType.Filled);
        } else {
            float ey = 390 + MathUtils.sin(time * 2) * 15;
            shapes.setColor(enemyColor); shapes.circle(1090, ey, 63, 48);
            for (int i = 0; i < 8; i++) {
                float a = time + i * MathUtils.PI2 / 8;
                shapes.circle(1090 + MathUtils.cos(a) * 82, ey + MathUtils.sin(a) * 82, 9, 16);
            }
            shapes.setColor(Color.WHITE); shapes.triangle(1053, ey + 20, 1080, ey + 7, 1058, ey - 3);
            shapes.triangle(1127, ey + 20, 1100, ey + 7, 1122, ey - 3);
            shapes.setColor(0.15f, 0, 0.07f, 1); shapes.rect(1070, ey - 27, 40, 8);
        }
        for (Projectile p : projectiles) {
            shapes.setColor(p.hostile ? Color.SCARLET : SkinPalette.getColor(p.atom));
            shapes.circle(p.x, p.y, p.hostile ? 23 : 19, 24);
            shapes.rectLine(p.x, p.y, p.x + (p.hostile ? 55 : -45), p.y - 9, 7);
        }
        for (int i = 0; i < 3; i++) {
            shapes.setColor(0.12f, 0.23f, 0.35f, 1);
            shapes.rect(640 + i * 200, 150, 190, 45);
        }
        // Readable, large controls and goal counters.
        for (int i = 0; i < cfg.atoms.length; i++) {
            float x = 48 + i * 128;
            shapes.setColor(selected == cfg.atoms[i] ? Color.CYAN : new Color(0.15f, 0.23f, 0.32f, 1));
            shapes.rect(x, 35, 112, 88);
            shapes.setColor(SkinPalette.getColor(cfg.atoms[i])); shapes.circle(x + 56, 79, 29, 32);
        }
        shapes.setColor(shieldCooldown == 0 ? new Color(0.05f, 0.55f, 0.65f, 1) : Color.DARK_GRAY);
        shapes.rect(820, 35, 185, 88);
        shapes.setColor(new Color(0.72f, 0.32f, 0.06f, 1)); shapes.rect(1030, 35, 205, 88);
        shapes.setColor(Color.DARK_GRAY); shapes.rect(28, 626, 250, 12);
        shapes.setColor(stability > 40 ? Color.CYAN : Color.SCARLET); shapes.rect(28, 626, 250 * stability / 100f, 12);
        shapes.end();
        if (shieldTime > 0) {
            shapes.begin(ShapeRenderer.ShapeType.Line); shapes.setColor(Color.CYAN);
            for (int i = 0; i < 4; i++) shapes.circle(RX, RY, 115 + i * 2, 64);
            shapes.end();
        }
    }
    private void drawSprites() {
        AssetLoader a = AssetLoader.get();
        Texture max = phase == Phase.WON && a.maxThumbsUp != null ? a.maxThumbsUp : a.maxIdle;
        game.batch.begin();
        if (impact > 0) game.batch.setColor(1, 0.3f, 0.3f, 1);
        sprite(max, 225 + (impact > 0 ? MathUtils.sin(time * 70) * 12 : 0), 148, 200, 300);
        game.batch.setColor(Color.WHITE);
        sprite(phase == Phase.WON ? a.byteHappy : a.byteTalk, 395, 410, 125, 135);
        game.batch.end();
    }
    private void sprite(Texture tex, float cx, float y, float maxW, float maxH) {
        if (tex == null) return;
        float scale = Math.min(maxW / tex.getWidth(), maxH / tex.getHeight());
        float w = tex.getWidth() * scale, h = tex.getHeight() * scale;
        game.batch.draw(tex, cx - w / 2, y, w, h);
    }
    private void drawText() {
        game.batch.begin();
        text("LABORATORIO MOLECULAR   " + cfg.world + "-" + cfg.level, 28, 695, 1.6f, Color.CYAN);
        text("Estabilidad " + stability + "%", 28, 662, 1.4f, Color.WHITE);
        text("Pulsos: " + session.getMovesLeft(), 310, 662, 1.4f, Color.WHITE);
        text("PAUSA", 1165, 680, 1.4f, Color.WHITE);
        StringBuilder goals = new StringBuilder("Objetivos: ");
        for (int i = 0; i < cfg.goalAtoms.length; i++) goals.append(cfg.goalAtoms[i].getSymbol()).append(' ')
            .append(session.getCollected()[i]).append('/').append(cfg.goalAmounts[i]).append("   ");
        text(goals.toString(), 510, 662, 1.35f, Color.GOLD);
        PlayerInventory inv = PlayerInventory.get();
        text("+5 PULSOS x" + inv.getMoves(), 650, 180, 1.1f, Color.WHITE);
        text("PURGA x" + inv.getBombs(), 850, 180, 1.1f, Color.WHITE);
        text("COMODIN x" + inv.getWildcards(), 1050, 180, 1.1f, Color.WHITE);
        text("NUCLEO INESTABLE", 960, 535, 1.5f, enemyColor);
        text(cfg.moleculeName, 545, 505, 1.7f, Color.CYAN);
        if (messageTime > 0) text(message, 330, 576, 1.4f, Color.WHITE);
        for (int i = 0; i < cfg.atoms.length; i++) text(cfg.atoms[i].getSymbol(), 88 + i * 128, 87, 1.65f, Color.WHITE);
        text(shieldTime > 0 ? "ACTIVO" : shieldCooldown > 0 ? "RECARGA " + (int)Math.ceil(shieldCooldown) : "ESCUDO", 838, 88, 1.5f, Color.WHITE);
        text("DISPARAR x3", 1045, 88, 1.6f, Color.WHITE);
        text("Elige un atomo y dispara al reactor. Bloquea las cargas rojas con el escudo.", 35, 22, 1.2f, Color.LIGHT_GRAY);
        if (phase == Phase.WON) text("¡REACTOR ESTABLE! ¡Nucleo neutralizado!", 370, 285, 2, Color.GOLD);
        if (phase == Phase.LOST) text(stability <= 0 ? "REACTOR INESTABLE" : "SIN PULSOS", 470, 285, 2, Color.SCARLET);
        game.batch.end();
    }
    private void text(String value, float x, float y, float scale, Color color) {
        font.getData().setScale(scale); font.setColor(color); font.draw(game.batch, value, x, y);
    }
    private void drawOverlay() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.025f, 0.06f, 0.14f, 1); shapes.rect(235, 175, 810, 370);
        shapes.setColor(Color.CYAN); shapes.rect(235, 540, 810, 5);
        shapes.setColor(0.07f, 0.5f, 0.58f, 1); shapes.rect(450, 200, 380, 65);
        shapes.end();
        game.batch.begin();
        text(paused ? "PAUSA" : "BYTE: ¡EL LABORATORIO ESTA BAJO ATAQUE!", 275, 500, 1.8f, Color.CYAN);
        text("1. Selecciona un atomo de los objetivos de arriba.", 280, 445, 1.6f, Color.WHITE);
        text("2. DISPARAR envia hasta 3 atomos hacia el reactor.", 280, 403, 1.6f, Color.WHITE);
        text("3. Activa ESCUDO justo antes de que llegue una carga roja.", 280, 361, 1.5f, Color.WHITE);
        text("Conserva 80% de estabilidad para ganar 3 estrellas.", 280, 312, 1.4f, Color.GOLD);
        text(paused ? "CONTINUAR" : "ENTRAR AL LABORATORIO", 480, 241, 1.6f, Color.WHITE);
        game.batch.end();
    }
    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        touch.set(Gdx.input.getX(), Gdx.input.getY()); viewport.unproject(touch);
        float x = touch.x, y = touch.y;
        if (phase == Phase.INTRO || paused) {
            if (x >= 450 && x <= 830 && y >= 200 && y <= 265) {
                paused = false; phase = Phase.BATTLE;
            }
            return;
        }
        if (phase != Phase.BATTLE) return;
        if (x >= 1150 && y >= 640) { paused = true; return; }
        if (y >= 150 && y <= 195 && x >= 640 && x <= 1230) {
            useAid((int)((x - 640) / 200)); return;
        }
        if (y >= 35 && y <= 123) {
            for (int i = 0; i < cfg.atoms.length; i++) {
                if (x >= 48 + i * 128 && x <= 160 + i * 128) { selected = cfg.atoms[i]; return; }
            }
            if (x >= 820 && x <= 1005) activateShield();
            else if (x >= 1030 && x <= 1235) fire();
        } else if (Vector2.dst(x, y, RX, RY) <= 115) fire();
    }
    private void useAid(int type) {
        PlayerInventory inv = PlayerInventory.get();
        boolean used = false;
        if (type == 0 && inv.useMoves()) {
            session.addMoves(5); used = true; say("Cinco pulsos extra disponibles.");
        } else if (type == 1 && inv.useBomb()) {
            projectiles.removeIf(p -> p.hostile);
            stability = Math.min(100, stability + 15);
            burst(RX, RY, Color.CYAN); used = true; say("Purga: cargas eliminadas y +15 estabilidad.");
        } else if (type == 2 && !hasFriendlyShots()) {
            for (int i = 0; i < cfg.goalAtoms.length; i++) {
                if (session.getCollected()[i] < cfg.goalAmounts[i] && inv.useWildcard()) {
                    projectiles.add(new Projectile(270, 230, false, cfg.goalAtoms[i], 0.45f));
                    used = true; say("Comodin: pulso correcto sin gastar disparos."); break;
                }
            }
        }
        if (used) { SaveManager.saveInventory(); AudioManager.get().playPowerup(); }
        else say("Ayuda no disponible o espera a que llegue tu disparo.");
    }
    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() { if (phase == Phase.BATTLE) paused = true; }
    @Override public void resume() {}
    private void restoreViewport() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    @Override public void hide() { restoreViewport(); dispose(); }
    @Override public void dispose() {
        if (disposed) return;
        disposed = true; shapes.dispose(); font.dispose(); particles.dispose();
    }
}
