package com.quimimatch.board;

import java.util.Random;

public class MatchBoard {

    public static final int ROWS = 7;
    public static final int COLS = 7;

    private Tile[][] tiles;
    private AtomType[] availableAtoms;
    private Random random;

    public MatchBoard(AtomType[] availableAtoms) {
        this.availableAtoms = availableAtoms;
        this.random = new Random();
        this.tiles  = new Tile[ROWS][COLS];
        generateBoard();
    }

    // -------------------------------------------------------
    // Generación inicial — sin matches al comenzar
    // -------------------------------------------------------
    private void generateBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                AtomType atom = randomAtomWithoutMatch(row, col);
                tiles[row][col] = new Tile(atom, row, col);
            }
        }
    }

    /**
     * Elige un átomo aleatorio que no genere un match-3 inmediato
     * al ser colocado en [row][col].
     */
    private AtomType randomAtomWithoutMatch(int row, int col) {
        AtomType candidate;
        int attempts = 0;

        do {
            candidate = availableAtoms[random.nextInt(availableAtoms.length)];
            attempts++;
            if (attempts > 50) break; // salvaguarda
        } while (wouldCreateMatch(row, col, candidate));

        return candidate;
    }

    private boolean wouldCreateMatch(int row, int col, AtomType type) {
        // Horizontal: dos a la izquierda
        if (col >= 2
                && tiles[row][col - 1] != null && tiles[row][col - 1].getType() == type
                && tiles[row][col - 2] != null && tiles[row][col - 2].getType() == type) {
            return true;
        }
        // Vertical: dos abajo
        if (row >= 2
                && tiles[row - 1][col] != null && tiles[row - 1][col].getType() == type
                && tiles[row - 2][col] != null && tiles[row - 2][col].getType() == type) {
            return true;
        }
        return false;
    }

    // -------------------------------------------------------
    // Acceso
    // -------------------------------------------------------
    public Tile getTile(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        return tiles[row][col];
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public AtomType[] getAvailableAtoms() {
        return availableAtoms;
    }

    public void setAvailableAtoms(AtomType[] atoms) {
        this.availableAtoms = atoms;
    }

    /**
     * Rellena una celda vacía (null) con un átomo nuevo desde arriba.
     * Se llamará desde Sprint 2 en la fase de "gravedad".
     */
    public void fillEmpty() {
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (tiles[row][col] == null) {
                    AtomType atom = availableAtoms[random.nextInt(availableAtoms.length)];
                    tiles[row][col] = new Tile(atom, row, col);
                    // La ficha "cae" desde arriba → posición visual inicial por encima del tablero
                    tiles[row][col].setY(ROWS * Tile.SIZE);
                }
            }
        }
    }

    /**
     * Intercambia dos fichas en el tablero (usado en Sprint 2).
     */
    public void swap(int r1, int c1, int r2, int c2) {
        Tile tmp = tiles[r1][c1];
        tiles[r1][c1] = tiles[r2][c2];
        tiles[r2][c2] = tmp;

        // Actualizar referencias de fila/columna
        if (tiles[r1][c1] != null) {
            tiles[r1][c1].setRow(r1);
            tiles[r1][c1].setCol(c1);
        }
        if (tiles[r2][c2] != null) {
            tiles[r2][c2].setRow(r2);
            tiles[r2][c2].setCol(c2);
        }
    }
}
