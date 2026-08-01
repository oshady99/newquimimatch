package com.quimimatch.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.quimimatch.managers.Compound;

/**
 * Capa cinematográfica mostrada antes de iniciar una Expedición.
 *
 * No cambia de Screen: se dibuja encima de RunnerScreen.
 */
public class MissionIntroOverlay {

    private static final float TOTAL_DURATION = 7f;
    private static final float MIN_SKIP_TIME = 1f;

    private final Compound compound;

    private float elapsed;
    private boolean finished;

    public MissionIntroOverlay(Compound compound) {
        this.compound = compound;
        this.elapsed = 0f;
        this.finished = false;
    }

    /**
     * Actualiza la cinemática.
     */
    public void update(float delta) {
        if (finished) {
            return;
        }

        elapsed += delta;

        boolean skipRequested =
            elapsed >= MIN_SKIP_TIME && Gdx.input.justTouched();

        if (elapsed >= TOTAL_DURATION || skipRequested) {
            finished = true;
        }
    }

    /**
     * Dibuja la introducción cinematográfica.
     */
    public void draw(
        SpriteBatch batch,
        ShapeRenderer shapeRenderer,
        BitmapFont font,
        BitmapFont fontBig,
        BitmapFont fontSmall,
        GlyphLayout layout,
        float screenWidth,
        float screenHeight
    ) {
        float fadeAlpha = calculateFadeAlpha();

        drawBackground(
            shapeRenderer,
            screenWidth,
            screenHeight,
            fadeAlpha
        );

        drawHolographicPanel(
            shapeRenderer,
            screenWidth,
            screenHeight,
            fadeAlpha
        );

        drawText(
            batch,
            font,
            fontBig,
            fontSmall,
            layout,
            screenWidth,
            screenHeight,
            fadeAlpha
        );
    }

    private void drawBackground(
        ShapeRenderer shapeRenderer,
        float screenWidth,
        float screenHeight,
        float alpha
    ) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Oscurece el Runner sin ocultarlo completamente.
        shapeRenderer.setColor(0.01f, 0.02f, 0.08f, 0.94f * alpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);

        // Líneas holográficas horizontales.
        shapeRenderer.setColor(0.1f, 0.65f, 1f, 0.10f * alpha);

        for (float y = 0; y < screenHeight; y += 42f) {
            shapeRenderer.rect(0, y, screenWidth, 2f);
        }

        // Escáner animado.
        float scanProgress = Math.min(elapsed / 2f, 1f);
        float scanY = screenHeight * (1f - scanProgress);

        shapeRenderer.setColor(0.2f, 0.85f, 1f, 0.75f * alpha);
        shapeRenderer.rect(0, scanY, screenWidth, 7f);

        shapeRenderer.setColor(0.2f, 0.85f, 1f, 0.18f * alpha);
        shapeRenderer.rect(0, scanY - 35f, screenWidth, 70f);

