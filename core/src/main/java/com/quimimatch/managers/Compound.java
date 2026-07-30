package com.quimimatch.managers;

import com.quimimatch.board.AtomType;

/**
 * Representa un compuesto especial utilizado
 * en las expediciones del juego.
 */
public class Compound {

    private final String id;
    private final String name;
    private final String formula;
    private final String theme;
    private final String description;
    private final String curiosity;

    private final AtomType[] atoms;
    private final int[] atomCounts;

    public Compound(
        String id,
        String name,
        String formula,
        String theme,
        String description,
        String curiosity,
        AtomType[] atoms,
        int[] atomCounts
    ) {
        if (atoms == null || atomCounts == null || atoms.length != atomCounts.length) {
            throw new IllegalArgumentException(
                "Los átomos y sus cantidades deben tener la misma longitud."
            );
        }

        this.id = id;
        this.name = name;
        this.formula = formula;
        this.theme = theme;
        this.description = description;
        this.curiosity = curiosity;
        this.atoms = atoms.clone();
        this.atomCounts = atomCounts.clone();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormula() {
        return formula;
    }

    public String getTheme() {
        return theme;
    }

    public String getDescription() {
        return description;
    }

    public String getCuriosity() {
        return curiosity;
    }

    public AtomType[] getAtoms() {
        return atoms.clone();
    }

    public int[] getAtomCounts() {
        return atomCounts.clone();
    }
}
