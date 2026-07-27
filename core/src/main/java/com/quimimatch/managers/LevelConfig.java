package com.quimimatch.managers;

import com.quimimatch.board.AtomType;

/**
 * Configuración completa de los 6 mundos x 4 niveles = 24 niveles.
 */
public class LevelConfig {

    public final int     world;
    public final int     level;
    public final int     moves;
    public final AtomType[] atoms;
    public final AtomType[] goalAtoms;
    public final int[]   goalAmounts;
    public final String  moleculeName;
    public final int     starScore2;
    public final int     starScore3;

    public LevelConfig(int world, int level, int moves,
                       AtomType[] atoms,
                       AtomType[] goalAtoms, int[] goalAmounts,
                       String moleculeName,
                       int starScore2, int starScore3) {
        this.world        = world;
        this.level        = level;
        this.moves        = moves;
        this.atoms        = atoms;
        this.goalAtoms    = goalAtoms;
        this.goalAmounts  = goalAmounts;
        this.moleculeName = moleculeName;
        this.starScore2   = starScore2;
        this.starScore3   = starScore3;
    }

    // ── Átomos por mundo ────────────────────────────────────────
    private static final AtomType[] W1 = { AtomType.H,  AtomType.O,  AtomType.C,  AtomType.N  };
    private static final AtomType[] W2 = { AtomType.H,  AtomType.O,  AtomType.Na, AtomType.Cl };
    private static final AtomType[] W3 = { AtomType.H,  AtomType.O,  AtomType.S,  AtomType.N  };
    private static final AtomType[] W4 = { AtomType.Fe, AtomType.O,  AtomType.C,  AtomType.Mg };
    private static final AtomType[] W5 = { AtomType.C,  AtomType.H,  AtomType.O,  AtomType.N  };
    private static final AtomType[] W6 = { AtomType.Cu, AtomType.Zn, AtomType.Fe, AtomType.O  };

