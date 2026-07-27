package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.board.AtomType;
import com.quimimatch.board.GravityHandler;
import com.quimimatch.board.MatchBoard;
import com.quimimatch.board.MatchDetector;
import com.quimimatch.board.Tile;
import com.quimimatch.board.SpecialMatcher;
import com.quimimatch.board.SpecialActivator;
import com.quimimatch.board.TileSpecial;
import com.quimimatch.managers.GameSession;
import com.quimimatch.managers.LevelConfig;
import com.quimimatch.managers.PlayerInventory;
import com.quimimatch.managers.SkinPalette;
import com.quimimatch.managers.AssetLoader;
import com.quimimatch.managers.AudioManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import java.util.ArrayList;
import java.util.List;

public class Match3Screen implements Screen {

    private final QuimiMatchGame game;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont fontSmall;
    private GlyphLayout layout;

    private MatchBoard board;
    private GravityHandler gravity;

    private static final int TILE = Tile.SIZE;
    private static final int GAP  = 4;

    private float boardOffsetX;
    private float boardOffsetY;

    // Estado de selección
    private Tile selectedTile = null;

    // Máquina de estados
    private enum BoardState { IDLE, SWAPPING, SWAP_BACK, RESOLVING, FALLING, GAME_OVER, BOMB_SELECT }
    private BoardState state = BoardState.IDLE;

    private Tile swapA, swapB;
    private float swapTimer = 0f;
    private float fallTimer = 0f;
    private static final float SWAP_DURATION = 0.18f;
    private static final float FALL_DURATION = 0.22f;

    // Panel educativo lateral
    private MoleculePanel moleculePanel;

    // Partículas y efectos
    private ParticleSystem particles;
    private int comboCount = 0;

    // Power-up state
    private boolean wildcardMode = false; // esperando toque para aplicar comodín

    // Mensaje flotante
    private String floatMsg   = "";
    private float  floatTimer = 0f;

    // Delay antes de ir a victoria/derrota
    private float endDelay = 0f;
    private boolean goVictory = false;

    // Colores
    private static final Color COLOR_BG       = new Color(0.08f, 0.08f, 0.18f, 1f);
    private static final Color COLOR_CELL_BG  = new Color(0.15f, 0.15f, 0.30f, 1f);
    private static final Color COLOR_SELECTED = new Color(1f,    1f,    0.2f,  1f);
    private static final Color COLOR_HUD_BG   = new Color(0.10f, 0.10f, 0.25f, 1f);

