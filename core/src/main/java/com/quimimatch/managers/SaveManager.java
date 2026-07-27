package com.quimimatch.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Sprint A — Persistencia completa del juego usando LibGDX Preferences.
 * Guarda y carga: estrellas, monedas, vidas, inventario, audio, skins.
 */
public class SaveManager {

    private static final String PREFS_NAME = "quimimatch_save";
    private static Preferences prefs;

    // ── Claves ───────────────────────────────────────────────────
    private static final String KEY_COINS        = "coins";
    private static final String KEY_LIVES        = "lives";
    private static final String KEY_MOVES_PUP    = "pup_moves";
    private static final String KEY_BOMBS_PUP    = "pup_bombs";
    private static final String KEY_WILDCARD_PUP = "pup_wildcards";
    private static final String KEY_ACTIVE_SKIN  = "active_skin";
    private static final String KEY_MUSIC_ON     = "music_enabled";
    private static final String KEY_SOUND_ON     = "sound_enabled";
    private static final String KEY_STARS_PREFIX = "stars_w";  // stars_w0_l0 ... stars_w5_l3
    private static final String KEY_SKIN_PREFIX  = "skin_";    // skin_neon, skin_pastel, etc.

    private static Preferences get() {
        if (prefs == null) prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs;
    }

    // ── GUARDAR ──────────────────────────────────────────────────

    /** Guarda todo el estado del juego de una vez */
    public static void saveAll() {
        saveInventory();
        saveStars();
        saveAudioSettings();
        get().flush();
    }

    public static void saveInventory() {
        PlayerInventory inv = PlayerInventory.get();
        Preferences p = get();
        p.putInteger(KEY_COINS,        inv.getCoins());
        p.putInteger(KEY_LIVES,        GameSession.get().getLives());
        p.putInteger(KEY_MOVES_PUP,    inv.getMoves());
        p.putInteger(KEY_BOMBS_PUP,    inv.getBombs());
        p.putInteger(KEY_WILDCARD_PUP, inv.getWildcards());
        p.putString (KEY_ACTIVE_SKIN,  inv.getActiveSkin());

        // Skins desbloqueados
        String[] skinIds = { "default", "neon", "pastel", "metallic", "galaxy" };
        for (String id : skinIds) {
            p.putBoolean(KEY_SKIN_PREFIX + id, inv.isSkinUnlocked(id));
        }
        p.flush();
    }

    public static void saveStars() {
        Preferences p = get();
        GameSession gs = GameSession.get();
        for (int w = 0; w < 6; w++) {
            for (int l = 0; l < 4; l++) {
                p.putInteger(KEY_STARS_PREFIX + w + "_l" + l, gs.getStars(w, l));
            }
        }
        p.flush();
    }

    public static void saveAudioSettings() {
        Preferences p = get();
        p.putBoolean(KEY_MUSIC_ON, AudioManager.get().isMusicEnabled());
        p.putBoolean(KEY_SOUND_ON, AudioManager.get().isSoundEnabled());
        p.flush();
    }

    // ── CARGAR ───────────────────────────────────────────────────

    /** Carga todo al iniciar el juego */
    public static void loadAll() {
        loadInventory();
        loadStars();
        loadAudioSettings();
    }

    public static void loadInventory() {
        Preferences p   = get();
        PlayerInventory inv = PlayerInventory.get();

        // Monedas y vidas
        inv.setCoins(p.getInteger(KEY_COINS, 120));
        inv.setLives(p.getInteger(KEY_LIVES, 5));
        inv.setPowerupMoves(p.getInteger(KEY_MOVES_PUP, 2));
        inv.setPowerupBombs(p.getInteger(KEY_BOMBS_PUP, 1));
        inv.setPowerupWildcards(p.getInteger(KEY_WILDCARD_PUP, 0));

        // Skins
        String[] skinIds = { "default", "neon", "pastel", "metallic", "galaxy" };
        for (String id : skinIds) {
            if (p.getBoolean(KEY_SKIN_PREFIX + id, id.equals("default"))) {
                inv.unlockSkin(id);
            }
        }
        String activeSkin = p.getString(KEY_ACTIVE_SKIN, "default");
        inv.activateSkin(activeSkin);
    }

    public static void loadStars() {
        Preferences p  = get();
        GameSession gs = GameSession.get();
        for (int w = 0; w < 6; w++) {
            for (int l = 0; l < 4; l++) {
                int stars = p.getInteger(KEY_STARS_PREFIX + w + "_l" + l, 0);
                gs.setStars(w, l, stars);
            }
        }
    }

    public static void loadAudioSettings() {
        Preferences p = get();
        AudioManager.get().setMusicEnabled(p.getBoolean(KEY_MUSIC_ON, true));
        AudioManager.get().setSoundEnabled(p.getBoolean(KEY_SOUND_ON, true));
    }

    /** Borra todos los datos guardados (reset completo) */
    public static void resetAll() {
        get().clear();
        get().flush();
    }

    /** ¿Es la primera vez que se abre el juego? */
    public static boolean isFirstLaunch() {
        return !get().contains(KEY_COINS);
    }
}
