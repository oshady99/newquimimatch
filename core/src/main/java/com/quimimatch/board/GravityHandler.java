package com.quimimatch.board;

import java.util.Random;

/**
 * Sprint 2 — Aplica gravedad al tablero:
 * las fichas caen hacia abajo (fila mayor = abajo visualmente)
 * y rellena los huecos con fichas nuevas desde arriba.
 */
public class GravityHandler {

    private Random random = new Random();

    /**
     * Hace caer todas las fichas hacia la fila más alta (abajo en pantalla).
     * En nuestra convención: fila 7 = fondo, fila 0 = techo.
     */
    public void applyGravity(Tile[][] tiles) {
        for (int col = 0; col < MatchBoard.COLS; col++) {
            // Recorremos de abajo (fila 7) hacia arriba (fila 0)
            for (int row = MatchBoard.ROWS - 1; row >= 0; row--) {
                if (tiles[row][col] == null) {
                    // Buscar la primera ficha no nula por encima
                    for (int above = row - 1; above >= 0; above--) {
                        if (tiles[above][col] != null) {
                            // Mover esa ficha hacia abajo
                            tiles[row][col] = tiles[above][col];
                            tiles[above][col] = null;
                            tiles[row][col].setRow(row);
                            tiles[row][col].setCol(col);
                            // Animación: ficha empieza visualmente desde arriba
                            tiles[row][col].setY(tiles[row][col].getY() + Tile.SIZE);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Rellena los huecos que quedaron en la parte superior con fichas nuevas.
     */
    public void fillEmpty(Tile[][] tiles, AtomType[] availableAtoms) {
        for (int col = 0; col < MatchBoard.COLS; col++) {
            for (int row = 0; row < MatchBoard.ROWS; row++) {
                if (tiles[row][col] == null) {
                    AtomType atom = availableAtoms[random.nextInt(availableAtoms.length)];
                    tiles[row][col] = new Tile(atom, row, col);
                    // Empieza por encima del tablero para animación de caída
                    tiles[row][col].setY((MatchBoard.ROWS + (row)) * Tile.SIZE);
                }
            }
        }
    }
}