    public Match3Screen(QuimiMatchGame game) {
        this.game     = game;
        shapeRenderer = new ShapeRenderer();
        font          = new BitmapFont();
        font.getData().setScale(2.4f);
        fontSmall     = new BitmapFont();
        fontSmall.getData().setScale(1.7f);
        layout        = new GlyphLayout();

        // Usar átomos del nivel actual
        LevelConfig cfg = GameSession.get().getConfig();
        board   = new MatchBoard(cfg.atoms);
        gravity = new GravityHandler();

        recalcOffsets(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        snapAllTilesToGrid();
        moleculePanel = new MoleculePanel();
        particles     = new ParticleSystem();
        AudioManager.get().muteMusic(); // Tablero: solo efectos, sin música
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(COLOR_BG.r, COLOR_BG.g, COLOR_BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackground();
        update(delta);
        drawBoard();
        drawHUD();
        // drawMax(); // Eliminado de bg_lab
        particles.update(delta);
        particles.draw(shapeRenderer, game.batch);
        moleculePanel.update(delta);
        moleculePanel.draw(shapeRenderer, game.batch);
        drawFloatMsg(delta);
    }

    private void drawBackground() {
        AssetLoader assets = AssetLoader.get();
        int world = GameSession.get().getConfig().world;
        if (assets.hasLabBackground(world)) {
            game.batch.begin();
            // Oscurecer un poco el fondo para que resalte el tablero
            game.batch.setColor(0.6f, 0.6f, 0.6f, 1f);
            game.batch.draw(assets.getLabBackground(world), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.batch.setColor(Color.WHITE);
            game.batch.end();
        }
    }

    // ── Lógica ──────────────────────────────────────────────────

    private void update(float delta) {

        // Espera antes de cambiar pantalla
        if (state == BoardState.GAME_OVER) {
            endDelay -= delta;
            if (endDelay <= 0) {
                if (goVictory) game.setScreen(new VictoryScreen(game));
                else           game.setScreen(new DefeatScreen(game));
            }
            return;
        }

        switch (state) {

            case IDLE:
                handleInput();
                break;

            case BOMB_SELECT:
                // Waiting for tile tap to apply bomb
                if (Gdx.input.justTouched()) {
                    float tx = Gdx.input.getX();
                    float ty = Gdx.graphics.getHeight() - Gdx.input.getY();
                    Tile touched = tileAt(tx, ty);
                    if (touched != null) {
                        PlayerInventory.get().useBomb();
                        // Explotar area 3x3
                        List<Tile> toRemove = new ArrayList<>();
                        Tile[][] tiles = board.getTiles();
                        int br = touched.getRow(), bc = touched.getCol();
                        for (int dr = -1; dr <= 1; dr++)
                            for (int dc = -1; dc <= 1; dc++) {
                                int nr = br+dr, nc = bc+dc;
                                if (nr>=0&&nr<MatchBoard.ROWS&&nc>=0&&nc<MatchBoard.COLS&&tiles[nr][nc]!=null)
                                    toRemove.add(tiles[nr][nc]);
                            }
                        // Explosion particles
                        float px = boardOffsetX + bc*(TILE+GAP)+TILE/2f;
                        float py = boardOffsetY + (MatchBoard.ROWS-1-br)*(TILE+GAP)+TILE/2f;
                        particles.addFlash(px, py, TILE*1.5f, new Color(1f,0.5f,0.1f,1f));
                        resolveMatches(toRemove);
                        GameSession.get().useMove();
                        state = BoardState.RESOLVING;
                    } else {
                        state = BoardState.IDLE;
                        showFloat("Bomba cancelada");
                    }
                }
                break;

            case SWAPPING:
                swapTimer += delta;
                animateSwap(swapTimer / SWAP_DURATION);
                if (swapTimer >= SWAP_DURATION) {
                    finalizeSwap();
                    List<Tile> matches = MatchDetector.findMatches(board.getTiles());
                    if (matches.isEmpty()) {
                        state     = BoardState.SWAP_BACK;
                        swapTimer = 0f;
                        showFloat("Sin match");
                        comboCount = 0;
                        board.swap(swapB.getRow(), swapB.getCol(),
                                   swapA.getRow(), swapA.getCol());
                        Tile tmp = swapA; swapA = swapB; swapB = tmp;
                    } else {
                        GameSession.get().useMove();
                        resolveMatches(matches);
                    }
                }
                break;

            case SWAP_BACK:
                swapTimer += delta;
                animateSwap(swapTimer / SWAP_DURATION);
                if (swapTimer >= SWAP_DURATION) {
                    finalizeSwap();
                    state = BoardState.IDLE;
                }
                break;

            case RESOLVING:
                fallTimer += delta;
                animateFall(fallTimer / FALL_DURATION);
                if (fallTimer >= FALL_DURATION) {
                    snapFall();
                    List<Tile> cascade = MatchDetector.findMatches(board.getTiles());
                    if (!cascade.isEmpty()) {
                        resolveMatches(cascade);
                    } else {
                        checkEndCondition();
                    }
                }
                break;

            case FALLING:
                fallTimer += delta;
                animateFall(fallTimer / FALL_DURATION);
                if (fallTimer >= FALL_DURATION) {
                    snapFall();
                    checkEndCondition();
                }
                break;
        }
    }

    private void checkEndCondition() {
        GameSession gs = GameSession.get();
        if (gs.isVictory()) {
            moleculePanel.show(gs.getConfig().moleculeName);
            showFloat("¡MOLECULA COMPLETA!");
            AudioManager.get().playVictory();
            state    = BoardState.GAME_OVER;
            endDelay = 2.5f;
            goVictory = true;
        } else if (gs.isDefeat()) {
            showFloat("Sin movimientos...");
            AudioManager.get().playDefeat();
            state    = BoardState.GAME_OVER;
            endDelay = 1.5f;
            goVictory = false;
        } else {
            state = BoardState.IDLE;
        }
    }

    // ── Input ────────────────────────────────────────────────────

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;

        float tx = Gdx.input.getX();
        float ty = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Check powerup buttons first
        handlePowerupInput(tx, ty);
        if (state != BoardState.IDLE) return;

        Tile touched = tileAt(tx, ty);
        if (touched == null) { selectedTile = null; return; }

        if (selectedTile == null) {
            selectedTile = touched;
            touched.setSelected(true);
        } else if (touched == selectedTile) {
            selectedTile.setSelected(false);
            selectedTile = null;
        } else {
            int dr = Math.abs(touched.getRow() - selectedTile.getRow());
            int dc = Math.abs(touched.getCol() - selectedTile.getCol());
            if (dr + dc == 1) {
                selectedTile.setSelected(false);
                startSwap(selectedTile, touched);
                selectedTile = null;
            } else {
                selectedTile.setSelected(false);
                selectedTile = touched;
                touched.setSelected(true);
            }
        }
    }

    // ── Swap ─────────────────────────────────────────────────────

    private void startSwap(Tile a, Tile b) {
        swapA = a; swapB = b;
        swapTimer = 0f;
        state = BoardState.SWAPPING;
        board.swap(a.getRow(), a.getCol(), b.getRow(), b.getCol());
        AudioManager.get().playSwap();
    }

    private void animateSwap(float t) {
        t = Math.min(t, 1f);
        float eased = easeInOut(t);

        float axTarget = boardOffsetX + swapA.getCol() * (TILE + GAP);
        float ayTarget = boardOffsetY + (MatchBoard.ROWS - 1 - swapA.getRow()) * (TILE + GAP);
        float bxTarget = boardOffsetX + swapB.getCol() * (TILE + GAP);
        float byTarget = boardOffsetY + (MatchBoard.ROWS - 1 - swapB.getRow()) * (TILE + GAP);

        float axFrom = bxTarget, ayFrom = byTarget;
        float bxFrom = axTarget, byFrom = ayTarget;

        swapA.setX(axFrom + (axTarget - axFrom) * eased);
        swapA.setY(ayFrom + (ayTarget - ayFrom) * eased);
        swapB.setX(bxFrom + (bxTarget - bxFrom) * eased);
        swapB.setY(byFrom + (byTarget - byFrom) * eased);
    }

    private void finalizeSwap() {
        snapTileToGrid(swapA);
        snapTileToGrid(swapB);
    }

    // ── Resolución ───────────────────────────────────────────────

    private void resolveMatches(List<Tile> matches) {
        // Detectar grupos con especiales
        List<SpecialMatcher.MatchGroup> groups =
                SpecialMatcher.findMatchGroups(board.getTiles());

        int pts = 0;
        GameSession gs = GameSession.get();

        // Recolectar todas las tiles a eliminar
        List<Tile> toRemove = new ArrayList<>(matches);

        // Activar fichas especiales que estén dentro del match
        for (Tile t : matches) {
            if (t.isSpecial()) {
                List<Tile> extra = SpecialActivator.activate(
                        board.getTiles(), t.getRow(), t.getCol());
                for (Tile e : extra) {
                    if (!toRemove.contains(e)) toRemove.add(e);
                }
            }
        }

        // Eliminar tiles y generar partículas
        for (Tile t : toRemove) {
            if (board.getTiles()[t.getRow()][t.getCol()] == t) {
                board.getTiles()[t.getRow()][t.getCol()] = null;
            }
            t.setMatched(true);
            pts += 100;
            gs.collectAtom(t.getType());

            // Explosión de partículas en posición de la ficha
            float px = boardOffsetX + t.getCol() * (TILE + GAP) + TILE / 2f;
            float py = boardOffsetY + (MatchBoard.ROWS - 1 - t.getRow()) * (TILE + GAP) + TILE / 2f;
            particles.addExplosion(px, py, t.getType().getColor(), 8);
            particles.addScoreText(px, py + TILE / 2f, 100, t.getType().getColor());
        }

        // Crear fichas especiales en pivot de cada grupo
        for (SpecialMatcher.MatchGroup g : groups) {
            if (g.special != TileSpecial.NONE && g.pivotRow >= 0) {
                // Solo si la celda fue eliminada (es null ahora)
                if (board.getTiles()[g.pivotRow][g.pivotCol] == null) {
                    // Obtener tipo del primer átomo del grupo
                    AtomType type = g.tiles.isEmpty()
                            ? gs.getConfig().atoms[0]
                            : g.tiles.get(0).getType();
                    Tile special = new Tile(type, g.pivotRow, g.pivotCol);
                    special.setSpecial(g.special);
                    board.getTiles()[g.pivotRow][g.pivotCol] = special;
                    snapTileToGrid(special);
                    pts += 500;
                    // Flash visual al crear especial
                    float fx = boardOffsetX + g.pivotCol * (TILE + GAP) + TILE / 2f;
                    float fy = boardOffsetY + (MatchBoard.ROWS - 1 - g.pivotRow) * (TILE + GAP) + TILE / 2f;
                    particles.addFlash(fx, fy, TILE / 2f, Color.WHITE);
                    particles.addScoreText(fx, fy + TILE, 500, Color.YELLOW);
                    AudioManager.get().playSpecial();
                    showFloat(specialName(g.special) + " creada! +" + 500);
                }
            }
        }

        gs.addScore(pts);
        AudioManager.get().playMatch();
        if (comboCount >= 2) AudioManager.get().playCascade();
        if (groups.stream().noneMatch(g -> g.special != TileSpecial.NONE)) {
            showFloat("+" + pts + "  (" + toRemove.size() + " atomos)");
        }

        // Combo
        comboCount++;
        if (comboCount >= 2) {
            particles.addComboText(
                Gdx.graphics.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f,
                comboCount);
        }

        gravity.applyGravity(board.getTiles());
        gravity.fillEmpty(board.getTiles(), gs.getConfig().atoms);

        // Snap posición de fichas especiales que sobrevivieron
        Tile[][] tiles = board.getTiles();
        for (int r = 0; r < MatchBoard.ROWS; r++)
            for (int c = 0; c < MatchBoard.COLS; c++)
                if (tiles[r][c] != null && tiles[r][c].isSpecial())
                    snapTileToGrid(tiles[r][c]);

        fallTimer = 0f;
        state     = BoardState.RESOLVING;
    }

    private String specialName(TileSpecial s) {
        switch (s) {
            case LINE_H:   return "BOMBA FILA";
            case LINE_V:   return "BOMBA COLUMNA";
            case AREA:     return "EXPLOSION 3x3";
            case WILDCARD: return "COMODIN";
            default:       return "";
        }
    }

    // ── Animación de caída ───────────────────────────────────────

    private void animateFall(float t) {
        t = Math.min(t, 1f);
        Tile[][] tiles = board.getTiles();
        for (int row = 0; row < MatchBoard.ROWS; row++) {
            for (int col = 0; col < MatchBoard.COLS; col++) {
                Tile tile = tiles[row][col];
                if (tile == null) continue;
                float targetY = boardOffsetY + (MatchBoard.ROWS - 1 - row) * (TILE + GAP);
                tile.setY(tile.getY() + (targetY - tile.getY()) * easeInOut(t));
            }
        }
    }

    private void snapFall() {
        Tile[][] tiles = board.getTiles();
        for (int row = 0; row < MatchBoard.ROWS; row++)
            for (int col = 0; col < MatchBoard.COLS; col++)
                if (tiles[row][col] != null) snapTileToGrid(tiles[row][col]);
    }

    // ── Dibujo ──────────────────────────────────────────────────

    private void drawBoard() {
        Tile[][] tiles = board.getTiles();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int row = 0; row < MatchBoard.ROWS; row++) {
            for (int col = 0; col < MatchBoard.COLS; col++) {
                float cx = boardOffsetX + col * (TILE + GAP);
                float cy = boardOffsetY + (MatchBoard.ROWS - 1 - row) * (TILE + GAP);

                shapeRenderer.setColor(COLOR_CELL_BG);
                shapeRenderer.rect(cx, cy, TILE, TILE);

                Tile tile = tiles[row][col];
                if (tile == null || tile.isMatched()) continue;

                float px = tile.getX(), py = tile.getY();
                float r  = TILE / 2f - 5;
                float ox = px + TILE / 2f, oy = py + TILE / 2f;

                // Solo dibujar círculo si no hay textura
                if (!AssetLoader.get().hasAtom(tile.getType().getSymbol())) {
                    shapeRenderer.setColor(0f, 0f, 0f, 0.35f);
                    shapeRenderer.circle(ox + 2, oy - 2, r, 36);

                    shapeRenderer.setColor(SkinPalette.getColor(tile.getType()));
                    shapeRenderer.circle(ox, oy, r, 36);

                    shapeRenderer.setColor(1f, 1f, 1f, 0.22f);
                    shapeRenderer.circle(ox - r * 0.28f, oy + r * 0.28f, r * 0.32f, 20);
                }

                // ── Visual de ficha especial ──────────────────────
                if (tile.isSpecial()) {
                    drawSpecialIndicator(shapeRenderer, tile, ox, oy, r);
                }

                if (tile.isSelected()) {
                    shapeRenderer.setColor(COLOR_SELECTED);
                    shapeRenderer.rect(px, py, TILE, 3);
                    shapeRenderer.rect(px, py + TILE - 3, TILE, 3);
                    shapeRenderer.rect(px, py, 3, TILE);
                    shapeRenderer.rect(px + TILE - 3, py, 3, TILE);
                }
            }
        }
        shapeRenderer.end();

        game.batch.begin();
        AssetLoader assets = AssetLoader.get();
        for (int row = 0; row < MatchBoard.ROWS; row++) {
            for (int col = 0; col < MatchBoard.COLS; col++) {
                Tile tile = tiles[row][col];
                if (tile == null || tile.isMatched()) continue;

                String sym = tile.getType().getSymbol();

                // Dibujar textura de átomo si existe
                if (assets.hasAtom(sym)) {
                    Texture tex = assets.getAtom(sym);
                    game.batch.draw(tex, tile.getX() + 4, tile.getY() + 4, TILE - 8, TILE - 8);
                } else {
                    layout.setText(font, sym);
                    float tx = tile.getX() + (TILE - layout.width)  / 2f;
                    float ty = tile.getY() + (TILE + layout.height) / 2f;
                    font.setColor(0f, 0f, 0f, 0.5f);
                    font.draw(game.batch, sym, tx + 1, ty - 1);
                    font.setColor(Color.WHITE);
                    font.draw(game.batch, sym, tx, ty);
                }
            }
        }
        game.batch.end();
    }

    /** Dibuja el anillo/indicador visual de una ficha especial */
    private void drawSpecialIndicator(ShapeRenderer sr, Tile tile,
                                       float cx, float cy, float r) {
        switch (tile.getSpecial()) {
            case LINE_H:
                // Anillo dorado + línea horizontal
                sr.setColor(1f, 0.85f, 0f, 0.9f);
                sr.rectLine(cx - r, cy, cx + r, cy, 4f);
                break;
            case LINE_V:
                // Anillo dorado + línea vertical
                sr.setColor(1f, 0.85f, 0f, 0.9f);
                sr.rectLine(cx, cy - r, cx, cy + r, 4f);
                break;
            case AREA:
                // Cruz naranja
                sr.setColor(1f, 0.5f, 0f, 0.9f);
                sr.rectLine(cx - r, cy, cx + r, cy, 3f);
                sr.rectLine(cx, cy - r, cx, cy + r, 3f);
                break;
            case WILDCARD:
                // Estrella blanca (círculo interior brillante)
                sr.setColor(1f, 1f, 1f, 0.8f);
                sr.circle(cx, cy, r * 0.35f, 20);
                break;
            default:
                break;
        }
    }

    // Power-up button positions
    private float pupBtnY  = 14f;
    private float pupSize  = 72f;

    private float pupBtnX(int idx) {
        float sw = Gdx.graphics.getWidth();
        return sw - (3 - idx) * (pupSize + 12) - 10;
    }

    private void handlePowerupInput(float tx, float ty) {
        if (state != BoardState.IDLE) return;
        PlayerInventory inv = PlayerInventory.get();

        // +Movimientos
        if (tx >= pupBtnX(0) && tx <= pupBtnX(0) + pupSize
         && ty >= pupBtnY   && ty <= pupBtnY + pupSize) {
            if (inv.useMoves()) {
                GameSession.get().addMoves(5);
                showFloat("+5 movimientos!");
                particles.addScoreText(
                    Gdx.graphics.getWidth() / 2f,
                    Gdx.graphics.getHeight() / 2f, 0,
                    new Color(0.2f, 0.8f, 0.4f, 1f));
            } else { showFloat("Sin +Movimientos"); }
            return;
        }

        // Bomba 3x3 — modo selección de celda
        if (tx >= pupBtnX(1) && tx <= pupBtnX(1) + pupSize
         && ty >= pupBtnY   && ty <= pupBtnY + pupSize) {
            if (inv.getBombs() > 0) {
                wildcardMode = false;
                showFloat("Toca donde lanzar la bomba!");
                state = BoardState.BOMB_SELECT;
            } else { showFloat("Sin Bombas"); }
            return;
        }

        // Comodín — modo selección de átomo
        if (tx >= pupBtnX(2) && tx <= pupBtnX(2) + pupSize
         && ty >= pupBtnY   && ty <= pupBtnY + pupSize) {
            if (inv.getWildcards() > 0) {
                wildcardMode = true;
                showFloat("Toca el atomo a eliminar!");
            } else { showFloat("Sin Comodines"); }
            return;
        }

        // Comodín: aplicar en la ficha tocada
        if (wildcardMode) {
            Tile touched = tileAt(tx, ty);
            if (touched != null) {
                wildcardMode = false;
                inv.useWildcard();
                List<Tile> toRemove = new ArrayList<>();
                AtomType target = touched.getType();
                Tile[][] tiles = board.getTiles();
                for (int r = 0; r < MatchBoard.ROWS; r++)
                    for (int c = 0; c < MatchBoard.COLS; c++)
                        if (tiles[r][c] != null && tiles[r][c].getType() == target)
                            toRemove.add(tiles[r][c]);
                resolveMatches(toRemove);
                GameSession.get().useMove();
            }
            return;
        }
    }

    private void drawHUD() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        GameSession gs  = GameSession.get();
        LevelConfig cfg = gs.getConfig();
        PlayerInventory inv = PlayerInventory.get();

        float HUD_H = 140f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_HUD_BG);
        shapeRenderer.rect(0, sh - HUD_H, sw, HUD_H);

