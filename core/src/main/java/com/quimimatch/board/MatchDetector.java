package com.quimimatch.board;

import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 2 — Detecta grupos de 3 o más fichas iguales
 * en horizontal y vertical.
 */
public class MatchDetector {

    /**
     * Devuelve todas las fichas que forman un match en el tablero actual.
     * Una ficha puede aparecer una sola vez aunque pertenezca a dos matches.
     */
    public static List<Tile> findMatches(Tile[][] tiles) {

        boolean[][] marked = new boolean[MatchBoard.ROWS][MatchBoard.COLS];
        List<Tile> result   = new ArrayList<>();

        // ── Horizontal ──────────────────────────────────────────
        for (int row = 0; row < MatchBoard.ROWS; row++) {
            int col = 0;
            while (col < MatchBoard.COLS - 2) {

                Tile t = tiles[row][col];
                if (t == null) { col++; continue; }

                AtomType type  = t.getType();
                int      end   = col + 1;

                while (end < MatchBoard.COLS
                        && tiles[row][end] != null
                        && tiles[row][end].getType() == type) {
                    end++;
                }

                if (end - col >= 3) {
                    for (int c = col; c < end; c++) {
                        if (!marked[row][c]) {
                            marked[row][c] = true;
                            result.add(tiles[row][c]);
                        }
                    }
                }
                col = end;
            }
        }

        // ── Vertical ────────────────────────────────────────────
        for (int col = 0; col < MatchBoard.COLS; col++) {
            int row = 0;
            while (row < MatchBoard.ROWS - 2) {

                Tile t = tiles[row][col];
                if (t == null) { row++; continue; }

                AtomType type = t.getType();
                int      end  = row + 1;

                while (end < MatchBoard.ROWS
                        && tiles[end][col] != null
                        && tiles[end][col].getType() == type) {
                    end++;
                }

                if (end - row >= 3) {
                    for (int r = row; r < end; r++) {
                        if (!marked[r][col]) {
                            marked[r][col] = true;
                            result.add(tiles[r][col]);
                        }
                    }
                }
                row = end;
            }
        }

        return result;
    }

    /**
     * Verifica si intercambiar dos fichas adyacentes produciría al menos un match.
     * Usado para validar movimientos del jugador.
     */
    public static boolean isValidSwap(Tile[][] tiles, int r1, int c1, int r2, int c2) {

        // Hacer swap temporal
        swapInPlace(tiles, r1, c1, r2, c2);
        boolean hasMatch = !findMatches(tiles).isEmpty();
        // Deshacer
        swapInPlace(tiles, r1, c1, r2, c2);

        return hasMatch;
    }

    private static void swapInPlace(Tile[][] tiles, int r1, int c1, int r2, int c2) {
        Tile tmp       = tiles[r1][c1];
        tiles[r1][c1]  = tiles[r2][c2];
        tiles[r2][c2]  = tmp;

        if (tiles[r1][c1] != null) { tiles[r1][c1].setRow(r1); tiles[r1][c1].setCol(c1); }
        if (tiles[r2][c2] != null) { tiles[r2][c2].setRow(r2); tiles[r2][c2].setCol(c2); }
    }
}
