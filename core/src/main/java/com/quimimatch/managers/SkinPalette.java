package com.quimimatch.managers;

import com.badlogic.gdx.graphics.Color;
import com.quimimatch.board.AtomType;

/**
 * Define los colores de cada átomo según el skin activo.
 */
public class SkinPalette {

    public static Color getColor(AtomType type) {
        String skin = PlayerInventory.get().getActiveSkin();
        switch (skin) {
            case "neon":     return getNeon(type);
            case "pastel":   return getPastel(type);
            case "metallic": return getMetallic(type);
            case "galaxy":   return getGalaxy(type);
            default:         return type.getColor(); // default original
        }
    }

    private static Color getNeon(AtomType type) {
        switch (type) {
            case H:  return new Color(0.0f,  1.0f,  1.0f,  1f); // cyan
            case O:  return new Color(1.0f,  0.0f,  0.5f,  1f); // magenta
            case C:  return new Color(0.5f,  0.0f,  1.0f,  1f); // violeta
            case N:  return new Color(0.0f,  1.0f,  0.0f,  1f); // verde neon
            case Na: return new Color(1.0f,  1.0f,  0.0f,  1f); // amarillo neon
            case Cl: return new Color(0.0f,  0.8f,  1.0f,  1f); // azul neon
            default: return type.getColor();
        }
    }

    private static Color getPastel(AtomType type) {
        switch (type) {
            case H:  return new Color(0.68f, 0.85f, 1.0f,  1f); // azul pastel
            case O:  return new Color(1.0f,  0.72f, 0.72f, 1f); // rosa pastel
            case C:  return new Color(0.78f, 0.78f, 0.78f, 1f); // gris pastel
            case N:  return new Color(0.82f, 0.72f, 1.0f,  1f); // lila pastel
            case Na: return new Color(1.0f,  0.95f, 0.72f, 1f); // crema
            case Cl: return new Color(0.72f, 1.0f,  0.78f, 1f); // verde menta
            default: return type.getColor();
        }
    }

    private static Color getMetallic(AtomType type) {
        switch (type) {
            case H:  return new Color(0.75f, 0.85f, 0.95f, 1f); // plata azulada
            case O:  return new Color(0.85f, 0.25f, 0.20f, 1f); // rojo acero
            case C:  return new Color(0.45f, 0.45f, 0.50f, 1f); // grafito
            case N:  return new Color(0.60f, 0.45f, 0.80f, 1f); // titanio violeta
            case Na: return new Color(0.90f, 0.80f, 0.30f, 1f); // dorado
            case Cl: return new Color(0.40f, 0.78f, 0.40f, 1f); // bronce verde
            default: return type.getColor();
        }
    }

    private static Color getGalaxy(AtomType type) {
        switch (type) {
            case H:  return new Color(0.30f, 0.55f, 1.0f,  1f); // nebulosa azul
            case O:  return new Color(0.85f, 0.15f, 0.55f, 1f); // nebulosa rosa
            case C:  return new Color(0.20f, 0.20f, 0.45f, 1f); // espacio profundo
            case N:  return new Color(0.65f, 0.25f, 0.90f, 1f); // púrpura galaxia
            case Na: return new Color(1.0f,  0.75f, 0.20f, 1f); // estrella dorada
            case Cl: return new Color(0.15f, 0.85f, 0.65f, 1f); // aurora
            default: return type.getColor();
        }
    }
}
