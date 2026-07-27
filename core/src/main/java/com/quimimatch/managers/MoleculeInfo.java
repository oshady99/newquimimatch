package com.quimimatch.managers;

import java.util.HashMap;
import java.util.Map;

/**
 * Info rápida de moléculas para el MoleculePanel en el tablero.
 * Cubre los 24 niveles (6 mundos x 4 niveles).
 */
public class MoleculeInfo {

    public final String formula;
    public final String name;
    public final String emoji;
    public final String fact;

    public MoleculeInfo(String formula, String name, String emoji, String fact) {
        this.formula = formula;
        this.name    = name;
        this.emoji   = emoji;
        this.fact    = fact;
    }

    private static final Map<String, MoleculeInfo> DB = new HashMap<>();

    static {
        // Mundo 1 — Basicas
        add("H2O",      "Agua",              "💧", "Cubre el 71% de la Tierra. Sin ella no hay vida posible.");
        add("H2O2",     "Agua Oxigenada",    "🫧", "Las celulas la producen para destruir bacterias invasoras.");
        add("NH3",      "Amoniaco",          "🌿", "Alimenta a la mitad del mundo via fertilizantes.");
        add("C2H2O",    "Acetaldehido",      "⚗️", "Primer compuesto que el higado produce al metabolizar alcohol.");

        // Mundo 2 — Sales
        add("NaCl",     "Sal de Mesa",       "🧂", "Los romanos pagaban a sus soldados con sal. De ahi viene salario.");
        add("NaOH",     "Soda Caustica",     "🧪", "Es tan corrosiva que puede disolver tejidos biologicos en horas.");
        add("HCl+Na",   "Acido Clorhidrico", "⚠️", "Tu estomago produce HCl para digerir proteinas.");
        add("Sal Marina","Sal del Mar",      "🌊", "El oceano tiene mas de 50 millones de toneladas de sal por km cubico.");

        // Mundo 3 — Acidos
        add("H2SO4",    "Acido Sulfurico",   "⚗️", "El acido sulfurico es el producto quimico mas fabricado del mundo.");
        add("H2S",      "Sulfuro de H",      "🥚", "Huele exactamente a huevo podrido. Toxico a altas concentraciones.");
        add("HNO3",     "Acido Nitrico",     "💣", "Se usa para fabricar explosivos como el TNT y fertilizantes.");
        add("Acido Mix", "Mezcla Acida",     "⚠️", "Las mezclas de acidos pueden ser extremadamente reactivas.");

        // Mundo 4 — Metales
        add("Fe2O3",    "Oxido de Hierro",   "🦀", "Es el oxido rojo que conocemos como herrumbre del hierro.");
        add("MgO",      "Oxido de Magnesio", "🔥", "Resiste temperaturas de hasta 2852 grados Celsius.");
        add("Acero",    "Acero",             "🏗️", "La aleacion mas usada del mundo. Sin el no habria rascacielos.");
        add("Aleacion", "Aleacion Metalica", "⚙️", "Las aleaciones combinan metales para obtener propiedades superiores.");

        // Mundo 5 — Organica
        add("C6H14",    "Hexano",            "⛽", "El hexano es un componente principal de la gasolina.");
        add("Glucosa",  "Glucosa",           "🍬", "El combustible principal de tu cerebro: 120g al dia.");
        add("Aminoacido","Aminoacido",       "🧬", "Los ladrillos de todas las proteinas de tu cuerpo.");
        add("Proteina", "Proteina",          "💪", "Tu cuerpo contiene mas de 100.000 tipos distintos de proteinas.");

        // Mundo 6 — Avanzada
        add("CuO",      "Oxido de Cobre",    "🟫", "El oxido de cobre da el color negro a las ceramicas antiguas.");
        add("ZnO",      "Oxido de Zinc",     "🌞", "El ingrediente activo del bloqueador solar.");
        add("Laton",    "Laton",             "🎺", "Se usa en instrumentos musicales por su sonido brillante.");
        add("Bronce",   "Bronce",            "🏺", "El Bronce marco una era de la humanidad: la Edad de Bronce.");
    }

    private static void add(String formula, String name, String emoji, String fact) {
        DB.put(formula, new MoleculeInfo(formula, name, emoji, fact));
    }

    public static MoleculeInfo get(String formula) {
        return DB.get(formula);
    }
}
