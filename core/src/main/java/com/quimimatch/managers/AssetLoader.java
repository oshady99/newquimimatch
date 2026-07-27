package com.quimimatch.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Carga y gestiona todos los assets gráficos del juego.
 * Usa fallback a ShapeRenderer si el asset no existe todavía.
 */
public class AssetLoader implements Disposable {

    private static AssetLoader instance;
    public static AssetLoader get() {
        if (instance == null) instance = new AssetLoader();
        return instance;
    }

    private final Map<String, Texture> textures = new HashMap<>();

    // ── Animaciones de Max ───────────────────────────────────────
    public Animation<TextureRegion> maxRun;
    public Animation<TextureRegion> maxJump;
    public Animation<TextureRegion> maxCrouch;
    public Texture maxIdle;
    public Texture maxThumbsUp;

    // ── Animaciones de Byte ──────────────────────────────────────
    public Animation<TextureRegion> byteIdle;
    public Texture byteTalk;
    public Texture byteHappy;

    // ── Átomos ──────────────────────────────────────────────────
    private final Map<String, Texture> atomTextures = new HashMap<>();

    // ── Fondos de tablero ────────────────────────────────────────
    private final Texture[] labBackgrounds = new Texture[7];

    // ── Fondos del runner ────────────────────────────────────────
    private final Texture[] runnerBackgrounds = new Texture[7];

    // ── Obstáculos del runner ────────────────────────────────────
    public Texture obsLow;
    public Texture obsHigh;
    public Texture obsMolecule;

    // ── UI ───────────────────────────────────────────────────────
    public Texture logo;
    public Texture starOn;
    public Texture starOff;
    public Texture heartOn;
    public Texture heartOff;
    public Texture coin;
    public Texture btnPlay;
    public Texture btnShop;
    public Texture btnEncyc;

    // Átomos disponibles
    private static final String[] ATOM_KEYS = {
        "H","O","C","N","Na","Cl","S","Fe","Mg","Ca","Cu","Zn","K","P"
    };

    private AssetLoader() {}

    /**
     * Carga todos los assets disponibles.
     * Los que no existen quedan como null (se usa fallback ShapeRenderer).
     */
    public void loadAll() {
        // Átomos
        for (String sym : ATOM_KEYS) {
            loadAtom(sym);
        }

        // Fichas especiales
        loadSafe("special_line_h",  "atoms/atom_special_line_h.png");
        loadSafe("special_line_v",  "atoms/atom_special_line_v.png");
        loadSafe("special_area",    "atoms/atom_special_area.png");
        loadSafe("special_wild",    "atoms/atom_special_wild.png");

        // Fondos de laboratorio
        for (int i = 0; i < 7; i++) {
            String path = "backgrounds/bg_lab" + (i + 1) + ".png";
            if (fileExists(path)) {
                labBackgrounds[i] = loadTexture(path);
            }
        }

        // Fondos del runner
        for (int i = 0; i < 7; i++) {
            String path = "backgrounds/runner_bg" + (i + 1) + ".png";
            if (fileExists(path)) {
                runnerBackgrounds[i] = loadTexture(path);
            }
        }

        // Max animaciones
        maxRun    = loadAnimation("characters/max_run", 4, 0.12f);
        maxJump   = loadAnimation("characters/max_jump", 1, 0.1f);
        maxCrouch = loadAnimation("characters/max_crouch", 1, 0.1f);
        maxIdle   = loadSafe("max_idle", "characters/max_idle.png");
        maxThumbsUp = loadSafe("max_thumbsup", "characters/max_thumbsup.png");

        // Byte animaciones
        byteIdle  = loadAnimation("characters/byte_idle", 2, 0.4f);
        byteTalk  = loadSafe("byte_talk",  "characters/byte_talk.png");
        byteHappy = loadSafe("byte_happy", "characters/byte_happy.png");

        // Obstáculos runner
        obsLow      = loadSafe("obs_low",      "runner/obs_low.png");
        obsHigh     = loadSafe("obs_high",     "runner/obs_high.png");
        obsMolecule = loadSafe("obs_molecule", "runner/obs_molecule.png");

        // UI
        logo      = loadSafe("logo",      "ui/logo.png");
        starOn    = loadSafe("star_on",   "ui/star_on.png");
        starOff   = loadSafe("star_off",  "ui/star_off.png");
        heartOn   = loadSafe("heart_on",  "ui/heart_on.png");
        heartOff  = loadSafe("heart_off", "ui/heart_off.png");
        coin      = loadSafe("coin",      "ui/coin.png");
        btnPlay   = loadSafe("btn_play",  "ui/btn_play.png");
        btnShop   = loadSafe("btn_shop",  "ui/btn_shop.png");
        btnEncyc  = loadSafe("btn_encyc", "ui/btn_encyc.png");
    }

