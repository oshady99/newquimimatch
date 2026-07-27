package com.quimimatch.board;

/**
 * Sprint 3B — Tipos de ficha especial.
 * NONE    = ficha normal
 * LINE_H  = bomba horizontal (match 4 en fila)   → elimina fila entera
 * LINE_V  = bomba vertical   (match 4 en col)    → elimina columna entera
 * AREA    = explosiva        (match L o T)        → elimina área 3x3
 * WILDCARD= comodín          (match 5)            → elimina todos del mismo tipo
 */
public enum TileSpecial {
    NONE,
    LINE_H,
    LINE_V,
    AREA,
    WILDCARD
}
