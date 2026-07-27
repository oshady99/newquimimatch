package com.quimimatch.managers;

/**
 * Sprint 3A — Sesión de juego activa.
 * Lleva el estado del nivel actual: movimientos, puntos,
 * átomos recolectados, vidas y progreso.
 */
public class GameSession {

    // ── Singleton ────────────────────────────────────────────────
    private static GameSession instance;

    public static GameSession get() {
        if (instance == null) instance = new GameSession();
        return instance;
    }

    // ── Progresión ───────────────────────────────────────────────
    private int currentWorld = 0;   // índice base 0
    private int currentLevel = 0;   // índice base 0
    private int lives        = 5;
    private int[] levelStars;        // estrellas conseguidas por nivel

    // ── Estado del nivel en curso ────────────────────────────────
    private LevelConfig config;
    private int   score;
    private int   movesLeft;
    private int[] collected;         // átomos recogidos para cada objetivo

    private GameSession() {
        levelStars = new int[LevelConfig.ALL.length];
        startLevel(0, 0);
    }

    // ── Iniciar nivel ────────────────────────────────────────────

    public void startLevel(int worldIdx, int levelIdx) {
        currentWorld = worldIdx;
        currentLevel = levelIdx;
        config       = LevelConfig.get(worldIdx, levelIdx);
        score        = 0;
        movesLeft    = config.moves;
        collected    = new int[config.goalAtoms.length];
    }

    public void restartLevel() {
        startLevel(currentWorld, currentLevel);
    }

    // ── Acciones durante el juego ────────────────────────────────

    public void addScore(int pts) {
        score += pts;
    }

    public void useMove() {
        if (movesLeft > 0) movesLeft--;
    }

    public void addMoves(int amount) {
        movesLeft += amount;
    }

    /**
     * Registra que se eliminó un átomo del tipo dado.
     * Devuelve true si ese átomo era parte del objetivo.
     */
    public boolean collectAtom(com.quimimatch.board.AtomType type) {
        for (int i = 0; i < config.goalAtoms.length; i++) {
            if (config.goalAtoms[i] == type && collected[i] < config.goalAmounts[i]) {
                collected[i]++;
                return true;
            }
        }
        return false;
    }

    // ── Condiciones de victoria / derrota ────────────────────────

    /** ¿Se cumplieron todos los objetivos? */
    public boolean isVictory() {
        for (int i = 0; i < config.goalAtoms.length; i++) {
            if (collected[i] < config.goalAmounts[i]) return false;
        }
        return true;
    }

    /** ¿Se acabaron los movimientos sin ganar? */
    public boolean isDefeat() {
        return movesLeft <= 0 && !isVictory();
    }

    /** Calcula estrellas obtenidas (1-3) */
    public int calculateStars() {
        if (!isVictory()) return 0;
        if (score >= config.starScore3) return 3;
        if (score >= config.starScore2) return 2;
        return 1;
    }

    /** Guarda las estrellas si son mejores que las previas */
    public void saveStars() {
        int idx   = currentWorld * 4 + currentLevel;
        int stars = calculateStars();
        if (stars > levelStars[idx]) levelStars[idx] = stars;
    }

    /** Avanza al siguiente nivel. Devuelve false si no hay más. */
    public boolean nextLevel() {
        int nextL = currentLevel + 1;
        int nextW = currentWorld;
        if (nextL >= 4) { nextL = 0; nextW++; }
        if (nextW * 4 + nextL >= LevelConfig.ALL.length) return false;
        startLevel(nextW, nextL);
        return true;
    }

    // ── Vidas ────────────────────────────────────────────────────

    public void loseLife() { if (lives > 0) lives--; }
    public void addLife()  { if (lives < 5) lives++;  }
    public boolean hasLives() { return lives > 0; }

    // ── Getters ──────────────────────────────────────────────────


    public LevelConfig getConfig()      { return config; }
    public int  getScore()              { return score; }
    public int  getMovesLeft()          { return movesLeft; }
    public int  getLives()              { return lives; }
    public int  getCurrentWorld()       { return currentWorld; }
    public int  getCurrentLevel()       { return currentLevel; }
    public int[] getCollected()         { return collected; }
    public int   getStars(int w, int l) { return levelStars[w * 4 + l]; }
    public void setLives(int v) { lives = Math.max(0, Math.min(5, v)); }
    public void  setStars(int w, int l, int stars) {
        int idx = w * 4 + l;
        if (idx >= 0 && idx < levelStars.length) levelStars[idx] = stars;
    }

    /** Progreso del objetivo i entre 0.0 y 1.0 */
    public float getGoalProgress(int i) {
        if (i >= config.goalAmounts.length) return 0;
        return Math.min((float) collected[i] / config.goalAmounts[i], 1f);
    }
}
