package com.quimimatch.managers;

import com.quimimatch.board.AtomType;

/**
 * Sprint 3A — Sesión de juego activa.
 *
 * Lleva el estado del nivel actual:
 * movimientos, puntos, átomos recolectados, vidas,
 * progreso, rachas, multiplicador y estadísticas.
 */
public class GameSession {

    // ── Singleton ────────────────────────────────────────────────

    private static GameSession instance;

    public static GameSession get() {
        if (instance == null) {
            instance = new GameSession();
        }

        return instance;
    }

    // ── Progresión ───────────────────────────────────────────────

    private int currentWorld = 0;
    private int currentLevel = 0;
    private int lives = 5;
    private int[] levelStars;

    // ── Estado del nivel en curso ────────────────────────────────

    private LevelConfig config;
    private int score;
    private int movesLeft;
    private int[] collected;

    // ── Estadísticas y rachas ────────────────────────────────────

    private int correctActions;
    private int incorrectActions;
    private int currentStreak;
    private int bestStreak;

    private GameSession() {
        levelStars = new int[LevelConfig.ALL.length];
        startLevel(0, 0);
    }

    // ── Iniciar nivel ────────────────────────────────────────────

    public void startLevel(int worldIdx, int levelIdx) {
        currentWorld = worldIdx;
        currentLevel = levelIdx;
        config = LevelConfig.get(worldIdx, levelIdx);

        score = 0;
        movesLeft = config.moves;
        collected = new int[config.goalAtoms.length];

        correctActions = 0;
        incorrectActions = 0;
        currentStreak = 0;
        bestStreak = 0;
    }

    public void restartLevel() {
        startLevel(currentWorld, currentLevel);
    }

    // ── Acciones durante el juego ────────────────────────────────

    public void addScore(int points) {
        score += points;
    }

    public void useMove() {
        if (movesLeft > 0) {
            movesLeft--;
        }
    }

    public void addMoves(int amount) {
        movesLeft += amount;
    }

    /**
     * Registra una acción correcta.
     *
     * Incrementa la racha, actualiza la mejor racha,
     * calcula el multiplicador y suma los puntos obtenidos.
     *
     * @param basePoints puntos base de la acción.
     * @return puntos finales obtenidos aplicando el multiplicador.
     */
    public int registerCorrectAction(int basePoints) {
        correctActions++;
        currentStreak++;

        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }

        int earnedPoints = basePoints * getMultiplier();
        addScore(earnedPoints);

        return earnedPoints;
    }

    /**
     * Registra una acción incorrecta y reinicia la racha actual.
     */
    public void registerIncorrectAction() {
        incorrectActions++;
        currentStreak = 0;
    }

    /**
     * Calcula el multiplicador según la racha actual.
     */
    public int getMultiplier() {
        if (currentStreak >= 15) {
            return 4;
        }

        if (currentStreak >= 10) {
            return 3;
        }

        if (currentStreak >= 5) {
            return 2;
        }

        return 1;
    }

    /**
     * Devuelve la cantidad total de acciones registradas.
     */
    public int getTotalActions() {
        return correctActions + incorrectActions;
    }

    /**
     * Devuelve el porcentaje de precisión de la partida.
     */
    public float getAccuracyPercentage() {
        int totalActions = getTotalActions();

        if (totalActions == 0) {
            return 0f;
        }

        return correctActions * 100f / totalActions;
    }

    /**
     * Registra que se eliminó un átomo del tipo dado.
     *
     * @return true si el átomo formaba parte del objetivo.
     */
    public boolean collectAtom(AtomType type) {
        for (int i = 0; i < config.goalAtoms.length; i++) {
            boolean isGoalAtom = config.goalAtoms[i] == type;
            boolean stillNeeded = collected[i] < config.goalAmounts[i];

            if (isGoalAtom && stillNeeded) {
                collected[i]++;
                return true;
            }
        }

        return false;
    }

    // ── Condiciones de victoria y derrota ────────────────────────

    /**
     * Indica si se cumplieron todos los objetivos.
     */
    public boolean isVictory() {
        for (int i = 0; i < config.goalAtoms.length; i++) {
            if (collected[i] < config.goalAmounts[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indica si se acabaron los movimientos sin completar el nivel.
     */
    public boolean isDefeat() {
        return movesLeft <= 0 && !isVictory();
    }

    /**
     * Calcula las estrellas obtenidas entre 0 y 3.
     */
    public int calculateStars() {
        if (!isVictory()) {
            return 0;
        }

        if (score >= config.starScore3) {
            return 3;
        }

        if (score >= config.starScore2) {
            return 2;
        }

        return 1;
    }

    /**
     * Guarda las estrellas si superan el récord anterior.
     */
    public void saveStars() {
        int index = currentWorld * 4 + currentLevel;
        int stars = calculateStars();

        if (index >= 0
            && index < levelStars.length
            && stars > levelStars[index]) {

            levelStars[index] = stars;
        }
    }

    /**
     * Avanza al siguiente nivel.
     *
     * @return false si no quedan más niveles.
     */
    public boolean nextLevel() {
        int nextLevel = currentLevel + 1;
        int nextWorld = currentWorld;

        if (nextLevel >= 4) {
            nextLevel = 0;
            nextWorld++;
        }

        int nextIndex = nextWorld * 4 + nextLevel;

        if (nextIndex >= LevelConfig.ALL.length) {
            return false;
        }

        startLevel(nextWorld, nextLevel);
        return true;
    }

    // ── Vidas ────────────────────────────────────────────────────

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public void addLife() {
        if (lives < 5) {
            lives++;
        }
    }

    public boolean hasLives() {
        return lives > 0;
    }

    public void setLives(int value) {
        lives = Math.max(0, Math.min(5, value));
    }

    // ── Getters ──────────────────────────────────────────────────

    public LevelConfig getConfig() {
        return config;
    }

    public int getScore() {
        return score;
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public int getLives() {
        return lives;
    }

    public int getCurrentWorld() {
        return currentWorld;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int[] getCollected() {
        return collected;
    }

    public int getCorrectActions() {
        return correctActions;
    }

    public int getIncorrectActions() {
        return incorrectActions;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public int getStars(int world, int level) {
        int index = world * 4 + level;

        if (index < 0 || index >= levelStars.length) {
            return 0;
        }

        return levelStars[index];
    }

    public void setStars(int world, int level, int stars) {
        int index = world * 4 + level;

        if (index >= 0 && index < levelStars.length) {
            levelStars[index] = Math.max(0, Math.min(3, stars));
        }
    }

    /**
     * Devuelve el progreso del objetivo indicado entre 0.0 y 1.0.
     */
    public float getGoalProgress(int index) {
        if (index < 0 || index >= config.goalAmounts.length) {
            return 0f;
        }

        if (config.goalAmounts[index] <= 0) {
            return 0f;
        }

        return Math.min(
            (float) collected[index] / config.goalAmounts[index],
            1f
        );
    }
}