        // Vidas
        int lives = gs.getLives();
        for (int i = 0; i < 5; i++) {
            shapeRenderer.setColor(i < lives
                    ? new Color(1f, 0.2f, 0.3f, 1f)
                    : new Color(0.35f, 0.35f, 0.35f, 1f));
            shapeRenderer.circle(20 + i * 34, sh - 28, 13, 24);
        }

        // Barras objetivo
        int numGoals   = cfg.goalAtoms.length;
        float barW     = (sw * 0.55f - (numGoals - 1) * 10f) / numGoals;
        float barH     = 40f;
        float barStartX = 10f;
        float barY      = sh - HUD_H + 8;

        for (int i = 0; i < numGoals; i++) {
            float bx = barStartX + i * (barW + 10);
            shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
            shapeRenderer.rect(bx, barY, barW, barH);
            shapeRenderer.setColor(SkinPalette.getColor(cfg.goalAtoms[i]));
            shapeRenderer.rect(bx, barY, barW * gs.getGoalProgress(i), barH);
        }

        // Botones power-up (esquina inferior derecha)
        Color[] pupColors = {
            inv.getMoves()    > 0 ? new Color(0.2f,  0.75f, 0.35f, 1f) : new Color(0.25f, 0.25f, 0.25f, 1f),
            inv.getBombs()    > 0 ? new Color(1.0f,  0.45f, 0.10f, 1f) : new Color(0.25f, 0.25f, 0.25f, 1f),
            wildcardMode         ? new Color(1.0f,  1.0f,  0.20f, 1f)
            : (inv.getWildcards() > 0 ? new Color(0.65f, 0.25f, 0.95f, 1f) : new Color(0.25f, 0.25f, 0.25f, 1f)),
        };
        for (int i = 0; i < 3; i++) {
            shapeRenderer.setColor(pupColors[i]);
            shapeRenderer.rect(pupBtnX(i), pupBtnY, pupSize, pupSize);
        }

