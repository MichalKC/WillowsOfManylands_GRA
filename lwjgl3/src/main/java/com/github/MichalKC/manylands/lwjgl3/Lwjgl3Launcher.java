package com.github.MichalKC.manylands.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.MichalKC.manylands.GdxGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;

        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new GdxGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Manylands");
        configuration.setBackBufferConfig(8, 8, 8, 8, 16, 8, 0);
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode((int) (GdxGame.WORLD_WIDTH * 100f), (int) (GdxGame.WORLD_HEIGHT * 100f));
        configuration.setWindowIcon("Manylands.png", "Manylands.png", "Manylands.png", "Manylands.png");

        // Enable ANGLE for better compatibility with Windows drivers
        //configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
        return configuration;
    }
}