    // ── Getters con fallback null ────────────────────────────────

    public Texture getAtom(String symbol) {
        return atomTextures.get(symbol);
    }

    public boolean hasAtom(String symbol) {
        return atomTextures.containsKey(symbol) && atomTextures.get(symbol) != null;
    }

    public Texture getLabBackground(int world) {
        int idx = Math.max(0, Math.min(world - 1, 6));
        return labBackgrounds[idx];
    }

    public boolean hasLabBackground(int world) {
        int idx = Math.max(0, Math.min(world - 1, 6));
        return labBackgrounds[idx] != null;
    }

    public Texture getRunnerBackground(int world) {
        int idx = Math.max(0, Math.min(world - 1, 6));
        return runnerBackgrounds[idx];
    }

    public boolean hasRunnerBackground(int world) {
        int idx = Math.max(0, Math.min(world - 1, 6));
        return runnerBackgrounds[idx] != null;
    }

    public Texture getSpecial(String type) {
        return textures.get("special_" + type);
    }

    public boolean hasMaxRun()    { return maxRun    != null; }
    public boolean hasByteIdle()  { return byteIdle  != null; }
    public boolean hasLogo()      { return logo      != null; }
    public boolean hasStars()     { return starOn    != null; }
    public boolean hasHearts()    { return heartOn   != null; }

    // ── Carga interna ────────────────────────────────────────────

    private void loadAtom(String symbol) {
        String path = "atoms/atom_" + symbol + ".png";
        if (fileExists(path)) {
            Texture t = loadTexture(path);
            atomTextures.put(symbol, t);
        }
    }

    private Texture loadSafe(String key, String path) {
        if (!fileExists(path)) return null;
        Texture t = loadTexture(path);
        textures.put(key, t);
        return t;
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        return t;
    }

    /**
     * Carga animación desde archivos numerados: prefix1.png, prefix2.png...
     */
    private Animation<TextureRegion> loadAnimation(String prefix, int frameCount, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        int loaded = 0;
        for (int i = 1; i <= frameCount; i++) {
            String path = prefix + i + ".png";
            if (fileExists(path)) {
                Texture t = loadTexture(path);
                frames[loaded] = new TextureRegion(t);
                loaded++;
            }
        }
        if (loaded == 0) return null;
        TextureRegion[] trimmed = new TextureRegion[loaded];
        System.arraycopy(frames, 0, trimmed, 0, loaded);
        return new Animation<>(frameDuration, trimmed);
    }

    private boolean fileExists(String path) {
        return Gdx.files.internal(path).exists();
    }

    @Override
    public void dispose() {
        for (Texture t : textures.values()) if (t != null) t.dispose();
        for (Texture t : atomTextures.values()) if (t != null) t.dispose();
        for (Texture t : labBackgrounds)  if (t != null) t.dispose();
        for (Texture t : runnerBackgrounds) if (t != null) t.dispose();
        textures.clear();
        atomTextures.clear();
    }
}