        shapeRenderer.end();

        game.batch.begin();

        // Textos barras
        for (int i = 0; i < numGoals; i++) {
            float bx = barStartX + i * (barW + 10);
            int col  = gs.getCollected()[i];
            int need = cfg.goalAmounts[i];
            font.setColor(col >= need ? Color.GREEN : Color.WHITE);
            font.draw(game.batch, cfg.goalAtoms[i].getSymbol() + " " + col + "/" + need,
                    bx + 6, barY + barH - 4);
        }

        // MOV / PTS / META
        float rightX = sw - 280;
        font.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        font.draw(game.batch, "MOV: " + gs.getMovesLeft(), rightX, sh - 10);
        font.setColor(Color.WHITE);
        font.draw(game.batch, "PTS: " + gs.getScore(),     rightX, sh - 60);
        font.setColor(new Color(0.5f, 0.9f, 1f, 1f));
        font.draw(game.batch, "META: " + cfg.moleculeName, rightX, sh - HUD_H + 46);
        fontSmall.setColor(new Color(0.6f, 0.6f, 0.8f, 1f));
        fontSmall.draw(game.batch, "M" + cfg.world + "-N" + cfg.level, sw - 130, sh - 8);

        // Íconos botones power-up
        String[] icons  = { "+5", "B", "*" };
        int[]    stocks = { inv.getMoves(), inv.getBombs(), inv.getWildcards() };
        for (int i = 0; i < 3; i++) {
            font.setColor(Color.WHITE);
            layout.setText(font, icons[i]);
            font.draw(game.batch, icons[i],
                    pupBtnX(i) + (pupSize - layout.width) / 2f,
                    pupBtnY + pupSize - 8);
            fontSmall.setColor(new Color(1f, 0.9f, 0.3f, 1f));
            fontSmall.draw(game.batch, "x" + stocks[i], pupBtnX(i) + 6, pupBtnY + 22);
        }

