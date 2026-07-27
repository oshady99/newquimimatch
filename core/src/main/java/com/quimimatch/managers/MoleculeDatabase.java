package com.quimimatch.managers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base de datos extendida de moléculas con propiedades completas.
 */
public class MoleculeDatabase {

    public static class MoleculeEntry {
        public final String formula;
        public final String name;
        public final String type;       // Tipo: Oxido, Sal, Acido, Base, Organica
        public final String emoji;
        public final String fact;
        public final String property1;  // Propiedad física
        public final String property2;  // Propiedad química
        public final String uses;       // Usos cotidianos
        public final int[]  atomColors; // índices de color (referencial)
        public boolean discovered = false;

        public MoleculeEntry(String formula, String name, String type, String emoji,
                             String fact, String property1, String property2, String uses) {
            this.formula   = formula;
            this.name      = name;
            this.type      = type;
            this.emoji     = emoji;
            this.fact      = fact;
            this.property1 = property1;
            this.property2 = property2;
            this.uses      = uses;
            this.atomColors = new int[]{};
        }
    }

    private static final Map<String, MoleculeEntry> DB = new LinkedHashMap<>();
    private static final List<MoleculeEntry> ALL_LIST  = new ArrayList<>();

    static {
        add("H2O",      "Agua",             "Oxido",
            "💧", "Cubre el 71% de la Tierra y es esencial para toda vida conocida.",
            "Liquida entre 0°C y 100°C",    "Disuelve casi todo: el solvente universal",
            "Beber, cocinar, higiene, industria");

        add("H2O2",     "Agua Oxigenada",   "Oxido",
            "🫧", "Las celulas la producen como arma para destruir bacterias invasoras.",
            "Liquida, inestable a temperatura alta", "Oxidante fuerte, libera O2 al descomponerse",
            "Desinfectante heridas, blanqueador, cohetes");

        add("NH3",      "Amoniaco",         "Base",
            "🌿", "El amoniaco sintetico alimenta a casi la mitad de la humanidad via fertilizantes.",
            "Gas de olor penetrante, -33°C liquido", "Base fuerte, reacciona con acidos",
            "Fertilizantes, limpiadores, refrigeracion");

        add("C2H2O",    "Acetaldehido",     "Organica",
            "⚗️", "Es el primer compuesto que el higado produce al metabolizar el alcohol.",
            "Liquido volatil, olor a manzana",       "Aldehido reactivo, se oxida facilmente",
            "Fabricacion de plasticos, perfumes");

        add("NaCl",     "Sal de Mesa",      "Sal",
            "🧂", "Los romanos pagaban a sus soldados con sal. De ahi viene la palabra salario.",
            "Solido cristalino, funde a 801°C",      "Ionico, conduce electricidad al disolverse",
            "Condimento, conservante, industria quimica");

        add("NaOH",     "Soda Caustica",    "Base",
            "🧪", "Es tan corrosiva que puede disolver tejidos biologicos en horas.",
            "Solido blanco, altamente higroscopico",  "Base fuerte, pH cercano a 14",
            "Fabricar jabon, papel, desatascadores");

        add("HCl+Na",   "Acido Clorhidrico","Acido",
            "⚠️", "Tu estomago produce HCl para digerir proteinas. El pH gastrico es de 1.5 a 2.",
            "Gas incoloro de olor sofocante",         "Acido fuerte, corroe metales",
            "Limpieza industrial, laboratorios, PVC");

        add("Sal Marina","Sal del Mar",     "Sal",
            "🌊", "El oceano contiene mas de 50 millones de toneladas de sal por km cubico.",
            "Mezcla de NaCl y minerales",            "Conductora electrica, higroscopica",
            "Gastronomia, talasoterapia, industria");

        add("H2SO4",    "Acido Sulfurico",  "Acido",
            "⚗️", "El acido sulfurico es el producto quimico mas fabricado del mundo.",
            "Liquido viscoso, muy corrosivo",         "Acido fuerte, deshidratante potente",
            "Fertilizantes, baterias, industria");

        add("H2S",      "Sulfuro de Hidrogeno","Acido",
            "🥚", "Huele exactamente a huevo podrido. Es toxico a altas concentraciones.",
            "Gas incoloro, olor caracteristico",      "Acido debil, reductor",
            "Laboratorios, industria del petroleo");

        add("HNO3",     "Acido Nitrico",    "Acido",
            "💣", "Se usa para fabricar explosivos como el TNT y fertilizantes.",
            "Liquido incoloro a amarillento",         "Acido fuerte, oxidante potente",
            "Explosivos, fertilizantes, colorantes");

        add("Acido Mix", "Mezcla Acida",    "Acido",
            "⚠️", "Las mezclas de acidos pueden ser extremadamente reactivas y peligrosas.",
            "Mezcla de multiples acidos",             "Reactividad combinada, muy corrosiva",
            "Investigacion quimica avanzada");

        add("Fe2O3",    "Oxido de Hierro",  "Oxido",
            "🦀", "Es el oxido rojo que conocemos como herrumbre o moho del hierro.",
            "Solido rojo-marron, muy estable",        "Oxido basico, poco soluble",
            "Pigmento, acero, imanes");

        add("MgO",      "Oxido de Magnesio","Oxido",
            "🔥", "Resiste temperaturas de hasta 2852 grados Celsius. Se usa en hornos.",
            "Solido blanco, punto de fusion muy alto","Oxido basico, antiácido",
            "Refractarios, medicamentos, fertilizantes");

        add("Acero",    "Acero",            "Aleacion",
            "🏗️", "El acero es la aleacion mas usada del mundo. Sin el no existirian rascacielos.",
            "Metalico, duro, maleable",               "Aleacion de Fe y C, resistente",
            "Construccion, herramientas, vehiculos");

        add("Aleacion", "Aleacion Metalica","Aleacion",
            "⚙️", "Las aleaciones combinan metales para obtener propiedades superiores.",
            "Variable segun componentes",             "Propiedades combinadas",
            "Ingenieria, manufactura avanzada");

        add("C6H14",    "Hexano",           "Organica",
            "⛽", "El hexano es un componente principal de la gasolina.",
            "Liquido incoloro, muy inflamable",       "Hidrocarburo apolar, disolvente",
            "Gasolina, disolvente industrial");

        add("Glucosa",  "Glucosa",          "Organica",
            "🍬", "Es el combustible principal de tu cerebro. Consume 120g de glucosa al dia.",
            "Solido blanco cristalino, dulce",        "Azucar simple, fuente de energia",
            "Medicina, alimentacion, fermentacion");

        add("Aminoacido","Aminoacido",      "Organica",
            "🧬", "Los aminoacidos son los ladrillos de todas las proteinas de tu cuerpo.",
            "Variable, generalmente solido",          "Anfotero, forma cadenas peptidicas",
            "Nutricion, farmaceutica, biotecnologia");

        add("Proteina", "Proteina",         "Organica",
            "💪", "Tu cuerpo contiene mas de 100.000 tipos distintos de proteinas.",
            "Solido coloidal, desnaturalizable",      "Polimero de aminoacidos",
            "Alimentacion, medicina, enzimas");

        add("CuO",      "Oxido de Cobre",   "Oxido",
            "🟫", "El oxido de cobre da el color negro a las cerámicas antiguas.",
            "Solido negro, punto de fusion 1201C",    "Oxido basico, semiconductor",
            "Ceramica, catalizadores, pigmentos");

        add("ZnO",      "Oxido de Zinc",    "Oxido",
            "🌞", "El oxido de zinc es el ingrediente activo del bloqueador solar.",
            "Polvo blanco, amplio band-gap",          "Semiconductor, fotocatalítico",
            "Protector solar, pinturas, electronica");

        add("Laton",    "Laton",            "Aleacion",
            "🎺", "El laton se usa en instrumentos musicales por su sonido brillante.",
            "Metalico amarillo, maleable",            "Aleacion Cu-Zn resistente",
            "Instrumentos, plomeria, decoracion");

        add("Bronce",   "Bronce",           "Aleacion",
            "🏺", "El Bronce marco una era de la humanidad: la Edad de Bronce.",
            "Metalico marron-dorado, duro",           "Aleacion Cu-Sn, resistente corrosion",
            "Arte, medallas, maquinaria, campanas");
    }

    private static void add(String formula, String name, String type, String emoji,
                             String fact, String p1, String p2, String uses) {
        MoleculeEntry e = new MoleculeEntry(formula, name, type, emoji, fact, p1, p2, uses);
        DB.put(formula, e);
        ALL_LIST.add(e);
    }

    public static MoleculeEntry get(String formula) {
        return DB.get(formula);
    }

    public static List<MoleculeEntry> getAll() {
        return ALL_LIST;
    }

    /** Marca una molécula como descubierta */
    public static void discover(String formula) {
        MoleculeEntry e = DB.get(formula);
        if (e != null) e.discovered = true;
    }

    /** Cuántas moléculas se han descubierto */
    public static int discoveredCount() {
        int count = 0;
        for (MoleculeEntry e : ALL_LIST) if (e.discovered) count++;
        return count;
    }
}
