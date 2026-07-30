package com.quimimatch.managers;

import com.quimimatch.board.AtomType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo de compuestos especiales de QuimiMatch.
 *
 * Hay un compuesto asignado a cada nivel Runner:
 * niveles globales 0, 2, 4, 6... 22.
 */
public final class CompoundDatabase {

    private CompoundDatabase() {
        // Evita crear objetos de esta clase.
    }

    // ── Mundo 1: cacao y energía ────────────────────────────────

    public static final Compound THEOBROMINE = new Compound(
        "theobromine",
        "Teobromina",
        "C7H8N4O2",
        "El secreto del chocolate",
        "Es uno de los compuestos característicos del cacao.",
        "Su nombre no significa que contenga bromo.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{7, 8, 4, 2}
    );

    public static final Compound CAFFEINE = new Compound(
        "caffeine",
        "Cafeína",
        "C8H10N4O2",
        "La energía del café",
        "Es un compuesto presente en el café, el té y algunas bebidas energéticas.",
        "Su fórmula se parece a la de la teobromina, pero su estructura es diferente.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{8, 10, 4, 2}
    );

    // ── Mundo 2: emociones ──────────────────────────────────────

    public static final Compound ADRENALINE = new Compound(
        "adrenaline",
        "Adrenalina",
        "C9H13NO3",
        "La química de la emoción",
        "El cuerpo la libera en situaciones de alerta o tensión.",
        "También se conoce como epinefrina.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{9, 13, 1, 3}
    );

    public static final Compound DOPAMINE = new Compound(
        "dopamine",
        "Dopamina",
        "C8H11NO2",
        "La molécula de la motivación",
        "Participa en procesos relacionados con motivación, movimiento y recompensa.",
        "No es simplemente la molécula de la felicidad; tiene varias funciones.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{8, 11, 1, 2}
    );

    // ── Mundo 3: mente y sueño ──────────────────────────────────

    public static final Compound SEROTONIN = new Compound(
        "serotonin",
        "Serotonina",
        "C10H12N2O",
        "Química del bienestar",
        "Participa en funciones relacionadas con el estado de ánimo y otros procesos corporales.",
        "Gran parte de la serotonina del organismo se encuentra fuera del cerebro.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{10, 12, 2, 1}
    );

    public static final Compound MELATONIN = new Compound(
        "melatonin",
        "Melatonina",
        "C13H16N2O2",
        "La química del sueño",
        "Es una hormona relacionada con los ciclos de sueño y vigilia.",
        "La oscuridad favorece su producción natural.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{13, 16, 2, 2}
    );

    // ── Mundo 4: alimentación ───────────────────────────────────

    public static final Compound GLUCOSE = new Compound(
        "glucose",
        "Glucosa",
        "C6H12O6",
        "Energía para tus células",
        "Es una fuente importante de energía para el organismo.",
        "También se conoce como azúcar en sangre cuando circula por el cuerpo.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.O
        },
        new int[]{6, 12, 6}
    );

    public static final Compound VITAMIN_C = new Compound(
        "vitamin_c",
        "Vitamina C",
        "C6H8O6",
        "La química de las frutas",
        "Su nombre químico es ácido ascórbico.",
        "Se encuentra en frutas y verduras como cítricos, kiwi y pimentón.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.O
        },
        new int[]{6, 8, 6}
    );

    // ── Mundo 5: medicina ───────────────────────────────────────

    public static final Compound ASPIRIN = new Compound(
        "aspirin",
        "Aspirina",
        "C9H8O4",
        "Química de la medicina",
        "Su nombre químico es ácido acetilsalicílico.",
        "Su historia está relacionada con sustancias encontradas en el sauce.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.O
        },
        new int[]{9, 8, 4}
    );

    public static final Compound PARACETAMOL = new Compound(
        "paracetamol",
        "Paracetamol",
        "C8H9NO2",
        "Una molécula conocida",
        "También se conoce como acetaminofén en algunos países.",
        "Su nombre cambia según el país, pero se trata del mismo compuesto.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{8, 9, 1, 2}
    );

    // ── Mundo 6: deporte y movimiento ───────────────────────────

    public static final Compound LACTIC_ACID = new Compound(
        "lactic_acid",
        "Ácido láctico",
        "C3H6O3",
        "Química del ejercicio",
        "Es un compuesto relacionado con el metabolismo energético.",
        "El lactato puede ser reutilizado por el organismo como fuente de energía.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.O
        },
        new int[]{3, 6, 3}
    );

    public static final Compound CREATINE = new Compound(
        "creatine",
        "Creatina",
        "C4H9N3O2",
        "Energía muscular",
        "Participa en sistemas que ayudan a suministrar energía a músculos y otros tejidos.",
        "El cuerpo puede producir creatina y también obtenerla de ciertos alimentos.",
        new AtomType[]{
            AtomType.C,
            AtomType.H,
            AtomType.N,
            AtomType.O
        },
        new int[]{4, 9, 3, 2}
    );

    private static final List<Compound> ALL = Collections.unmodifiableList(
        Arrays.asList(
            THEOBROMINE,
            CAFFEINE,
            ADRENALINE,
            DOPAMINE,
            SEROTONIN,
            MELATONIN,
            GLUCOSE,
            VITAMIN_C,
            ASPIRIN,
            PARACETAMOL,
            LACTIC_ACID,
            CREATINE
        )
    );

    public static List<Compound> getAll() {
        return ALL;
    }

    public static Compound getById(String id) {
        if (id == null) {
            return null;
        }

        for (Compound compound : ALL) {
            if (compound.getId().equalsIgnoreCase(id)) {
                return compound;
            }
        }

        return null;
    }

    /**
     * Asigna un compuesto a cada nivel Runner.
     *
     * Los niveles Runner son:
     * 0, 2, 4, 6, 8... 22.
     *
     * Los niveles Candy son:
     * 1, 3, 5, 7... 23.
     */
    public static Compound getForLevel(int world, int level) {
        int globalLevel = world * 4 + level;

        switch (globalLevel) {
            case 0:
                return THEOBROMINE;

            case 2:
                return CAFFEINE;

            case 4:
                return ADRENALINE;

            case 6:
                return DOPAMINE;

            case 8:
                return SEROTONIN;

            case 10:
                return MELATONIN;

            case 12:
                return GLUCOSE;

            case 14:
                return VITAMIN_C;

            case 16:
                return ASPIRIN;

            case 18:
                return PARACETAMOL;

            case 20:
                return LACTIC_ACID;

            case 22:
                return CREATINE;

            default:
                return null;
        }
    }

    public static boolean isSpecialLevel(int world, int level) {
        return getForLevel(world, level) != null;
    }
}