        game.batch.end();
    }

    private void drawFloatMsg(float delta) {
        if (floatTimer <= 0) return;
        floatTimer -= delta;
        game.batch.begin();
        float alpha = Math.min(floatTimer / 1.2f, 1f);
        fontSmall.setColor(1f, 1f, 0.3f, alpha);
        layout.setText(fontSmall, floatMsg);
        fontSmall.draw(game.batch, floatMsg,
                (Gdx.graphics.getWidth() - layout.width) / 2f,
                boardOffsetY - 20);
        game.batch.end();
    }

    private void showFloat(String msg) {
        floatMsg   = msg;
        floatTimer = 1.8f;
    }

    // ── Utilidades ───────────────────────────────────────────────

    private Tile tileAt(float screenX, float screenY) {
        Tile[][] tiles = board.getTiles();
        for (int row = 0; row < MatchBoard.ROWS; row++) {
            for (int col = 0; col < MatchBoard.COLS; col++) {
                Tile t = tiles[row][col];
                if (t == null) continue;
                float tx = boardOffsetX + col * (TILE + GAP);
                float ty = boardOffsetY + (MatchBoard.ROWS - 1 - row) * (TILE + GAP);
                if (screenX >= tx && screenX <= tx + TILE
                 && screenY >= ty && screenY <= ty + TILE) return t;
            }
        }
        return null;
    }

    private void snapTileToGrid(Tile t) {
        t.setX(boardOffsetX + t.getCol() * (TILE + GAP));
        t.setY(boardOffsetY + (MatchBoard.ROWS - 1 - t.getRow()) * (TILE + GAP));
    }

    private void snapAllTilesToGrid() {
        Tile[][] tiles = board.getTiles();
        for (int row = 0; row < MatchBoard.ROWS; row++)
            for (int col = 0; col < MatchBoard.COLS; col++)
                if (tiles[row][col] != null) snapTileToGrid(tiles[row][col]);
    }

    private void recalcOffsets(int w, int h) {
        float boardW = MatchBoard.COLS * (TILE + GAP) - GAP;
        float boardH = MatchBoard.ROWS * (TILE + GAP) - GAP;
        boardOffsetX = (w - boardW) / 2f;
        boardOffsetY = (h - boardH) / 2f - 20;
    }

    private float easeInOut(float t) {
        return t * t * (3 - 2 * t);
    }

    @Override public void resize(int w, int h) { recalcOffsets(w, h); snapAllTilesToGrid(); moleculePanel.resize(); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { AudioManager.get().unmuteMusic(); dispose(); }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        fontSmall.dispose();
        moleculePanel.dispose();
        particles.dispose();
    }
}