    public static final LevelConfig[] ALL = {

        // ════════════════════════════════════════════════════════
        // MUNDO 1 — Moleculas Basicas
        // ════════════════════════════════════════════════════════
        new LevelConfig(1, 1, 20, W1,
            new AtomType[]{ AtomType.H, AtomType.O },
            new int[]{ 10, 5 },
            "H2O", 3000, 6000),

        new LevelConfig(1, 2, 22, W1,
            new AtomType[]{ AtomType.H, AtomType.O },
            new int[]{ 20, 10 },
            "H2O2", 5000, 10000),

        new LevelConfig(1, 3, 25, W1,
            new AtomType[]{ AtomType.N, AtomType.H },
            new int[]{ 5, 15 },
            "NH3", 6000, 12000),

        new LevelConfig(1, 4, 30, W1,
            new AtomType[]{ AtomType.C, AtomType.O, AtomType.H },
            new int[]{ 6, 6, 6 },
            "C2H2O", 8000, 16000),

        // ════════════════════════════════════════════════════════
        // MUNDO 2 — Sales
        // ════════════════════════════════════════════════════════
        new LevelConfig(2, 1, 20, W2,
            new AtomType[]{ AtomType.Na, AtomType.Cl },
            new int[]{ 8, 8 },
            "NaCl", 4000, 8000),

        new LevelConfig(2, 2, 25, W2,
            new AtomType[]{ AtomType.H, AtomType.O, AtomType.Na },
            new int[]{ 10, 5, 5 },
            "NaOH", 6000, 12000),

        new LevelConfig(2, 3, 28, W2,
            new AtomType[]{ AtomType.Na, AtomType.Cl, AtomType.H },
            new int[]{ 10, 10, 5 },
            "HCl+Na", 8000, 15000),

        new LevelConfig(2, 4, 32, W2,
            new AtomType[]{ AtomType.H, AtomType.O, AtomType.Na, AtomType.Cl },
            new int[]{ 8, 8, 8, 8 },
            "Sal Marina", 10000, 20000),

        // ════════════════════════════════════════════════════════
        // MUNDO 3 — Acidos
        // ════════════════════════════════════════════════════════
        new LevelConfig(3, 1, 22, W3,
            new AtomType[]{ AtomType.H, AtomType.O },
            new int[]{ 12, 8 },
            "H2SO4", 5000, 10000),

        new LevelConfig(3, 2, 25, W3,
            new AtomType[]{ AtomType.H, AtomType.S },
            new int[]{ 8, 6 },
            "H2S", 6000, 12000),

        new LevelConfig(3, 3, 28, W3,
            new AtomType[]{ AtomType.N, AtomType.O, AtomType.H },
            new int[]{ 6, 12, 4 },
            "HNO3", 8000, 16000),

        new LevelConfig(3, 4, 32, W3,
            new AtomType[]{ AtomType.S, AtomType.O, AtomType.H, AtomType.N },
            new int[]{ 8, 8, 8, 4 },
            "Acido Mix", 12000, 24000),

        // ════════════════════════════════════════════════════════
        // MUNDO 4 — Metales
        // ════════════════════════════════════════════════════════
        new LevelConfig(4, 1, 24, W4,
            new AtomType[]{ AtomType.Fe, AtomType.O },
            new int[]{ 10, 15 },
            "Fe2O3", 6000, 12000),

        new LevelConfig(4, 2, 26, W4,
            new AtomType[]{ AtomType.Mg, AtomType.O },
            new int[]{ 8, 8 },
            "MgO", 7000, 14000),

        new LevelConfig(4, 3, 30, W4,
            new AtomType[]{ AtomType.Fe, AtomType.C },
            new int[]{ 12, 6 },
            "Acero", 10000, 20000),

        new LevelConfig(4, 4, 35, W4,
            new AtomType[]{ AtomType.Fe, AtomType.O, AtomType.Mg, AtomType.C },
            new int[]{ 8, 8, 6, 6 },
            "Aleacion", 14000, 28000),

        // ════════════════════════════════════════════════════════
        // MUNDO 5 — Quimica Organica
        // ════════════════════════════════════════════════════════
        new LevelConfig(5, 1, 25, W5,
            new AtomType[]{ AtomType.C, AtomType.H },
            new int[]{ 6, 14 },
            "C6H14", 7000, 14000),

        new LevelConfig(5, 2, 28, W5,
            new AtomType[]{ AtomType.C, AtomType.H, AtomType.O },
            new int[]{ 6, 12, 6 },
            "Glucosa", 9000, 18000),

        new LevelConfig(5, 3, 32, W5,
            new AtomType[]{ AtomType.C, AtomType.H, AtomType.N },
            new int[]{ 6, 8, 2 },
            "Aminoacido", 12000, 24000),

        new LevelConfig(5, 4, 38, W5,
            new AtomType[]{ AtomType.C, AtomType.H, AtomType.O, AtomType.N },
            new int[]{ 8, 10, 6, 4 },
            "Proteina", 16000, 32000),

        // ════════════════════════════════════════════════════════
        // MUNDO 6 — Quimica Avanzada
        // ════════════════════════════════════════════════════════
        new LevelConfig(6, 1, 28, W6,
            new AtomType[]{ AtomType.Cu, AtomType.O },
            new int[]{ 10, 10 },
            "CuO", 8000, 16000),

        new LevelConfig(6, 2, 30, W6,
            new AtomType[]{ AtomType.Zn, AtomType.O },
            new int[]{ 10, 10 },
            "ZnO", 10000, 20000),

        new LevelConfig(6, 3, 34, W6,
            new AtomType[]{ AtomType.Cu, AtomType.Zn, AtomType.O },
            new int[]{ 8, 8, 12 },
            "Laton", 14000, 28000),

        new LevelConfig(6, 4, 40, W6,
            new AtomType[]{ AtomType.Cu, AtomType.Zn, AtomType.Fe, AtomType.O },
            new int[]{ 8, 8, 8, 8 },
            "Bronce", 18000, 36000),
    };

    public static LevelConfig get(int worldIndex, int levelIndex) {
        int idx = worldIndex * 4 + levelIndex;
        if (idx < 0 || idx >= ALL.length) return ALL[0];
        return ALL[idx];
    }
}
