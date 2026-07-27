package io.github.Juego_Oshady.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.quimimatch.QuimiMatchGame;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config =
            new AndroidApplicationConfiguration();

        initialize(new QuimiMatchGame(), config);
    }
}
