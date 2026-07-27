package com.quimimatch.board;

import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 3B — Activa fichas especiales y devuelve las fichas eliminadas.
 */
public class SpecialActivator {

    /**
     * Activa una ficha especial en [row][col] y devuelve todas las fichas
     * que deben eliminarse como resultado.
     */
    public static List<Tile> activate(Tile[][] tiles, int row, int col) {
        List<Tile> result = new ArrayList<>();
        Tile t = tiles[row][col];
        if (t == null) return result;

        switch (t.getSpecial()) {

            case LINE_H:
                // Elimina toda la fila
                for (int c = 0; c < MatchBoard.COLS; c++) {
                    if (tiles[row][c] != null) result.add(tiles[row][c]);
                }
                break;

            case LINE_V:
                // Elimina toda la columna
                for (int r = 0; r < MatchBoard.ROWS; r++) {
                    if (tiles[r][col] != null) result.add(tiles[r][col]);
                }
                break;

            case AREA:
                // Elimina área 3x3
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = row + dr, nc = col + dc;
                        if (nr >= 0 && nr < MatchBoard.ROWS
                         && nc >= 0 && nc < MatchBoard.COLS
                         && tiles[nr][nc] != null) {
                            result.add(tiles[nr][nc]);
                        }
                    }
                }
                break;

            case WILDCARD:
                // Elimina todos los átomos del mismo tipo
                AtomType type = t.getType();
                for (int r = 0; r < MatchBoard.ROWS; r++) {
                    for (int c = 0; c < MatchBoard.COLS; c++) {
                        if (tiles[r][c] != null && tiles[r][c].getType() == type) {
                            result.add(tiles[r][c]);
                        }
                    }
                }
                break;

            default:
                break;
        }

        return result;
    }

    /**
     * Activa una ficha especial al ser tocada directamente por el jugador
     * (toque doble sobre ficha especial).
     */
    public static List<Tile> activateByTouch(Tile[][] tiles, Tile special) {
        return activate(tiles, special.getRow(), special.getCol());
    }
}
