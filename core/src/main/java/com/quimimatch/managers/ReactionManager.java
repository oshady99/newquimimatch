package com.quimimatch.managers;

import com.quimimatch.board.AtomType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controla la lógica química del modo Runner.
 *
 * Responsabilidades:
 * - Validar átomos recolectados.
 * - Detectar átomos incorrectos o sobrantes.
 * - Controlar la pureza de la reacción.
 * - Llevar la racha de recolecciones correctas.
 * - Informar el progreso de la ecuación.
 * - Generar mensajes para Byte.
 */
public class ReactionManager {

    public enum CollectionType {
        CORRECT,
        EXTRA,
        INCORRECT,
        COMPLETED,
        CONTAMINATED
    }

    /**
     * Resultado producido al recoger un átomo.
     */
    public static class CollectionResult {

        private final CollectionType type;
        private final AtomType atom;
        private final int purity;
        private final int streak;
        private final String message;

        public CollectionResult(
            CollectionType type,
            AtomType atom,
            int purity,
            int streak,
            String message
        ) {
            this.type = type;
            this.atom = atom;
            this.purity = purity;
            this.streak = streak;
            this.message = message;
        }

        public CollectionType getType() {
            return type;
        }

        public AtomType getAtom() {
            return atom;
        }

        public int getPurity() {
            return purity;
        }

        public int getStreak() {
            return streak;
        }

        public String getMessage() {
            return message;
        }

        public boolean isCorrect() {
            return type == CollectionType.CORRECT
                || type == CollectionType.COMPLETED;
        }
    }

    private static final int INITIAL_PURITY = 100;
    private static final int INCORRECT_PENALTY = 15;
    private static final int EXTRA_PENALTY = 5;

    private final EquationUtil.RunnerTarget target;
    private final Map<AtomType, Integer> collected;

    private int purity;
    private int currentStreak;
    private int bestStreak;
    private int correctAtoms;
    private int incorrectAtoms;

    private boolean complete;
    private boolean contaminated;

    private String byteMessage;
    private float byteMessageTime;

    public ReactionManager(EquationUtil.RunnerTarget target) {
        if (target == null) {
            throw new IllegalArgumentException(
                "ReactionManager necesita un RunnerTarget válido."
            );
        }

        this.target = target;
        this.collected = new LinkedHashMap<>();

        reset();
    }

    /**
     * Restablece la reacción para una nueva partida.
     */
    public void reset() {
        collected.clear();

        purity = INITIAL_PURITY;
        currentStreak = 0;
        bestStreak = 0;
        correctAtoms = 0;
        incorrectAtoms = 0;

        complete = false;
        contaminated = false;

        byteMessage = getNextRequiredMessage();
        byteMessageTime = 3f;
    }

    /**
     * Procesa el átomo recogido por el jugador.
     */
    public CollectionResult collectAtom(AtomType atom) {
        if (atom == null) {
            return createResult(
                CollectionType.INCORRECT,
                null,
                "Byte: No pude identificar ese átomo."
            );
        }

        if (contaminated) {
            return createResult(
                CollectionType.CONTAMINATED,
                atom,
                "Byte: La reacción ya está contaminada."
            );
        }

        if (complete) {
            return createResult(
                CollectionType.EXTRA,
                atom,
                "Byte: La ecuación ya está completa."
            );
        }

        int targetIndex = findTargetIndex(atom);

        // El átomo no pertenece a la ecuación.
        if (targetIndex == -1) {
            incorrectAtoms++;
            currentStreak = 0;
            reducePurity(INCORRECT_PENALTY);

            GameSession.get().registerIncorrectAction();

            if (purity <= 0) {
                contaminated = true;

                return createResult(
                    CollectionType.CONTAMINATED,
                    atom,
                    "Byte: ¡Reacción contaminada!"
                );
            }

            return createResult(
                CollectionType.INCORRECT,
                atom,
                "Byte: " + atom.getSymbol()
                    + " no pertenece a esta reacción."
            );
        }

        int collectedAmount = collected.getOrDefault(atom, 0);
        int requiredAmount = target.neededCounts[targetIndex];

        // El átomo pertenece, pero ya se recogió la cantidad necesaria.
        if (collectedAmount >= requiredAmount) {
            incorrectAtoms++;
            currentStreak = 0;
            reducePurity(EXTRA_PENALTY);

            GameSession.get().registerIncorrectAction();

            return createResult(
                CollectionType.EXTRA,
                atom,
                "Byte: Ya tenemos suficiente "
                    + atom.getSymbol() + "."
            );
        }

        // Recolección correcta.
        collected.put(atom, collectedAmount + 1);

        correctAtoms++;
        currentStreak++;

        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }

