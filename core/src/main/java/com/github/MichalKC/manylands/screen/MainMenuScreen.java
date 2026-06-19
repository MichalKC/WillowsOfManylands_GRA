package com.github.MichalKC.manylands.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.MusicAsset;
import com.github.MichalKC.manylands.asset.SkinAsset;
import com.github.MichalKC.manylands.input.KeyboardController;
import com.github.MichalKC.manylands.input.UiControllerState;
import com.github.MichalKC.manylands.ui.model.MenuViewModel;
import com.github.MichalKC.manylands.ui.view.MenuView;

public class MainMenuScreen extends ScreenAdapter {

    private final GdxGame game;
    private final Stage stage;
    private final Skin skin;
    private final Viewport uiViewport;
    private final KeyboardController keyboardController;

    private boolean startFadeActive;
    private float startFadeTimer;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer startShapeRenderer;
    private static final float RESUME_TRANS_OUT = 0.5f;
    private static boolean startupFadePlayed = false;
    private boolean startReachedBlack;
    private float startHoldTimer;
    private static final float START_FADE_HOLD = 0.03f;

    public MainMenuScreen(GdxGame game) {
        this.game = game;
        this.uiViewport = new FitViewport(800f, 450f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);
        this.keyboardController = new KeyboardController(UiControllerState.class, null, stage);
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        this.game.setInputProcessors(stage, keyboardController);
        MenuViewModel model = new MenuViewModel(game);
        model.setStartGameCallback(this::onStartGameRequested);
        this.stage.addActor(new MenuView(stage, skin, model));
        this.game.getAudioService().playMusic(MusicAsset.MENU);
    }

    @Override
    public void hide() {
        this.stage.clear();
    }

    @Override
    public void render(float delta) {
        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();

        if (startFadeActive) {
            startFadeTimer += delta;
            if (startFadeTimer >= RESUME_TRANS_OUT) startReachedBlack = true;
            renderStartFade();
            if (startReachedBlack) {
                startHoldTimer += delta;
                if (startHoldTimer >= START_FADE_HOLD) {
                    startFadeActive = false;
                    com.github.MichalKC.manylands.screen.GameScreen.requestStartupFadeInOnly();
                    game.setScreen(com.github.MichalKC.manylands.screen.GameScreen.class);
                }
            }
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (startShapeRenderer != null) { startShapeRenderer.dispose(); startShapeRenderer = null; }
    }

    private void onStartGameRequested() {
        if (startFadeActive) return;
        if (startupFadePlayed) {
            game.setScreen(com.github.MichalKC.manylands.screen.GameScreen.class);
            return;
        }
        startupFadePlayed = true;
        startFadeActive = true;
        startFadeTimer = 0f;
        startReachedBlack = false;
        startHoldTimer = 0f;
        if (startShapeRenderer == null) startShapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
    }

    private void renderStartFade() {
        if (startShapeRenderer == null) return;
        float vw = uiViewport.getWorldWidth();
        float vh = uiViewport.getWorldHeight();
        float xc = vw / 2f;
        float yc = vh / 2f;
        float maxR = (float)Math.hypot(vw * 0.5f, vh * 0.5f) + 8f;
        float t = Math.min(1f, startFadeTimer / RESUME_TRANS_OUT);
        float r = startReachedBlack ? 0f : maxR * (1f - t);

        startShapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        if (startReachedBlack || r <= 0f) {
            startShapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
            startShapeRenderer.setColor(com.badlogic.gdx.graphics.Color.BLACK);
            startShapeRenderer.rect(0f, 0f, vw, vh);
            startShapeRenderer.end();
        } else {
            float rOut = Math.max(maxR * 2f, Math.max(vw, vh) * 2f);
            drawCircularCutout(startShapeRenderer, xc, yc, r, com.badlogic.gdx.graphics.Color.BLACK, rOut);
        }
    }

    private static void drawCircularCutout(com.badlogic.gdx.graphics.glutils.ShapeRenderer renderer, float xc, float yc, float r, com.badlogic.gdx.graphics.Color color, float rOut) {
        float vw = renderer.getProjectionMatrix().getScaleX();
        if (r <= 0f) {
            renderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
            renderer.setColor(color);
            renderer.rect(-2000f, -2000f, 4000f, 4000f);
            renderer.end();
            return;
        }

        renderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        renderer.setColor(color);
        int segments = 40;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            float x1 = xc + r * cos1;
            float y1 = yc + r * sin1;
            float x2 = xc + r * cos2;
            float y2 = yc + r * sin2;

            float x3 = xc + rOut * cos2;
            float y3 = yc + rOut * sin2;
            float x4 = xc + rOut * cos1;
            float y4 = yc + rOut * sin1;

            renderer.triangle(x1, y1, x2, y2, x3, y3);
            renderer.triangle(x1, y1, x3, y3, x4, y4);
        }
        renderer.end();
    }
}
