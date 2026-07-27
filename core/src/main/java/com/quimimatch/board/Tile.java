package com.quimimatch.board;

public class Tile {

    private AtomType type;

    private int row;
    private int col;

    // Posición visual en pantalla (píxeles)
    private float x;
    private float y;

    // Para animaciones futuras (caída, swap, etc.)
    private float targetX;
    private float targetY;
    private boolean isAnimating;

    // Estado especial (para Sprint 3B)
    private TileSpecial special = TileSpecial.NONE;

    // Estado especial (para Sprint 3+)
    private boolean isSelected;
    private boolean isMatched;

    public static final int SIZE = 130; // Tamaño en px de cada ficha (reducido de 140)

    public Tile(AtomType type, int row, int col) {
        this.type   = type;
        this.row    = row;
        this.col    = col;
        this.x      = col * SIZE;
        this.y      = row * SIZE;
        this.targetX = this.x;
        this.targetY = this.y;
        this.isAnimating = false;
        this.isSelected  = false;
        this.isMatched   = false;
    }

    // ---------- Getters / Setters ----------

    public AtomType getType()       { return type; }
    public void     setType(AtomType t) { this.type = t; }

    public int   getRow()           { return row; }
    public void  setRow(int r)      { this.row = r; }

    public int   getCol()           { return col; }
    public void  setCol(int c)      { this.col = c; }

    public float getX()             { return x; }
    public void  setX(float x)     { this.x = x; }

    public float getY()             { return y; }
    public void  setY(float y)     { this.y = y; }

    public float getTargetX()       { return targetX; }
    public void  setTargetX(float v){ this.targetX = v; }

    public float getTargetY()       { return targetY; }
    public void  setTargetY(float v){ this.targetY = v; }

    public boolean isAnimating()         { return isAnimating; }
    public void    setAnimating(boolean v){ this.isAnimating = v; }

    public TileSpecial getSpecial()            { return special; }
    public void        setSpecial(TileSpecial s){ this.special = s; }
    public boolean     isSpecial()             { return special != TileSpecial.NONE; }

    public boolean isSelected()         { return isSelected; }
    public void    setSelected(boolean v){ this.isSelected = v; }

    public boolean isMatched()          { return isMatched; }
    public void    setMatched(boolean v){ this.isMatched = v; }
}
