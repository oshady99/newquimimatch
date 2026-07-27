package com.quimimatch;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.quimimatch.screens.SplashScreen;
import com.quimimatch.managers.SaveManager;
import com.quimimatch.managers.AssetLoader;

public class QuimiMatchGame extends Game {

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        SaveManager.loadAll();
        AssetLoader.get().loadAll(); // Cargar assets gráficos // Cargar datos guardados
        setScreen(new SplashScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        AssetLoader.get().dispose();
        super.dispose();
    }
}
