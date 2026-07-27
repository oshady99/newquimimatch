package com.quimimatch.board;

import com.badlogic.gdx.graphics.Color;

public enum AtomType {

    H("H", new Color(0.4f, 0.7f, 1f, 1f)),      // Azul claro
    O("O", new Color(1f, 0.3f, 0.3f, 1f)),       // Rojo
    C("C", new Color(0.4f, 0.4f, 0.4f, 1f)),     // Gris
    N("N", new Color(0.5f, 0.3f, 0.9f, 1f)),     // Violeta

    // Mundo 2 (no activos aún)
    Na("Na", new Color(1f, 0.85f, 0f, 1f)),      // Amarillo
    Cl("Cl", new Color(0.5f, 0.9f, 0.3f, 1f)),  // Verde

    // Mundo 3
    Ca("Ca", new Color(1f, 0.6f, 0.2f, 1f)),     // Naranja
    Mg("Mg", new Color(0.2f, 0.9f, 0.7f, 1f)),  // Turquesa

    // Mundo 4
    K("K",   new Color(0.9f, 0.4f, 0.7f, 1f)),  // Rosa
    Fe("Fe", new Color(0.6f, 0.2f, 0.1f, 1f)),  // Café rojizo

    // Mundo 5
    S("S",   new Color(0.95f, 0.9f, 0.2f, 1f)), // Amarillo azufre
    P("P",   new Color(0.9f, 0.5f, 0.1f, 1f)),  // Naranja fósforo

    // Mundo 6
    Cu("Cu", new Color(0.7f, 0.45f, 0.2f, 1f)), // Cobre
    Zn("Zn", new Color(0.6f, 0.75f, 0.8f, 1f)); // Zinc azulado

    private final String symbol;
    private final Color color;

    AtomType(String symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }
}
