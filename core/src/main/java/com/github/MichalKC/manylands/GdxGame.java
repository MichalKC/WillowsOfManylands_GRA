package com.github.MichalKC.manylands;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.screen.GameScreen;
import com.github.MichalKC.manylands.screen.LoadingScreen;
import com.github.MichalKC.manylands.tiled.TiledService;
import com.github.MichalKC.manylands.world.MapWorldState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.github.MichalKC.manylands.combat.CombatResult;

public class GdxGame extends Game {
    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;
    public static final float UNIT_SCALE = 1f / 32f;

    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger;
    private InputMultiplexer inputMultiplexer;
    private AudioService audioService;

    private Cursor cursorNormal;
    private Cursor cursorPressed;
    private InputAdapter cursorInputAdapter;

    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();
    private GameResumeState pendingGameResume;
    private PlayerSessionState playerSessionState;
    private final Map<String, MapWorldState> mapWorldStates = new HashMap<>();
    private final Set<String> wonCombats = new HashSet<>();
    private CombatResult pendingCombatResult;
    private String pendingCombatEnemyId;
    private float pendingCombatPlayerHp;
    private float pendingCombatHeartScreenX = -1f;
    private float pendingCombatHeartScreenY = -1f;
    private float pendingCombatHeartNormX = -1f; // 0..1 within GameScreen viewport area
    private float pendingCombatHeartNormY = -1f; // 0..1 within GameScreen viewport area

    private PlayerSessionState checkpointPlayerSession;
    private MapWorldState checkpointMapWorldState;
    private String checkpointMapKey;
    private Set<String> checkpointWonCombats = new HashSet<>();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        this.inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        this.assetService = new AssetService(new InternalFileHandleResolver());
        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();
        this.fpsLogger = new FPSLogger();
        this.audioService = new AudioService(assetService);

