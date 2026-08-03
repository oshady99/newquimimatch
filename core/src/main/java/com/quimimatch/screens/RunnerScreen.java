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
import com.quimimatch.managers.Compound;
import com.quimimatch.managers.CompoundDatabase;
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
import com.quimimatch.managers.RunnerDifficulty;


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

    //particulas class
    private static class RunnerParticle {

        float x;
        float y;
        float vx;
        float vy;

        float life;
        float maxLife;

        float size;

        Color color;

        RunnerParticle(
            float x,
            float y,
            float vx,
            float vy,
            float life,
            float size,
            Color color) {

            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;

            this.life = life;
            this.maxLife = life;

            this.size = size;
            this.color = color.cpy();
        }
    }

    private List<Confetti> confetti = new ArrayList<>();

    private final List<RunnerParticle> runnerParticles =
        new ArrayList<>();
    private boolean confettiLaunched = false;

    // ── Ecuación química objetivo (Fase 1 — La Fuga Nuclear) ─────
    // repeatFactor controla cuántos "ciclos" de la proporción balanceada
    // hay que juntar durante los 14s del runner.
    private static final int RUNNER_REPEAT_FACTOR = 3;
    private EquationUtil.RunnerTarget equationTarget;
    private ReactionManager reactionManager;
    private RunnerDifficulty difficulty;
    private Compound specialCompound;
    private MissionIntroOverlay missionIntro;
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
    private enum State {INTRO, RUNNING, SUMMARY }
    private State state = State.INTRO;
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

        sr = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(2.8f);

        fontBig = new BitmapFont();
        fontBig.getData().setScale(4.5f);

        fontSmall = new BitmapFont();
        fontSmall.getData().setScale(2.2f);

        layout = new GlyphLayout();

        AudioManager.get().startWorldMusic(
            GameSession.get().getConfig().world
        );

        int levelIdx = GameSession.get().getCurrentLevel();
        boolean decomposeMode = levelIdx % 2 == 1;

        specialCompound = CompoundDatabase.getForLevel(
            GameSession.get().getCurrentWorld(),
            GameSession.get().getCurrentLevel()
        );

        if (specialCompound != null) {
            equationTarget = EquationUtil.fromCompound(
                specialCompound,
                decomposeMode,
                1
            );
        } else {
            equationTarget = EquationUtil.fromLevelConfig(
                GameSession.get().getConfig(),
                decomposeMode,
                RUNNER_REPEAT_FACTOR
            );
        }
        difficulty = RunnerDifficulty.fromTarget(
            equationTarget
        );

        reactionManager = new ReactionManager(
            equationTarget
        );

        missionIntro = new MissionIntroOverlay(
            specialCompound
        );

        reactionManager = new ReactionManager(equationTarget);
        missionIntro = new MissionIntroOverlay(specialCompound);

        dpadCX = 220f;
        dpadCY = 200f;
    }
    @Override public void show() {}

    @Override
    public void render(float delta) {
        stateTime += delta;

        Gdx.gl.glClearColor(0.05f, 0.08f, 0.22f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state == State.INTRO) {
            drawScene();

            missionIntro.update(delta);

            missionIntro.draw(
                game.batch,
                sr,
                font,
                fontBig,
                fontSmall,
                layout,
                SW,
                SH
            );

            if (missionIntro.isFinished()) {
                state = State.RUNNING;
            }

        } else if (state == State.RUNNING) {
            updateRunner(delta);
            drawScene();
            updateAndDrawRunnerParticles(delta);
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
        reactionManager.update(delta);

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

                    ReactionManager.CollectionResult result =
                        reactionManager.collectAtom(o.atomType);

                    // Sincronizar el mapa que usa el HUD.
                    collected.clear();
                    collected.putAll(reactionManager.getCollected());

                    float[] sp = screenPos(o.lane, o.z);

                    switch (result.getType()) {

                        case CORRECT:
                            AudioManager.get().playCollect();
                            spawnRunnerParticles(
                                sp[0],
                                sp[1],
                                getCpkColor(o.atomType),
                                false
                            );

                            floatLabels.add(
                                "+" + o.atomType.getSymbol()
                            );

                            floatPos.add(
                                new float[]{
                                    sp[0],
                                    sp[1] + 140f
                                }
                            );
                            floatLife.add(1.0f);
                            break;

                        case COMPLETED:
                            AudioManager.get().playCollect();
                            spawnRunnerParticles(
                                sp[0],
                                sp[1],
                                getCpkColor(o.atomType),
                                false
                            );

                            reactionComplete = true;
                            reactionCelebrateT = 2.5f;

                            floatLabels.add(
                                "¡MOLECULA COMPLETADA!"
                            );

                            floatPos.add(
                                new float[]{
                                    SW / 2f - 220f,
                                    SH * 0.65f
                                }
                            );

                            floatLife.add(2.0f);

                            PlayerInventory.get().addCoins(25);
                            break;

                        case EXTRA:
                            AudioManager.get().playObstacle();
                            spawnRunnerParticles(
                                sp[0],
                                sp[1],
                                new Color(
                                    1f,
                                    0.60f,
                                    0.10f,
                                    1f
                                ),
                                true
                            );

                            floatLabels.add(
                                "¡ATOMO SOBRANTE! -5%"
                            );

                            floatPos.add(
                                new float[]{sp[0], sp[1]}
                            );

                            floatLife.add(1.2f);
                            break;

                        case INCORRECT:
                            AudioManager.get().playObstacle();
                            spawnRunnerParticles(
                                sp[0],
                                sp[1],
                                new Color(
                                    0.35f,
                                    1f,
                                    0.25f,
                                    1f
                                ),
                                true
                            );

                            floatLabels.add(
                                "¡CONTAMINANTE! -15%"
                            );

                            floatPos.add(
                                new float[]{sp[0], sp[1]}
                            );

                            floatLife.add(1.2f);
                            break;

                        case CONTAMINATED:
                            AudioManager.get().playObstacle();

                            floatLabels.add(
                                "¡REACCION CONTAMINADA!"
                            );

                            floatPos.add(
                                new float[]{
                                    SW / 2f - 220f,
                                    SH * 0.65f
                                }
                            );

                            floatLife.add(2.0f);
                            break;
                    }

                    it.remove();

                } else {

                    // Obstáculo — verificar si el jugador esquivó.
                    boolean evaded =
                        (o.isHigh && crouching)
                            || (!o.isHigh && jumping);

                    o.hit = true;

                    if (!evaded) {
                        AudioManager.get().playObstacle();
                        GameSession.get().loseLife();

                        floatLabels.add("¡VIDA -1!");

                        float[] sp = screenPos(o.lane, o.z);

                        floatPos.add(
                            new float[]{
                                sp[0],
                                sp[1] + 60
                            }
                        );

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

    private void spawnObject() {
        int selectedLane = random.nextInt(3);
        float depth = 0.95f;

        // 70 % de probabilidad de generar un átomo.
        if (random.nextFloat() < 0.70f) {

            AtomType selectedAtom;

            // 75 % átomo necesario y 25 % contaminante.
            if (random.nextFloat() < 0.75f) {

                AtomType[] targetAtoms = equationTarget.atoms;

                selectedAtom = targetAtoms[
                    random.nextInt(targetAtoms.length)
                    ];

            } else {

                selectedAtom = getRandomContaminant();
            }

            objects.add(
                new Obj3D(
                    depth,
                    selectedLane,
                    selectedAtom
                )
            );

        } else {

            // 30 % de probabilidad de obstáculo.
            objects.add(
                new Obj3D(
                    depth,
                    selectedLane,
                    random.nextBoolean()
                )
            );
        }
    }

    private AtomType getRandomContaminant() {
        List<AtomType> contaminants = new ArrayList<>();

        for (AtomType atom : AtomType.values()) {

            if (!reactionManager.isTargetAtom(atom)) {
                contaminants.add(atom);
            }
        }

        // Respaldo de seguridad.
        if (contaminants.isEmpty()) {
            AtomType[] targetAtoms = equationTarget.atoms;

            return targetAtoms[
                random.nextInt(targetAtoms.length)
                ];
        }

        return contaminants.get(
            random.nextInt(contaminants.size())
        );
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
        float atomScale =
            Math.max(0.65f, scale * 2.0f);

        AssetLoader assets = AssetLoader.get();

        if (o.isAtom) {
            String sym = o.atomType.getSymbol();
            if (assets.hasAtom(sym)) {
                game.batch.begin();
                float size = 250f * atomScale;
                game.batch.draw(assets.getAtom(sym), cx - size / 2f, cy - size / 2f, size, size);
                game.batch.end();
            } else {
                sr.begin(ShapeRenderer.ShapeType.Filled);
                // Átomo colectable — círculo brillante
                float r = 105f * scale; // Reducido más (era 80)
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

        float remainingTime = Math.max(
            0f,
            RUN_DURATION - runTimer
        );

        int purity = reactionManager.getPurity();
        float purityProgress = reactionManager.getPurityProgress();

        // ── Panel holográfico ────────────────────────────────────────

        float panelWidth = SW * 0.68f;
        float panelHeight = 158f;

        float panelX = (SW - panelWidth) / 2f;
        float panelY = SH - panelHeight - 10f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Sombra muy ligera.
        sr.setColor(0f, 0f, 0f, 0.15f);
        sr.rect(
            panelX + 5f,
            panelY - 5f,
            panelWidth,
            panelHeight
        );

        // Panel transparente.
        sr.setColor(0.015f, 0.06f, 0.14f, 0.42f);
        sr.rect(
            panelX,
            panelY,
            panelWidth,
            panelHeight
        );

        // Bordes holográficos.
        sr.setColor(0.10f, 0.82f, 1f, 0.90f);

        sr.rect(
            panelX,
            panelY + panelHeight - 3f,
            panelWidth,
            3f
        );

        sr.rect(
            panelX,
            panelY,
            panelWidth,
            2f
        );

        // Separador antes de los contadores.
        sr.setColor(0.15f, 0.70f, 0.95f, 0.45f);

        sr.rect(
            panelX + 20f,
            panelY + 62f,
            panelWidth - 40f,
            2f
        );

        // ── Barra de pureza ──────────────────────────────────────────

        float purityBarX = panelX + 115f;
        float purityBarY = panelY + 12f;
        float purityBarWidth = panelWidth - 230f;
        float purityBarHeight = 12f;

        // Fondo de la barra.
        sr.setColor(0.10f, 0.13f, 0.20f, 0.90f);
        sr.rect(
            purityBarX,
            purityBarY,
            purityBarWidth,
            purityBarHeight
        );

        Color purityColor;

        if (purity >= 75) {
            purityColor = new Color(
                0.20f,
                0.90f,
                0.55f,
                1f
            );
        } else if (purity >= 50) {
            purityColor = new Color(
                1f,
                0.80f,
                0.15f,
                1f
            );
        } else if (purity >= 25) {
            purityColor = new Color(
                1f,
                0.45f,
                0.10f,
                1f
            );
        } else {
            purityColor = new Color(
                1f,
                0.15f,
                0.15f,
                1f
            );
        }

        sr.setColor(purityColor);

        sr.rect(
            purityBarX,
            purityBarY,
            purityBarWidth * purityProgress,
            purityBarHeight
        );

        sr.end();

        // ── Escalas del HUD ──────────────────────────────────────────

        float originalFontScale =
            font.getData().scaleX;

        float originalSmallScale =
            fontSmall.getData().scaleX;

        // Letras más grandes.
        font.getData().setScale(1.80f);
        fontSmall.getData().setScale(1.28f);

        game.batch.begin();

        float centerX = SW / 2f;

        String theme;
        String compoundName;
        String formula;

        if (specialCompound != null) {

            theme = specialCompound
                .getTheme()
                .toUpperCase();

            compoundName = specialCompound
                .getName()
                .toUpperCase();

            formula = specialCompound.getFormula();

        } else {

            theme = "EXPEDICION QUIMICA";

            compoundName =
                equationTarget.moleculeName.toUpperCase();

            formula =
                equationTarget.equationString();
        }

        // Tema cotidiano.
        fontSmall.setColor(
            new Color(1f, 0.79f, 0.22f, 1f)
        );

        drawC(
            fontSmall,
            theme,
            centerX,
            panelY + 137f
        );

        // Nombre del compuesto.
        font.setColor(Color.WHITE);

        drawC(
            font,
            compoundName,
            centerX,
            panelY + 108f
        );

        // Fórmula molecular.
        fontSmall.setColor(
            reactionComplete
                ? new Color(0.35f, 1f, 0.55f, 1f)
                : new Color(0.20f, 0.90f, 1f, 1f)
        );

        drawC(
            fontSmall,
            formula,
            centerX,
            panelY + 80f
        );

        // Tiempo.
        fontSmall.setColor(
            remainingTime <= 4f
                ? new Color(1f, 0.30f, 0.20f, 1f)
                : Color.WHITE
        );

        String timerText =
            String.format(
                java.util.Locale.US,
                "%02d s",
                (int) Math.ceil(remainingTime)
            );

        layout.setText(
            fontSmall,
            timerText
        );

        fontSmall.draw(
            game.batch,
            timerText,
            panelX + panelWidth
                - layout.width
                - 14f,
            panelY + panelHeight - 12f
        );

        game.batch.end();

        // ── Contadores de átomos ─────────────────────────────────────

        int atomCount =
            equationTarget.atoms.length;

        float countersLeft =
            panelX + 30f;

        float countersWidth =
            panelWidth - 60f;

        float itemWidth =
            countersWidth / atomCount;

        float counterY =
            panelY + 45f;

        for (int i = 0; i < atomCount; i++) {

            AtomType atom =
                equationTarget.atoms[i];

            int have =
                collected.getOrDefault(atom, 0);

            int need =
                equationTarget.neededCounts[i];

            float itemCenterX =
                countersLeft
                    + itemWidth * i
                    + itemWidth / 2f;

            Color atomColor =
                getCpkColor(atom);

            // Círculo CPK más grande.
            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(
                0f,
                0f,
                0f,
                0.38f
            );

            sr.circle(
                itemCenterX - 38f + 2f,
                counterY - 2f,
                12f,
                18
            );

            sr.setColor(atomColor);

            sr.circle(
                itemCenterX - 38f,
                counterY,
                11f,
                18
            );

            sr.end();

            game.batch.begin();

            fontSmall.setColor(Color.WHITE);

            String counterText =
                atom.getSymbol()
                    + " "
                    + Math.min(have, need)
                    + "/"
                    + need;

            layout.setText(
                fontSmall,
                counterText
            );

            fontSmall.draw(
                game.batch,
                counterText,
                itemCenterX
                    - layout.width / 2f
                    + 10f,
                counterY + 10f
            );

            game.batch.end();
        }

        // ── Texto de pureza ──────────────────────────────────────────

        game.batch.begin();

        fontSmall.setColor(purityColor);

        String purityText =
            "PUREZA  " + purity + "%";

        layout.setText(
            fontSmall,
            purityText
        );

        fontSmall.draw(
            game.batch,
            purityText,
            panelX + 15f,
            panelY + 24f
        );

        game.batch.end();
        drawByteMessage(panelX, panelY, panelWidth);

        // Restaurar escalas originales.
        font.getData().setScale(
            originalFontScale
        );

        fontSmall.getData().setScale(
            originalSmallScale
        );

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    //drawByteMessage()

    private void drawByteMessage(
        float hudPanelX,
        float hudPanelY,
        float hudPanelWidth
    ) {
        if (!reactionManager.shouldShowByteMessage()) {
            return;
        }

        // ── Medidas del comunicador ──────────────────────────────────

        float boxWidth = Math.min(430f, SW * 0.32f);
        float boxHeight = 125f;

        // Esquina superior derecha, debajo del HUD.
        float boxX =
            hudPanelX + hudPanelWidth - boxWidth;

        float boxY =
            hudPanelY - boxHeight - 12f;

        // ── Fondo holográfico ────────────────────────────────────────

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Sombra.
        sr.setColor(0f, 0f, 0f, 0.25f);
        sr.rect(
            boxX + 5f,
            boxY - 5f,
            boxWidth,
            boxHeight
        );

        // Panel transparente.
        sr.setColor(
            0.02f,
            0.07f,
            0.16f,
            0.62f
        );

        sr.rect(
            boxX,
            boxY,
            boxWidth,
            boxHeight
        );

        // Línea holográfica superior.
        sr.setColor(
            0.12f,
            0.85f,
            1f,
            0.95f
        );

        sr.rect(
            boxX,
            boxY + boxHeight - 4f,
            boxWidth,
            4f
        );

        // Indicador circular de Byte.
        float indicatorX = boxX + 28f;
        float indicatorY = boxY + boxHeight - 28f;

        sr.setColor(
            0f,
            0f,
            0f,
            0.35f
        );

        sr.circle(
            indicatorX + 2f,
            indicatorY - 2f,
            13f,
            18
        );

        sr.setColor(
            0.20f,
            0.95f,
            1f,
            1f
        );

        sr.circle(
            indicatorX,
            indicatorY,
            12f,
            18
        );

        sr.end();

        // ── Texto ────────────────────────────────────────────────────

        float originalSmallScale =
            fontSmall.getData().scaleX;

        // Tamaño visible, pero suficientemente compacto para envolver texto.
        fontSmall.getData().setScale(1.45f);

        String message =
            reactionManager.getByteMessage();

        if (message == null) {
            message = "";
        }

        // ReactionManager ya agrega "Byte:"; lo quitamos para no repetirlo.
        if (message.startsWith("Byte: ")) {
            message = message.substring(6);
        }

        game.batch.begin();

        // Nombre del asistente.
        fontSmall.setColor(
            new Color(
                0.25f,
                0.95f,
                1f,
                1f
            )
        );

        fontSmall.draw(
            game.batch,
            "BYTE",
            boxX + 50f,
            boxY + boxHeight - 18f
        );

        // Mensaje con ajuste automático de línea.
        fontSmall.setColor(Color.WHITE);

        fontSmall.draw(
            game.batch,
            message,
            boxX + 18f,
            boxY + 72f,
            boxWidth - 36f,
            com.badlogic.gdx.utils.Align.left,
            true
        );

        game.batch.end();

        fontSmall.getData().setScale(
            originalSmallScale
        );
    }

    //getCPKColor
    private Color getCpkColor(AtomType atom) {

        switch (atom) {

            case H:
                return new Color(
                    0.95f,
                    0.95f,
                    0.95f,
                    1f
                );

            case C:
                return new Color(
                    0.22f,
                    0.22f,
                    0.22f,
                    1f
                );

            case N:
                return new Color(
                    0.15f,
                    0.35f,
                    0.95f,
                    1f
                );

            case O:
                return new Color(
                    0.95f,
                    0.15f,
                    0.15f,
                    1f
                );

            case S:
                return new Color(
                    1f,
                    0.90f,
                    0.10f,
                    1f
                );

            case P:
                return new Color(
                    1f,
                    0.45f,
                    0.10f,
                    1f
                );

            case Cl:
                return new Color(
                    0.20f,
                    0.85f,
                    0.25f,
                    1f
                );

            case Na:
                return new Color(
                    0.55f,
                    0.20f,
                    0.85f,
                    1f
                );

            case Ca:
                return new Color(
                    0.45f,
                    0.85f,
                    0.35f,
                    1f
                );

            case Mg:
                return new Color(
                    0.10f,
                    0.75f,
                    0.45f,
                    1f
                );

            case Fe:
                return new Color(
                    0.65f,
                    0.30f,
                    0.15f,
                    1f
                );

            case Cu:
                return new Color(
                    0.72f,
                    0.45f,
                    0.22f,
                    1f
                );

            case Zn:
                return new Color(
                    0.55f,
                    0.65f,
                    0.72f,
                    1f
                );

            case K:
                return new Color(
                    0.55f,
                    0.20f,
                    0.75f,
                    1f
                );

            default:
                return Color.WHITE;
        }
    }

    // ── D-PAD IZQUIERDO ─────────────────────────────────────────

    private void drawDpad() {
        float cx = dpadCX;
        float cy = dpadCY;
        float separation = 105f;

        drawNeonDpadButton(
            cx,
            cy + separation,
            btnUp,
            "^"
        );

        drawNeonDpadButton(
            cx,
            cy - separation,
            btnDown,
            "v"
        );

        drawNeonDpadButton(
            cx - separation,
            cy,
            btnLeft,
            "<"
        );

        drawNeonDpadButton(
            cx + separation,
            cy,
            btnRight,
            ">"
        );
    } // Esta llave cierra drawDpad()


    private void drawNeonDpadButton(
        float x,
        float y,
        boolean pressed,
        String symbol
    ) {
        float outerRadius = 49f;
        float middleRadius = 43f;
        float innerRadius = 36f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Resplandor exterior.
        sr.setColor(
            0.05f,
            0.65f,
            1f,
            pressed ? 0.55f : 0.28f
        );
        sr.circle(x, y, outerRadius, 32);

        // Fondo individual transparente.
        sr.setColor(
            0.015f,
            0.055f,
            0.14f,
            pressed ? 0.92f : 0.68f
        );
        sr.circle(x, y, middleRadius, 32);

        // Centro.
        if (pressed) {
            sr.setColor(0.12f, 0.50f, 0.95f, 0.90f);
        } else {
            sr.setColor(0.03f, 0.10f, 0.24f, 0.72f);
        }

        sr.circle(x, y, innerRadius, 32);
        sr.end();

        // Bordes de neón.
        sr.begin(ShapeRenderer.ShapeType.Line);

        if (pressed) {
            sr.setColor(0.40f, 0.95f, 1f, 1f);
        } else {
            sr.setColor(0.05f, 0.78f, 1f, 0.95f);
        }

        sr.circle(x, y, middleRadius, 32);
        sr.circle(x, y, innerRadius, 32);

        sr.end();

        // Símbolo.
        float originalScale = fontBig.getData().scaleX;

        fontBig.getData().setScale(
            pressed ? 2.15f : 1.90f
        );

        game.batch.begin();

        fontBig.setColor(Color.WHITE);
        drawC(
            fontBig,
            symbol,
            x,
            y + 17f
        );

        game.batch.end();

        fontBig.getData().setScale(originalScale);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── Textos flotantes ─────────────────────────────────────────

    private void drawFloatTexts(float delta) {
        for (int i = floatLife.size() - 1; i >= 0; i--) {
            float t = floatLife.get(i) - delta;
            floatPos.get(i)[1] += 140f * delta;
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

    private void spawnRunnerParticles(
        float x,
        float y,
        Color color,
        boolean contaminant
    ) {
        int particleCount =
            contaminant ? 28 : 24;

        for (int i = 0; i < particleCount; i++) {

            float angle =
                random.nextFloat()
                    * (float) Math.PI
                    * 2f;

            float speed =
                contaminant
                    ? 160f + random.nextFloat() * 230f
                    : 150f + random.nextFloat() * 210f;

            float vx =
                (float) Math.cos(angle) * speed;

            float vy =
                (float) Math.sin(angle) * speed;

            if (contaminant) {
                vy += 50f;
            }

            float life =
                contaminant
                     ?0.65f + random.nextFloat() * 0.45f
                     :0.75f + random.nextFloat() * 0.50f;

            float size =
                contaminant
                    ? 10f + random.nextFloat() * 13f
                    : 8f + random.nextFloat() * 11f;

            runnerParticles.add(
                new RunnerParticle(
                    x,
                    y,
                    vx,
                    vy,
                    life,
                    size,
                    color
                )
            );
        }
    }

    private void updateAndDrawRunnerParticles(float delta) {

        for (int i = runnerParticles.size() - 1; i >= 0; i--) {

            RunnerParticle particle =
                runnerParticles.get(i);

            particle.life -= delta;

            if (particle.life <= 0f) {
                runnerParticles.remove(i);
                continue;
            }

            particle.x += particle.vx * delta;
            particle.y += particle.vy * delta;

            particle.vy -= 220f * delta;

            particle.vx *= 0.97f;
        }

        if (runnerParticles.isEmpty()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        sr.begin(ShapeRenderer.ShapeType.Filled);

        for (RunnerParticle particle : runnerParticles) {

            float alpha =
                Math.max(
                    0f,
                    particle.life / particle.maxLife
                );

            sr.setColor(
                particle.color.r,
                particle.color.g,
                particle.color.b,
                alpha
            );

            sr.circle(
                particle.x,
                particle.y,
                Math.max(3f, particle.size * alpha),
                12
            );
        }

        sr.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
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
    @Override public void pause()  { AudioManager.get().pauseMusic();}
    @Override public void resume() { AudioManager.get().resumeMusic();}
    @Override public void hide()   { AudioManager.get().pauseMusic();}

    @Override
    public void dispose() {
        sr.dispose();
        font.dispose();
        fontBig.dispose();
        fontSmall.dispose();
    }
}
