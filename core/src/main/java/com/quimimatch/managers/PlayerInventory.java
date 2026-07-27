package com.quimimatch.managers;

import java.util.HashMap;
import java.util.Map;

/**
 * Inventario del jugador — monedas, power-ups y skins.
 * Singleton que persiste durante la sesión.
 */
public class PlayerInventory {

    private static PlayerInventory instance;
    public static PlayerInventory get() {
        if (instance == null) instance = new PlayerInventory();
        return instance;
    }

    // ── Monedas ──────────────────────────────────────────────────
    private int coins = 120; // monedas iniciales para probar

    // ── Power-ups ────────────────────────────────────────────────
    private int powerupMoves    = 2; // +5 movimientos extra
    private int powerupBomb     = 1; // bomba manual (elimina 3x3)
    private int powerupWildcard = 0; // comodín manual

    // ── Skins activa ─────────────────────────────────────────────
    private String activeSkin = "default";
    private Map<String, Boolean> unlockedSkins = new HashMap<>();

    private PlayerInventory() {
        unlockedSkins.put("default",  true);
        unlockedSkins.put("neon",     false);
        unlockedSkins.put("pastel",   false);
        unlockedSkins.put("metallic", false);
        unlockedSkins.put("galaxy",   false);
    }

    // ── Setters para SaveManager ─────────────────────────────────
    public void setCoins(int v)            { coins = v; }
    public void setPowerupMoves(int v)     { powerupMoves = v; }
    public void setLives(int v)            { GameSession.get().setLives(v); }
    public void setPowerupBombs(int v)     { powerupBomb = v; }
    public void setPowerupWildcards(int v) { powerupWildcard = v; }
    public void unlockSkin(String id)      { unlockedSkins.put(id, true); }

    // ── Monedas ──────────────────────────────────────────────────
    public int  getCoins()          { return coins; }
    public void addCoins(int amount){ coins += amount; }
    public boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

    // ── Power-ups ────────────────────────────────────────────────
    public int  getMoves()    { return powerupMoves; }
    public int  getBombs()    { return powerupBomb; }
    public int  getWildcards(){ return powerupWildcard; }

    public boolean buyMoves(int cost) {
        if (!spendCoins(cost)) return false;
        powerupMoves++;
        return true;
    }
    public boolean buyBomb(int cost) {
        if (!spendCoins(cost)) return false;
        powerupBomb++;
        return true;
    }
    public boolean buyWildcard(int cost) {
        if (!spendCoins(cost)) return false;
        powerupWildcard++;
        return true;
    }

    /** Usa un power-up de movimientos en el nivel (+5 movs) */
    public boolean useMoves() {
        if (powerupMoves <= 0) return false;
        powerupMoves--;
        return true;
    }
    public boolean useBomb() {
        if (powerupBomb <= 0) return false;
        powerupBomb--;
        return true;
    }
    public boolean useWildcard() {
        if (powerupWildcard <= 0) return false;
        powerupWildcard--;
        return true;
    }

    // ── Skins ────────────────────────────────────────────────────
    public String getActiveSkin() { return activeSkin; }

    public boolean isSkinUnlocked(String skinId) {
        return unlockedSkins.getOrDefault(skinId, false);
    }

    public boolean buySkin(String skinId, int cost) {
        if (isSkinUnlocked(skinId)) { activeSkin = skinId; return true; }
        if (!spendCoins(cost)) return false;
        unlockedSkins.put(skinId, true);
        activeSkin = skinId;
        return true;
    }

    public void activateSkin(String skinId) {
        if (isSkinUnlocked(skinId)) activeSkin = skinId;
    }

    // ── Recompensas ──────────────────────────────────────────────
    /** Llamar al completar nivel — gana monedas según estrellas */
    public void rewardLevel(int stars) {
        int reward = stars * 15;
        addCoins(reward);
    }

    /** Llamar al terminar Runner — gana monedas por átomos */
    public void rewardRunner(int atomsCollected) {
        addCoins(atomsCollected * 3);
    }
}