        setupCursor();
        cursorInputAdapter = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (cursorPressed != null) Gdx.graphics.setCursor(cursorPressed);
                return false;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (cursorNormal != null) Gdx.graphics.setCursor(cursorNormal);
                return false;
            }
        };
        inputMultiplexer.addProcessor(cursorInputAdapter);

        addScreen(new LoadingScreen(this, assetService));
        setScreen(LoadingScreen.class);
    }

    public void setupCursor() {
        try {
            int size = 64;
            Pixmap pmSrc1 = new Pixmap(Gdx.files.internal("ui/inventory/inputsarrowsleftup1.png"));
            Pixmap pmSrc2 = new Pixmap(Gdx.files.internal("ui/inventory/inputsarrowsleftup.png"));
            Pixmap pmNormal = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pmNormal.drawPixmap(pmSrc1, 0, 0, pmSrc1.getWidth(), pmSrc1.getHeight(), 0, 0, size, size);
            Pixmap pmPressed = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pmPressed.drawPixmap(pmSrc2, 0, 0, pmSrc2.getWidth(), pmSrc2.getHeight(), 0, 0, size, size);
            pmSrc1.dispose();
            pmSrc2.dispose();
            if (cursorNormal != null) cursorNormal.dispose();
            if (cursorPressed != null) cursorPressed.dispose();
            cursorNormal = Gdx.graphics.newCursor(pmNormal, 0, 0);
            cursorPressed = Gdx.graphics.newCursor(pmPressed, 0, 0);
            pmNormal.dispose();
            pmPressed.dispose();
            Gdx.graphics.setCursor(cursorNormal);
            Gdx.app.log("GdxGame", "Cursor set: " + (cursorNormal != null));
        } catch (Exception e) {
            Gdx.app.error("GdxGame", "Failed to set cursor", e);
        }
    }

    public Cursor getCursorNormal() { return cursorNormal; }
    public Cursor getCursorPressed() { return cursorPressed; }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, false);
        super.resize(width, height);
    }

    public void addScreen(Screen screen) {
        screenCache.put(screen.getClass(), screen);
    }

    public void removeScreen(Screen loadingScreen) {
        screenCache.remove(screen.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T extends Screen> T getScreen(Class<T> screenClass) {
        return (T) screenCache.get(screenClass);
    }

    public void setScreen(Class<? extends Screen> screenClass){
        Screen screen = screenCache.get(screenClass);
        if(screen == null) {
            throw new GdxRuntimeException("No screen with class" + screenClass + " found in the screen cache");
        }
        super.setScreen(screen);
    }

    @Override
    public void render() {
        glProfiler.reset();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();

        Gdx.graphics.setTitle("Willows Of Manylands - Draw Calls: " + glProfiler.getDrawCalls());
        fpsLogger.log();
    }

    @Override
    public void dispose(){
        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();

        this.batch.dispose();
        this.assetService.debugDiagnostics();
        this.assetService.dispose();
        if (cursorNormal != null) { cursorNormal.dispose(); cursorNormal = null; }
        if (cursorPressed != null) { cursorPressed.dispose(); cursorPressed = null; }
    }

    public Batch getBatch() {
        return batch;
    }

    public AssetService getAssetService() {
        return assetService;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    public void setInputProcessors(InputProcessor... processors) {
        inputMultiplexer.clear();
        if (cursorInputAdapter != null) inputMultiplexer.addProcessor(cursorInputAdapter);
        if (processors == null) return;

        for (InputProcessor processor : processors) {
            inputMultiplexer.addProcessor(processor);
        }
    }

    public AudioService getAudioService() {
        return audioService;
    }

    public void setPendingGameResume(GameResumeState state) {
        this.pendingGameResume = state;
    }

    public GameResumeState takePendingGameResume() {
        GameResumeState s = this.pendingGameResume;
        this.pendingGameResume = null;
        return s;
    }

    public void setPlayerSessionState(PlayerSessionState state) {
        this.playerSessionState = state;
    }

    public PlayerSessionState getPlayerSessionState() {
        return playerSessionState;
    }

    public void clearPlayerSessionState() {
        this.playerSessionState = null;
    }

    public void putMapWorldState(String mapKey, MapWorldState state) {
        String key = TiledService.normalizeMapKey(mapKey);
        if (key == null || key.isBlank() || state == null) {
            return;
        }
        mapWorldStates.put(key, state);
    }

    public MapWorldState getMapWorldState(String mapKey) {
        String key = TiledService.normalizeMapKey(mapKey);
        if (key == null || key.isBlank()) {
            return null;
        }
        return mapWorldStates.get(key);
    }

    public Set<String> getWonCombats() { return wonCombats; }

    public void setPendingCombatResult(CombatResult result) { this.pendingCombatResult = result; }
    public CombatResult takePendingCombatResult() {
        CombatResult r = this.pendingCombatResult;
        this.pendingCombatResult = null;
        return r;
    }

    public void setPendingCombatEnemyId(String id) { this.pendingCombatEnemyId = id; }
    public String getPendingCombatEnemyId() { return pendingCombatEnemyId; }

    public void setPendingCombatPlayerHp(float hp) { this.pendingCombatPlayerHp = hp; }
    public float takePendingCombatPlayerHp() {
        float hp = this.pendingCombatPlayerHp;
        this.pendingCombatPlayerHp = 0;
        return hp;
    }

    public void setPendingCombatHeartScreen(float x, float y) {
        this.pendingCombatHeartScreenX = x;
        this.pendingCombatHeartScreenY = y;
    }

    public float getPendingCombatHeartScreenX() { return pendingCombatHeartScreenX; }
    public float getPendingCombatHeartScreenY() { return pendingCombatHeartScreenY; }
    public void clearPendingCombatHeartScreen() { this.pendingCombatHeartScreenX = -1f; this.pendingCombatHeartScreenY = -1f; }

    public void setPendingCombatHeartNormalized(float nx, float ny) {
        this.pendingCombatHeartNormX = nx;
        this.pendingCombatHeartNormY = ny;
    }
    public float getPendingCombatHeartNormX() { return pendingCombatHeartNormX; }
    public float getPendingCombatHeartNormY() { return pendingCombatHeartNormY; }
    public void clearPendingCombatHeartNormalized() { this.pendingCombatHeartNormX = -1f; this.pendingCombatHeartNormY = -1f; }

    public void saveCheckpoint(String mapKey, PlayerSessionState playerSession) {
        if (mapKey == null) return;
        this.checkpointMapKey = TiledService.normalizeMapKey(mapKey);

        this.checkpointPlayerSession = PlayerSessionState.clone(playerSession);

        MapWorldState activeMapState = getMapWorldState(mapKey);
        if (activeMapState != null) {
            this.checkpointMapWorldState = new MapWorldState(activeMapState);
        } else {
            this.checkpointMapWorldState = new MapWorldState();
        }

        this.checkpointWonCombats = new HashSet<>(this.wonCombats);

        Gdx.app.log("Checkpoint", "Saved checkpoint for map: " + this.checkpointMapKey);
    }

    public void restoreCheckpoint() {
        if (checkpointMapKey == null) return;

        if (checkpointPlayerSession != null) {
            this.playerSessionState = PlayerSessionState.clone(checkpointPlayerSession);
        }

        if (checkpointMapWorldState != null) {
            putMapWorldState(checkpointMapKey, new MapWorldState(checkpointMapWorldState));
        }

        this.wonCombats.clear();
        this.wonCombats.addAll(checkpointWonCombats);

        Gdx.app.log("Checkpoint", "Restored checkpoint for map: " + checkpointMapKey);
    }

    public String getCheckpointMapKey() {
        return checkpointMapKey;
    }
}
