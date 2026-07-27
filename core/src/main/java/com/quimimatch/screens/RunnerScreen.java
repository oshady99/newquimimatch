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
import com.quimimatch.managers.GameSession;
import com.quimimatch.managers.LevelConfig;
import com.quimimatch.managers.AssetLoader;
import com.quimimatch.managers.AudioManager;
import com.quimimatch.managers.EquationUtil;
import com.quimimatch.managers.PlayerInventory;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.quimimatch.managers.ReactionManager;

public class RunnerScreen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer sr;
    private BitmapFont font;
    private BitmapFont fontBig;
    private BitmapFont fontSmall;
    private GlyphLayout layout;
    private Random random = new Random();

    private float SW, SH;

    // ── Perspectiva ──────────────────────────────────────────────
    // El "horizonte" está en el centro de la pantalla
    // Los objetos empiezan pequeños al fondo y crecen al acercarse
    private static final float HORIZON_Y  = 0.58f; // fracción de SH
    private static final float VANISH_X   = 0.5f;  // fracción de SW (punto de fuga)

    // ── Max (espalda al jugador) ─────────────────────────────────
    private int   lane     = 1;
    private int   targetLane = 1;
    private float laneT    = 1f;   // interpolación 0→1 entre carriles
    private float bobTimer = 0f;   // animación de carrera

    private boolean crouching = false;
    private boolean jumping   = false;
    private float   jumpT     = 0f; // 0→1→0 arco de salto

    // ── Obstáculos 3D ────────────────────────────────────────────
    private static class Obj3D {
        float z;        // profundidad: 1.0=horizonte, 0.0=pantalla
        int   lane;
        boolean isHigh; // alto=agacharse, bajo=saltar
        boolean isAtom; // es colectable
        AtomType atomType;
        boolean hit = false;

        Obj3D(float z, int lane, boolean isHigh) {
            this.z = z; this.lane = lane; this.isHigh = isHigh;
        }
        Obj3D(float z, int lane, AtomType type) {
            this.z = z; this.lane = lane; this.isAtom = true; this.atomType = type;
        }
    }

    private List<Obj3D> objects = new ArrayList<>();
    private float spawnTimer = 0f;
    private float spawnInterval = 0.8f; // Reducido para que salgan más seguido (era 1.6)
    private float worldSpeed = 0.6f; // unidades z/segundo (crece con el tiempo)

    // ── Confeti ──────────────────────────────────────────────────
    private static class Confetti {
        float x, y, vx, vy, rot, rotV, size;
        Color color;
        float life = 3f;
        Confetti(float x, float y, Color c) {
            this.x = x; this.y = y; this.color = c.cpy();
            vx = (float)(Math.random() * 400 - 200);
            vy = (float)(Math.random() * 600 + 200);
            rot = (float)(Math.random() * 360);
            rotV = (float)(Math.random() * 360 - 180);
            size = 8f + (float)(Math.random() * 14);
        }
    }
    private List<Confetti> confetti = new ArrayList<>();
    private boolean confettiLaunched = false;

    // ── Ecuación química objetivo (Fase 1 — La Fuga Nuclear) ─────
    // repeatFactor controla cuántos "ciclos" de la proporción balanceada
    // hay que juntar durante los 14s del runner.
    private static final int RUNNER_REPEAT_FACTOR = 3;
    private EquationUtil.RunnerTarget equationTarget;
    private ReactionManager reactionManager;
    private boolean reactionComplete = false;
    private float reactionCelebrateT = 0f;

    // ── Recolección ──────────────────────────────────────────────
    private Map<AtomType, Integer> collected = new LinkedHashMap<>();
    private List<String>  floatLabels = new ArrayList<>();
    private List<float[]> floatPos    = new ArrayList<>();
    private List<Float>   floatLife   = new ArrayList<>();

    // ── Tiempo ───────────────────────────────────────────────────
    private float runTimer = 0f;
    private static final float RUN_DURATION = 14f;
    private float stateTime = 0f;

    // ── Estado ───────────────────────────────────────────────────
    private enum State { RUNNING, SUMMARY }
    private State state = State.RUNNING;
    private float summaryTimer = 4f;

    // Byte animación
    private float byteFloat = 0f;

    // ── D-Pad (lado IZQUIERDO) ───────────────────────────────────
    private float dpadCX, dpadCY;
    private static final float DPAD_R  = 90f;
    private static final float DPAD_BR = 50f;

    private boolean btnUp, btnDown, btnLeft, btnRight;
    private boolean prevUp, prevLeft, prevRight;

    // ── Colores ──────────────────────────────────────────────────
    private static final Color COL_SKY_TOP = new Color(0.05f, 0.08f, 0.22f, 1f);
    private static final Color COL_SKY_BOT = new Color(0.12f, 0.18f, 0.38f, 1f);
    private static final Color COL_ROAD_LN = new Color(0.28f, 0.28f, 0.50f, 1f);
    private static final Color COL_OBS_LO  = new Color(0.95f, 0.25f, 0.25f, 1f);
    private static final Color COL_OBS_HI  = new Color(1.0f,  0.55f, 0.05f, 1f);
    private static final Color COL_MAX     = new Color(0.25f, 0.65f, 1.0f,  1f);
    private static final Color COL_DPAD_BG = new Color(0.10f, 0.10f, 0.28f, 0.80f);
    private static final Color COL_DPAD_PR = new Color(0.45f, 0.45f, 1.0f,  1f);
    private static final Color COL_DPAD_N  = new Color(0.25f, 0.25f, 0.55f, 0.90f);

    public RunnerScreen(QuimiMatchGame game) {
        this.game = game;
        SW = Gdx.graphics.getWidth();
        SH = Gdx.graphics.getHeight();

        sr        = new ShapeRenderer();
        font      = new BitmapFont(); font.getData().setScale(2.8f); // Aumentado de 1.8
        fontBig   = new BitmapFont(); fontBig.getData().setScale(4.5f); // Aumentado de 2.5
        fontSmall = new BitmapFont(); fontSmall.getData().setScale(2.2f); // Aumentado de 1.4
        layout    = new GlyphLayout();
        AudioManager.get().startWorldMusic(GameSession.get().getConfig().world);

        // Alternamos Composición / Descomposición según el nivel.
        // Niveles pares (0,2...) = Composición ("arma la ecuación").
        // Niveles impares (1,3...) = Descomposición.
        // NOTA: hoy ambos modos comparten la misma mecánica de recolección;
        // la distinción visual/de reglas propia de Descomposición queda PENDIENDTE
        // pendiente para la siguiente sesión (ver comentario en drawHUD).
        int levelIdx = GameSession.get().getCurrentLevel();
        boolean decomposeMode = (levelIdx % 2 == 1);
        equationTarget = EquationUtil.fromLevelConfig(
            GameSession.get().getConfig(),
            decomposeMode,
            RUNNER_REPEAT_FACTOR
        );
        reactionManager = new ReactionManager(equationTarget);


        // D-Pad lado IZQUIERDO
        dpadCX = 220f;
        dpadCY = 200f;
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        stateTime += delta;
        Gdx.gl.glClearColor(0.05f, 0.08f, 0.22f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state == State.RUNNING) {
            updateRunner(delta);
            drawScene();
            drawHUD();
            drawDpad();
            drawFloatTexts(delta);
        } else {
            drawScene();
            updateConfetti(delta);
            drawConfetti();
            drawSummary(delta);
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────

    private void updateRunner(float delta) {
        runTimer  += delta;

        // La animación solo avanza si estamos cambiando de carril
        if (laneT < 1f) {
            bobTimer  += delta * 12f;
            stateTime += delta;
        } else {
            // Un pequeño balanceo idle
            bobTimer += delta * 2f;
        }

        worldSpeed = 0.55f + runTimer * 0.025f;

        readDpad();

        // Salto
        if (btnUp && !prevUp && !jumping) {
            jumping = true;
            jumpT   = 0f;
            crouching = false;
        }
        if (jumping) {
            jumpT += delta * 2.2f;
            if (jumpT >= 1f) { jumpT = 1f; jumping = false; }
        }
        crouching = btnDown && !jumping;

        // Carril
        if (btnLeft && !prevLeft && targetLane > 0) {
            targetLane--; laneT = 0f;
        }
        if (btnRight && !prevRight && targetLane < 2) {
            targetLane++; laneT = 0f;
        }
        if (laneT < 1f) laneT = Math.min(laneT + delta * 8f, 1f);
        float curLaneF = lane + (targetLane - lane) * easeInOut(laneT);
        if (laneT >= 1f) lane = targetLane;

        prevUp = btnUp; prevLeft = btnLeft; prevRight = btnRight;

        // Mover objetos
        Iterator<Obj3D> it = objects.iterator();
        while (it.hasNext()) {
            Obj3D o = it.next();
            o.z -= worldSpeed * delta;
            if (o.z <= 0f) { it.remove(); continue; }

            // Colisión cuando está muy cerca
            if (!o.hit && o.z < 0.12f && o.lane == Math.round(curLaneF)) {
                if (o.isAtom) {
                    o.hit = true;
                    collected.put(o.atomType, collected.getOrDefault(o.atomType, 0) + 1);
                    AudioManager.get().playCollect();
                    float[] sp = screenPos(o.lane, o.z);
                    floatLabels.add("+" + o.atomType.getSymbol());
                    floatPos.add(new float[]{ sp[0], sp[1] });
                    floatLife.add(1.0f);
                    it.remove();

                    // ¿Se completó la ecuación balanceada?
                    if (!reactionComplete && equationTarget.isComplete(collected)) {
                        reactionComplete = true;
                        reactionCelebrateT = 2.5f;
                        floatLabels.add("¡ECUACION BALANCEADA!");
                        floatPos.add(new float[]{ SW / 2f - 220f, SH * 0.65f });
                        floatLife.add(2.0f);
                        PlayerInventory.get().addCoins(25); // bono por balancear correctamente
                    }
                } else {
                    // Obstáculo — verificar si el jugador esquivó
                    boolean evaded = (o.isHigh && crouching) || (!o.isHigh && jumping);
                    o.hit = true;
                    if (!evaded) {
                        AudioManager.get().playObstacle();
                        GameSession.get().loseLife(); // Restar vida al chocar
                        floatLabels.add("¡VIDA -1!");
                        float[] sp = screenPos(o.lane, o.z);
                        floatPos.add(new float[]{ sp[0], sp[1] + 60 });
                        floatLife.add(1.0f);
                    }
                }
            }
        }

        // Spawn
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            // Intervalo más corto y dinámico para mayor ritmo
            spawnInterval = 0.6f + random.nextFloat() * 0.8f;
            spawnObject();
        }

        if (runTimer >= RUN_DURATION) {
            state = State.SUMMARY;
            launchConfetti();
        }
    }

    private void readDpad() {
        btnUp = false; btnDown = false; btnLeft = false; btnRight = false;
        for (int p = 0; p < 5; p++) {
            if (!Gdx.input.isTouched(p)) continue;
            float tx = Gdx.input.getX(p);
            float ty = SH - Gdx.input.getY(p);
            float dx = tx - dpadCX, dy = ty - dpadCY;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < DPAD_R * 1.6f) {
                if (Math.abs(dy) > Math.abs(dx)) {
                    if (dy >  15) btnUp   = true;
                    if (dy < -15) btnDown = true;
                } else {
                    if (dx < -15) btnLeft  = true;
                    if (dx >  15) btnRight = true;
                }
            }
        }
    }

    private void spawnObject() {
        int l = random.nextInt(3);
        float z = 0.95f;

        // Mayor probabilidad de átomos y mejor alternancia
        if (random.nextFloat() < 0.65f) {
            // Colectable (Átomo)
            LevelConfig cfg = GameSession.get().getConfig();
            AtomType type = cfg.atoms[random.nextInt(cfg.atoms.length)];
            objects.add(new Obj3D(z, l, type));
        } else {
            // Obstáculo (Alternando alto/bajo)
            objects.add(new Obj3D(z, l, random.nextBoolean()));
        }
    }

    // ── DIBUJO ESCENA 3D ─────────────────────────────────────────

    private void drawScene() {
        AssetLoader assets = AssetLoader.get();
        int world = GameSession.get().getConfig().world;

        // ── 1. Fondo (Imagen a Pantalla Completa) ───────────────
        game.batch.begin();
        if (assets.hasRunnerBackground(world)) {
            game.batch.draw(assets.getRunnerBackground(world), 0, 0, SW, SH);
        } else {
            // Fallback si no hay imagen
            game.batch.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(COL_SKY_TOP);
            sr.rect(0, 0, SW, SH);
            sr.end();
            game.batch.begin();
        }
        game.batch.end();

        // ── 2. Objetos y Max ────────────────────────────────────
        objects.sort((a, b) -> Float.compare(b.z, a.z));
        for (Obj3D o : objects) {
            if (!o.hit || o.isAtom) drawObject(o);
        }

        drawMax();
    }

    private void drawObject(Obj3D o) {
        float[] pos = screenPos(o.lane, o.z);
        float cx = pos[0], cy = pos[1];
        float scale = pos[2];

        AssetLoader assets = AssetLoader.get();

        if (o.isAtom) {
            String sym = o.atomType.getSymbol();
            if (assets.hasAtom(sym)) {
                game.batch.begin();
                float size = 140f * scale; // Reducido más (era 180)
                game.batch.draw(assets.getAtom(sym), cx - size / 2f, cy - size / 2f, size, size);
                game.batch.end();
            } else {
                sr.begin(ShapeRenderer.ShapeType.Filled);
                // Átomo colectable — círculo brillante
                float r = 60f * scale; // Reducido más (era 80)
                sr.setColor(0f, 0f, 0f, 0.4f);
                sr.circle(cx + r * 0.15f, cy - r * 0.15f, r, 24);
                sr.setColor(o.atomType.getColor());
                sr.circle(cx, cy, r, 24);
                sr.end();
            }

        } else {
            Texture obsTex = o.isHigh ? assets.obsHigh : assets.obsLow;
            if (obsTex != null) {
                game.batch.begin();
                float w = obsTex.getWidth() * scale * 1.5f; // Reducido más (era 2.0)
                float h = obsTex.getHeight() * scale * 1.5f;
                game.batch.draw(obsTex, cx - w / 2f, cy, w, h);
                game.batch.end();
            } else {
                sr.begin(ShapeRenderer.ShapeType.Filled);
                float w = 130f * scale; // Reducido más (era 170)
                float h = (o.isHigh ? 200f : 130f) * scale; // Reducido más
                Color c = o.isHigh ? COL_OBS_HI : COL_OBS_LO;

                // Sombra
                sr.setColor(0f, 0f, 0f, 0.35f);
                sr.rect(cx - w / 2f + 4, cy - 4, w, h);
                // Cuerpo
                sr.setColor(c);
                sr.rect(cx - w / 2f, cy, w, h);
                // Brillo top
                sr.setColor(1f, 1f, 1f, 0.18f);
                sr.rect(cx - w / 2f, cy + h - 8 * scale, w, 8 * scale);
                sr.end();
            }

            // Hint
            game.batch.begin();
            fontSmall.setColor(Color.WHITE);
            String hint = o.isHigh ? "AGACHA" : "SALTA";
            layout.setText(fontSmall, hint);
            fontSmall.draw(game.batch, hint,
                cx - layout.width / 2f, cy + (o.isHigh ? 100f : 65f) * scale + 24 * scale);
            game.batch.end();
        }
    }

    private void drawMax() {
        // Max está siempre en el centro-inferior de la pantalla
        float maxCX = SW * VANISH_X;

        // Interpolación de carril visual
        float laneF  = lane + (targetLane - lane) * easeInOut(laneT);
        float nearX0 = SW * 0.15f;
        float nearX2 = SW * 0.85f;
        float laneNearX = nearX0 + (nearX2 - nearX0) * (laneF / 2f);

        float bob    = jumping ? 0f : (float)Math.sin(bobTimer) * 6f;
        float jumpOff = jumping ? (float)Math.sin(jumpT * Math.PI) * 160f : 0f;

        // Bajamos a Max para que no esté tan "al medio"
        float py = -120f + jumpOff + bob; // Movido aún más atrás (hacia abajo)

        AssetLoader assets = AssetLoader.get();
        TextureRegion currentFrame = null;

        if (jumping && assets.maxJump != null) {
            currentFrame = assets.maxJump.getKeyFrame(jumpT, false);
        } else if (crouching && assets.maxCrouch != null) {
            currentFrame = assets.maxCrouch.getKeyFrame(stateTime, true);
        } else if (laneT < 1f && assets.maxRun != null) {
            // Solo corre si se está moviendo de carril
            currentFrame = assets.maxRun.getKeyFrame(stateTime, true);
        } else {
            // Imagen estática si no se mueve
            if (assets.maxIdle != null) {
                currentFrame = new TextureRegion(assets.maxIdle);
            } else if (assets.maxRun != null) {
                currentFrame = assets.maxRun.getKeyFrame(0, false);
            }
        }

        if (currentFrame != null) {
            game.batch.begin();
            float scale = 1.8f; // Max más grande
            float w = currentFrame.getRegionWidth() * scale;
            float h = currentFrame.getRegionHeight() * scale;
            game.batch.draw(currentFrame, laneNearX - w / 2f, py, w, h);
            game.batch.end();
        } else {
            // Fallback a dibujo con formas si no hay assets
            float bodyW  = 80f;
            float bodyH  = crouching ? 55f : 95f;
            float headR  = 32f;
            float bx = laneNearX - bodyW / 2f;
            float by = py;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            // Piernas
            float legPhase = (float)Math.sin(bobTimer);
            sr.setColor(new Color(0.15f, 0.45f, 0.8f, 1f));
            sr.rect(bx + 8,          by - 30, 26, 35 + legPhase * 10);
            sr.rect(bx + bodyW - 34, by - 30, 26, 35 - legPhase * 10);
            // Cuerpo
            sr.setColor(COL_MAX);
            sr.rect(bx, by, bodyW, bodyH);
            // Cabeza
            sr.setColor(new Color(0.88f, 0.72f, 0.58f, 1f));
            sr.circle(laneNearX, by + bodyH + headR * 0.8f, headR, 28);
            sr.end();
        }
    }

    // ── Byte (robot asistente en summary) ───────────────────────

    private void drawByte(float cx, float byteY) {
        AssetLoader assets = AssetLoader.get();
        if (assets.byteIdle != null) {
            game.batch.begin();
            TextureRegion frame = assets.byteIdle.getKeyFrame(stateTime, true);
            // Byte agrandado 5 veces (aprox 600px)
            game.batch.draw(frame, cx - 300, byteY, 600, 600);
            game.batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            // Cuerpo robot cuadrado
            sr.setColor(new Color(0.3f, 0.7f, 1.0f, 1f));
            sr.rect(cx - 55, byteY, 110, 90);
            // ... resto del dibujo manual ...
            sr.setColor(new Color(0.2f, 0.55f, 0.85f, 1f));
            sr.rect(cx - 42, byteY + 90, 84, 70);
            sr.setColor(new Color(0.1f, 1f, 0.6f, 1f));
            sr.circle(cx - 18, byteY + 128, 14, 16);
            sr.circle(cx + 18, byteY + 128, 14, 16);
            sr.end();
        }
    }

    // ── HUD ──────────────────────────────────────────────────────

    private void drawHUD() {
        float progress = Math.min(runTimer / RUN_DURATION, 1f);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Barra progreso superior
        sr.setColor(0.15f, 0.15f, 0.30f, 0.85f);
        sr.rect(0, SH - 48, SW, 48);
        sr.setColor(0.25f, 0.75f, 1f, 1f);
        sr.rect(0, SH - 48, SW * progress, 48);

        // Panel de la ecuación (debajo de la barra de progreso)
        sr.setColor(0.08f, 0.10f, 0.22f, 0.85f);
        sr.rect(0, SH - 48 - 90, SW, 90);
        sr.end();

        game.batch.begin();

        // Texto barra
        fontSmall.setColor(Color.WHITE);
        fontSmall.draw(game.batch, "CORRIENDO...", 20, SH - 14);

        // Ecuación objetivo (modo composición/descomposición)
        String eqLabel = equationTarget.decompose ? "DESCOMPON:" : "ARMA:";
        fontSmall.setColor(reactionComplete ? new Color(0.4f, 1f, 0.5f, 1f) : Color.WHITE);
        drawC(fontSmall, eqLabel + "  " + equationTarget.equationString(), SW / 2f, SH - 48 - 34);

        // Progreso por átomo objetivo (ej. "H 4/6   O 1/3")
        StringBuilder progressStr = new StringBuilder();
        for (int i = 0; i < equationTarget.atoms.length; i++) {
            AtomType at = equationTarget.atoms[i];
            int have = collected.getOrDefault(at, 0);
            int need = equationTarget.neededCounts[i];
            if (i > 0) progressStr.append("   ");
            progressStr.append(at.getSymbol()).append(" ").append(Math.min(have, need)).append("/").append(need);
        }
        fontSmall.setColor(new Color(0.75f, 0.85f, 1f, 1f));
        drawC(fontSmall, progressStr.toString(), SW / 2f, SH - 48 - 68);

        game.batch.end();
    }

    // ── D-PAD IZQUIERDO ─────────────────────────────────────────

    private void drawDpad() {
        float cx = dpadCX, cy = dpadCY;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(COL_DPAD_BG);
        sr.circle(cx, cy, DPAD_R + DPAD_BR + 10, 40);

        drawBtn(cx, cy + DPAD_R, btnUp);
        drawBtn(cx, cy - DPAD_R, btnDown);
        drawBtn(cx - DPAD_R, cy, btnLeft);
        drawBtn(cx + DPAD_R, cy, btnRight);
        sr.end();

        game.batch.begin();
        fontBig.setColor(Color.WHITE);
        drawC(fontBig, "^", cx,          cy + DPAD_R + 16);
        drawC(fontBig, "v", cx,          cy - DPAD_R + 16);
        drawC(fontBig, "<", cx - DPAD_R, cy + 16);
        drawC(fontBig, ">", cx + DPAD_R, cy + 16);
        game.batch.end();
    }

    private void drawBtn(float cx, float cy, boolean pressed) {
        sr.setColor(pressed ? COL_DPAD_PR : COL_DPAD_N);
        sr.circle(cx, cy, DPAD_BR, 24);
    }

    // ── Textos flotantes ─────────────────────────────────────────

    private void drawFloatTexts(float delta) {
        for (int i = floatLife.size() - 1; i >= 0; i--) {
            float t = floatLife.get(i) - delta;
            floatPos.get(i)[1] += 90f * delta;
            if (t <= 0) {
                floatLabels.remove(i); floatPos.remove(i); floatLife.remove(i);
            } else {
                floatLife.set(i, t);
            }
        }
        if (floatLabels.isEmpty()) return;
        game.batch.begin();
        for (int i = 0; i < floatLabels.size(); i++) {
            float alpha = Math.min(floatLife.get(i) * 2f, 1f);
            font.setColor(1f, 1f, 0.3f, alpha);
            float[] p = floatPos.get(i);
            font.draw(game.batch, floatLabels.get(i), p[0], p[1]);
        }
        game.batch.end();
    }

    // ── CONFETI ──────────────────────────────────────────────────

    private void launchConfetti() {
        if (confettiLaunched) return;
        confettiLaunched = true;
        Color[] cols = { Color.RED, Color.YELLOW, Color.GREEN,
            Color.CYAN, Color.MAGENTA, Color.ORANGE };
        for (int i = 0; i < 80; i++) {
            confetti.add(new Confetti(
                random.nextFloat() * SW,
                SH + random.nextFloat() * 100,
                cols[random.nextInt(cols.length)]));
        }
    }

    private void updateConfetti(float delta) {
        for (Confetti c : confetti) {
            c.x   += c.vx * delta;
            c.y   -= c.vy * delta;
            c.vy  -= 180f * delta;
            c.rot += c.rotV * delta;
            c.life -= delta;
        }
        confetti.removeIf(c -> c.life <= 0 || c.y < -20);
    }

    private void drawConfetti() {
        if (confetti.isEmpty()) return;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Confetti c : confetti) {
            float alpha = Math.min(c.life, 1f);
            sr.setColor(c.color.r, c.color.g, c.color.b, alpha);
            sr.rect(c.x, c.y, c.size, c.size * 0.5f);
        }
        sr.end();
    }

    // ── SUMMARY (Byte presenta resultados) ───────────────────────

    private void drawSummary(float delta) {
        summaryTimer -= delta;
        byteFloat += delta * 2.5f;

        float cx = SW / 2f;
        float cy = SH / 2f;
        float byteY = cy - 250 + (float)Math.sin(byteFloat) * 15f;

        // Panel fondo agrandado
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.06f, 0.12f, 0.26f, 0.98f);
        sr.rect(10, 10, SW - 20, SH - 20); // Casi pantalla completa
        sr.setColor(0.25f, 0.75f, 1f, 1f);
        sr.rect(10, SH - 30, SW - 20, 10);
        sr.end();

        // Byte a la izquierda (gigante)
        drawByte(cx - (SW * 0.25f), byteY);

        // Textos
        game.batch.begin();
        float textX = cx + (SW * 0.15f);

        fontBig.setColor(new Color(1f, 0.9f, 0.3f, 1f));
        drawC(fontBig, "BYTE dice:", textX, cy + 280);

        font.setColor(reactionComplete ? new Color(0.5f, 1f, 0.8f, 1f) : new Color(1f, 0.7f, 0.4f, 1f));
        drawC(font, reactionComplete
                ? "¡Ecuacion balanceada! +25 monedas"
                : "Ecuacion incompleta: " + equationTarget.equationString(),
            textX, cy + 180);

        // Lista de átomos
        float iy = cy + 60;
        if (collected.isEmpty()) {
            font.setColor(new Color(0.6f, 0.6f, 0.8f, 1f));
            drawC(font, "Ninguna esta vez...", textX, iy);
        } else {
            for (Map.Entry<AtomType, Integer> e : collected.entrySet()) {
                game.batch.end();
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(e.getKey().getColor());
                sr.circle(cx + (SW * 0.05f), iy - 15, 35, 24);
                sr.end();
                game.batch.begin();
                font.setColor(Color.WHITE);
                font.draw(game.batch,
                    e.getKey().getSymbol() + "  x" + e.getValue(),
                    cx + (SW * 0.12f), iy);
                iy -= 90;
            }
        }

        // Barra cuenta regresiva
        game.batch.end();
        float prog = Math.max(summaryTimer / 4f, 0f);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.2f, 0.35f, 1f);
        sr.rect(cx - 180, cy - 220, 360, 16);
        sr.setColor(0.25f, 0.75f, 1f, 1f);
        sr.rect(cx - 180, cy - 220, 360 * prog, 16);
        sr.end();

        game.batch.begin();
        fontSmall.setColor(new Color(0.7f, 0.8f, 1f, 1f));
        drawC(fontSmall, "Comenzando nivel...", cx, cy - 238);
        game.batch.end();

        if (summaryTimer <= 0) {
            game.setScreen(new Match3Screen(game));
        }
    }

    // ── Utilidades 3D ────────────────────────────────────────────

    /**
     * Convierte posición 3D (carril, z) a posición de pantalla.
     * z=1 → horizonte (pequeño), z=0 → pantalla (grande)
     * Retorna {screenX, screenY, scale}
     */
    private float[] screenPos(int laneIdx, float z) {
        float hY   = SH * 0.5f; // Horizonte un poco más bajo
        float vx   = SW * VANISH_X;

        float t = 1f - z; // t=0 lejos, t=1 cerca

        float nearX0 = SW * 0.15f;
        float nearX2 = SW * 0.85f;

        float laneNearX = nearX0 + (nearX2 - nearX0) * (laneIdx / 2f);
        float sx = lerp(vx, laneNearX, t);
        float sy = lerp(hY, -100f, t); // Que los objetos salgan de la pantalla por abajo
        float scale = lerp(0.05f, 1.2f, t * t);

        return new float[]{ sx, sy, scale };
    }

    private float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private float easeInOut(float t) { return t * t * (3 - 2 * t); }

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
        font.dispose();
        fontBig.dispose();
        fontSmall.dispose();
    }
}
