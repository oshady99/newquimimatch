package com.quimimatch.managers;

import com.quimimatch.board.AtomType;

/**
 * Utilidad para derivar la ecuación química balanceada a partir de los
 * ratios de átomos objetivo que ya define LevelConfig (goalAtoms/goalAmounts).
 *
 * Los goalAmounts de un nivel ya están en la proporción correcta de la
 * molécula real (ej. H2O -> {10,5} = ratio 2:1). Esta clase reduce esa
 * proporción a los coeficientes mínimos (2:1) para mostrarla como una
 * ecuación química legible, y calcula cuántos átomos de cada tipo se
 * necesitan recolectar en el Runner (una versión corta, no la del nivel
 * completo de Match-3).
 */
public class EquationUtil {

    /** Resultado: coeficientes reducidos + cantidades objetivo para el Runner. */
    public static class RunnerTarget {
        public final AtomType[] atoms;
        public final int[] coeffs;      // coeficientes balanceados mínimos (ej. 2, 1)
        public final int[] neededCounts; // cantidad a recolectar en el runner (coeffs * factor)
        public final String moleculeName;
        public final boolean decompose;  // true = modo descomposición (fase futura)

        public RunnerTarget(AtomType[] atoms, int[] coeffs, int[] neededCounts,
                            String moleculeName, boolean decompose) {
            this.atoms = atoms;
            this.coeffs = coeffs;
            this.neededCounts = neededCounts;
            this.moleculeName = moleculeName;
            this.decompose = decompose;
        }

        /** Texto de la ecuación, ej: "2 H + 1 O -> H2O" (o al revés si decompose) */
        public String equationString() {
            StringBuilder left = new StringBuilder();
            for (int i = 0; i < atoms.length; i++) {
                if (i > 0) left.append(" + ");
                left.append(coeffs[i] > 1 ? coeffs[i] + " " : "").append(atoms[i].getSymbol());
            }
            return decompose
                    ? moleculeName + " -> " + left
                    : left + " -> " + moleculeName;
        }

        /** ¿Ya se recolectó lo necesario de todos los átomos? */
        public boolean isComplete(java.util.Map<AtomType, Integer> collected) {
            for (int i = 0; i < atoms.length; i++) {
                if (collected.getOrDefault(atoms[i], 0) < neededCounts[i]) return false;
            }
            return true;
        }
    }

    private static int gcd(int a, int b) {
        while (b != 0) { int t = b; b = a % b; a = t; }
        return a;
    }

    /**
     * Construye el objetivo del Runner a partir del LevelConfig actual.
     * @param repeatFactor cuánto multiplicar la proporción reducida para que
     *                     alcance una duración de juego razonable (ej. 3).
     */
    public static RunnerTarget fromLevelConfig(LevelConfig cfg, boolean decompose, int repeatFactor) {
        int n = cfg.goalAtoms.length;
        int[] amounts = cfg.goalAmounts.clone();

        // Reducir todos los amounts por su MCD para obtener coeficientes mínimos
        int g = amounts[0];
        for (int i = 1; i < n; i++) g = gcd(g, amounts[i]);
        if (g <= 0) g = 1;

        int[] coeffs = new int[n];
        int[] needed = new int[n];
        for (int i = 0; i < n; i++) {
            coeffs[i] = Math.max(1, amounts[i] / g);
            needed[i] = coeffs[i] * repeatFactor;
        }

        return new RunnerTarget(cfg.goalAtoms, coeffs, needed, cfg.moleculeName, decompose);
    }

    /**
     * Construye el objetivo del Runner a partir
     * de un compuesto especial del catálogo.
     */
    public static RunnerTarget fromCompound(
        Compound compound,
        boolean decompose,
        int repeatFactor
    ) {
        if (compound == null) {
            throw new IllegalArgumentException(
                "El compuesto no puede ser null."
            );
        }

        AtomType[] atoms = compound.getAtoms();
        int[] baseCounts = compound.getAtomCounts();

        int[] neededCounts = new int[baseCounts.length];

        for (int i = 0; i < baseCounts.length; i++) {
            neededCounts[i] = baseCounts[i] * repeatFactor;
        }

        return new RunnerTarget(
            atoms,
            baseCounts,
            neededCounts,
            compound.getFormula(),
            decompose
        );
    }
}