        GameSession.get().registerCorrectAction(100);

        if (target.isComplete(collected)) {
            complete = true;

            return createResult(
                CollectionType.COMPLETED,
                atom,
                "Byte: ¡Ecuación balanceada!"
            );
        }

        return createResult(
            CollectionType.CORRECT,
            atom,
            getNextRequiredMessage()
        );
    }

    /**
     * Actualiza el tiempo durante el que permanece visible
     * el mensaje de Byte.
     */
    public void update(float delta) {
        if (byteMessageTime > 0f) {
            byteMessageTime = Math.max(0f, byteMessageTime - delta);
        }
    }

    /**
     * Comprueba si un átomo forma parte de la ecuación objetivo.
     */
    public boolean isTargetAtom(AtomType atom) {
        return findTargetIndex(atom) >= 0;
    }

    /**
     * Comprueba si todavía hace falta recoger ese átomo.
     */
    public boolean isAtomStillNeeded(AtomType atom) {
        int index = findTargetIndex(atom);

        if (index == -1) {
            return false;
        }

        int have = collected.getOrDefault(atom, 0);
        int need = target.neededCounts[index];

        return have < need;
    }

    /**
     * Devuelve cuántos átomos faltan en total.
     */
    public int getRemainingAtomCount() {
        int remaining = 0;

        for (int i = 0; i < target.atoms.length; i++) {
            AtomType atom = target.atoms[i];
            int have = collected.getOrDefault(atom, 0);
            int need = target.neededCounts[i];

            remaining += Math.max(need - have, 0);
        }

        return remaining;
    }

    /**
     * Genera un mensaje indicando cuál es el siguiente átomo necesario.
     */
    public String getNextRequiredMessage() {
        for (int i = 0; i < target.atoms.length; i++) {
            AtomType atom = target.atoms[i];
            int have = collected.getOrDefault(atom, 0);
            int need = target.neededCounts[i];

            if (have < need) {
                int remaining = need - have;

                return "Byte: Necesitamos "
                    + remaining
                    + " de "
                    + atom.getSymbol()
                    + ".";
            }
        }

        return "Byte: La reacción está lista.";
    }

    private int findTargetIndex(AtomType atom) {
        for (int i = 0; i < target.atoms.length; i++) {
            if (target.atoms[i] == atom) {
                return i;
            }
        }

        return -1;
    }

    private void reducePurity(int amount) {
        purity = Math.max(0, purity - amount);
    }

    private CollectionResult createResult(
        CollectionType type,
        AtomType atom,
        String message
    ) {
        byteMessage = message;
        byteMessageTime = 2.5f;

        return new CollectionResult(
            type,
            atom,
            purity,
            currentStreak,
            message
        );
    }

    public EquationUtil.RunnerTarget getTarget() {
        return target;
    }

    public Map<AtomType, Integer> getCollected() {
        return Collections.unmodifiableMap(collected);
    }

    public int getCollectedAmount(AtomType atom) {
        return collected.getOrDefault(atom, 0);
    }

    public int getPurity() {
        return purity;
    }

    public float getPurityProgress() {
        return purity / 100f;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public int getCorrectAtoms() {
        return correctAtoms;
    }

    public int getIncorrectAtoms() {
        return incorrectAtoms;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isContaminated() {
        return contaminated;
    }

    public String getByteMessage() {
        return byteMessage;
    }

    public boolean shouldShowByteMessage() {
        return byteMessageTime > 0f
            && byteMessage != null
            && !byteMessage.isEmpty();
    }
}
