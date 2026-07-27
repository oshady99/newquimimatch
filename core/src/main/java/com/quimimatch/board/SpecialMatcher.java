package com.quimimatch.board;

import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 3B — Detecta matches especiales (4, 5, L, T)
 * y decide qué ficha especial crear en la posición del swap.
 */
public class SpecialMatcher {

    public static class MatchGroup {
        public List<Tile> tiles     = new ArrayList<>();
        public TileSpecial special  = TileSpecial.NONE;
        public int pivotRow = -1;   // dónde crear la ficha especial
        public int pivotCol = -1;
    }

    /**
     * Analiza el tablero y devuelve grupos de matches con su tipo especial.
     * La posición pivot es donde se coloca la ficha especial resultante.
     */
    public static List<MatchGroup> findMatchGroups(Tile[][] tiles) {
        boolean[][] used = new boolean[MatchBoard.ROWS][MatchBoard.COLS];
        List<MatchGroup> groups = new ArrayList<>();

        // ── Buscar horizontales ──────────────────────────────────
        for (int row = 0; row < MatchBoard.ROWS; row++) {
            int col = 0;
            while (col < MatchBoard.COLS - 2) {
                Tile t = tiles[row][col];
                if (t == null) { col++; continue; }

                AtomType type = t.getType();
                int end = col + 1;
                while (end < MatchBoard.COLS
                        && tiles[row][end] != null
                        && tiles[row][end].getType() == type) end++;

                int len = end - col;
                if (len >= 3) {
                    MatchGroup g = new MatchGroup();
                    for (int c = col; c < end; c++) {
                        g.tiles.add(tiles[row][c]);
                        used[row][c] = true;
                    }
                    g.pivotRow = row;
                    g.pivotCol = col + len / 2;

                    if      (len >= 5) g.special = TileSpecial.WILDCARD;
                    else if (len == 4) g.special = TileSpecial.LINE_H;
                    else               g.special = TileSpecial.NONE;

                    groups.add(g);
                }
                col = end;
            }
        }

        // ── Buscar verticales ────────────────────────────────────
        for (int col = 0; col < MatchBoard.COLS; col++) {
            int row = 0;
            while (row < MatchBoard.ROWS - 2) {
                Tile t = tiles[row][col];
                if (t == null) { row++; continue; }

                AtomType type = t.getType();
                int end = row + 1;
                while (end < MatchBoard.ROWS
                        && tiles[end][col] != null
                        && tiles[end][col].getType() == type) end++;

                int len = end - row;
                if (len >= 3) {
                    MatchGroup g = new MatchGroup();
                    for (int r = row; r < end; r++) {
                        // Evitar duplicados con matches horizontales
                        if (!used[r][col]) g.tiles.add(tiles[r][col]);
                    }
                    if (!g.tiles.isEmpty()) {
                        g.pivotRow = row + len / 2;
                        g.pivotCol = col;

                        if      (len >= 5) g.special = TileSpecial.WILDCARD;
                        else if (len == 4) g.special = TileSpecial.LINE_V;
                        else               g.special = TileSpecial.NONE;

                        // ── Detectar L o T ───────────────────────
                        // Si existe un grupo horizontal que comparte tiles con este vertical
                        for (MatchGroup prev : groups) {
                            if (sharesAtom(prev, g)) {
                                // Forma L o T → AREA
                                prev.special = TileSpecial.AREA;
                                g.special    = TileSpecial.NONE; // absorber en el anterior
                                // Combinar tiles
                                for (Tile tile : g.tiles) {
                                    if (!prev.tiles.contains(tile)) prev.tiles.add(tile);
                                }
                                g.tiles.clear();
                                break;
                            }
                        }

                        if (!g.tiles.isEmpty()) groups.add(g);
                    }
                }
                row = end;
            }
        }

        return groups;
    }

    private static boolean sharesAtom(MatchGroup a, MatchGroup b) {
        for (Tile ta : a.tiles)
            for (Tile tb : b.tiles)
                if (ta == tb) return true;
        return false;
    }
}