        shapeRenderer.end();
    }

    private void drawHolographicPanel(
        ShapeRenderer shapeRenderer,
        float screenWidth,
        float screenHeight,
        float alpha
    ) {
        float panelWidth = screenWidth * 0.78f;
        float panelHeight = screenHeight * 0.74f;

        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = (screenHeight - panelHeight) / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Sombra exterior.
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f * alpha);
        shapeRenderer.rect(
            panelX + 18f,
            panelY - 18f,
            panelWidth,
            panelHeight
        );

        // Panel principal.
        shapeRenderer.setColor(0.035f, 0.08f, 0.20f, 0.97f * alpha);
        shapeRenderer.rect(
            panelX,
            panelY,
            panelWidth,
            panelHeight
        );

        // Bordes superior e inferior.
        shapeRenderer.setColor(0.15f, 0.75f, 1f, alpha);
        shapeRenderer.rect(
            panelX,
            panelY + panelHeight - 10f,
            panelWidth,
            10f
        );

        shapeRenderer.rect(
            panelX,
            panelY,
            panelWidth,
            7f
        );

        // Núcleo holográfico izquierdo.
        float moleculeX = panelX + panelWidth * 0.17f;
        float moleculeY = panelY + panelHeight * 0.50f;

        float pulse =
            1f + (float) Math.sin(elapsed * 4f) * 0.08f;

        shapeRenderer.setColor(0.15f, 0.75f, 1f, 0.15f * alpha);
        shapeRenderer.circle(
            moleculeX,
            moleculeY,
            125f * pulse,
            48
        );

        shapeRenderer.setColor(0.25f, 0.90f, 1f, 0.35f * alpha);
        shapeRenderer.circle(
            moleculeX,
            moleculeY,
            80f * pulse,
            40
        );

        shapeRenderer.setColor(0.40f, 1f, 0.80f, alpha);
        shapeRenderer.circle(
            moleculeX,
            moleculeY,
            34f * pulse,
            32
        );

        // Átomos secundarios decorativos.
        shapeRenderer.setColor(1f, 0.65f, 0.20f, alpha);
        shapeRenderer.circle(
            moleculeX - 100f,
            moleculeY + 70f,
            19f,
            24
        );

        shapeRenderer.setColor(0.75f, 0.35f, 1f, alpha);
        shapeRenderer.circle(
            moleculeX + 105f,
            moleculeY + 55f,
            23f,
            24
        );

        shapeRenderer.setColor(1f, 0.30f, 0.30f, alpha);
        shapeRenderer.circle(
            moleculeX + 80f,
            moleculeY - 90f,
            21f,
            24
        );

        shapeRenderer.end();
    }

    private void drawText(
        SpriteBatch batch,
        BitmapFont font,
        BitmapFont fontBig,
        BitmapFont fontSmall,
        GlyphLayout layout,
        float screenWidth,
        float screenHeight,
        float alpha
    ) {
        float centerX = screenWidth * 0.62f;
        float topY = screenHeight * 0.80f;

        batch.begin();

        // Fase 1: encabezado.
        if (elapsed >= 0.5f) {
            fontSmall.setColor(0.35f, 0.85f, 1f, alpha);

            drawCentered(
                batch,
                fontSmall,
                layout,
                "NUEVA EXPEDICION QUIMICA",
                centerX,
                topY
            );
        }

        // Fase 2: tema y nombre.
        if (elapsed >= 1.4f) {
            String theme = compound != null
                ? compound.getTheme()
                : "DESCUBRIMIENTO QUIMICO";

            font.setColor(1f, 0.86f, 0.22f, alpha);

            drawCentered(
                batch,
                font,
                layout,
                theme.toUpperCase(),
                centerX,
                topY - 85f
            );
        }

        if (elapsed >= 2.1f) {
            String name = compound != null
                ? compound.getName()
                : "NUEVA MOLECULA";

            fontBig.setColor(1f, 1f, 1f, alpha);

            drawCentered(
                batch,
                fontBig,
                layout,
                name.toUpperCase(),
                centerX,
                topY - 185f
            );
        }

        // Fase 3: fórmula.
        if (elapsed >= 2.8f) {
            String formula = compound != null
                ? compound.getFormula()
                : "";

            fontBig.setColor(0.35f, 1f, 0.68f, alpha);

            drawCentered(
                batch,
                fontBig,
                layout,
                formula,
                centerX,
                topY - 290f
            );
        }

        // Fase 4: mensaje de Byte.
        if (elapsed >= 3.5f) {
            String byteMessage = compound != null
                ? compound.getDescription()
                : "Recolecta los atomos necesarios para completar la reaccion.";

            fontSmall.setColor(0.78f, 0.86f, 1f, alpha);

            drawCentered(
                batch,
                fontSmall,
                layout,
                "BYTE: " + byteMessage,
                centerX,
                topY - 390f
            );
        }

        // Últimos tres segundos: cuenta regresiva.
        if (elapsed >= 4f) {
            int countdown = Math.max(
                1,
                (int) Math.ceil(TOTAL_DURATION - elapsed)
            );

            fontBig.setColor(1f, 0.68f, 0.18f, alpha);

            drawCentered(
                batch,
                fontBig,
                layout,
                String.valueOf(countdown),
                centerX,
                screenHeight * 0.18f
            );
        }

        fontSmall.setColor(0.55f, 0.65f, 0.85f, 0.85f * alpha);

        drawCentered(
            batch,
            fontSmall,
            layout,
            "Toca para omitir",
            centerX,
            screenHeight * 0.09f
        );

        batch.end();
    }

    private float calculateFadeAlpha() {
        // Entrada suave.
        if (elapsed < 0.5f) {
            return elapsed / 0.5f;
        }

        // Salida suave.
        if (elapsed > TOTAL_DURATION - 0.5f) {
            return Math.max(
                0f,
                (TOTAL_DURATION - elapsed) / 0.5f
            );
        }

        return 1f;
    }

    private void drawCentered(
        SpriteBatch batch,
        BitmapFont font,
        GlyphLayout layout,
        String text,
        float centerX,
        float y
    ) {
        layout.setText(font, text);

        font.draw(
            batch,
            text,
            centerX - layout.width / 2f,
            y
        );
    }

    public boolean isFinished() {
        return finished;
    }
}
