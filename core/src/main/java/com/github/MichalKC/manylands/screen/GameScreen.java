package com.github.MichalKC.manylands.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.MichalKC.manylands.GameResumeState;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.MapAsset;
import com.github.MichalKC.manylands.asset.SkinAsset;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.component.*;
import com.github.MichalKC.manylands.input.DialogueControllerState;
import com.github.MichalKC.manylands.input.GameControllerState;
import com.github.MichalKC.manylands.input.KeyboardController;
import com.github.MichalKC.manylands.input.ShopChoiceControllerState;
import com.github.MichalKC.manylands.input.UiControllerState;
import com.github.MichalKC.manylands.system.*;
import com.github.MichalKC.manylands.PlayerSessionState;
import com.github.MichalKC.manylands.player.PlayerStatePersistence;
import com.github.MichalKC.manylands.system.SessionRestoreSystem;
import com.github.MichalKC.manylands.tiled.PlayerSpawnHelper;
import com.github.MichalKC.manylands.world.MapWorldState;
import com.github.MichalKC.manylands.world.WorldStatePersistence;
import com.github.MichalKC.manylands.tiled.TiledAshleyConfigurator;
import com.github.MichalKC.manylands.tiled.TiledService;
import com.github.MichalKC.manylands.ui.model.GameViewModel;
import com.github.MichalKC.manylands.ui.model.InventoryViewModel;
import com.github.MichalKC.manylands.ui.view.GameView;
import com.github.MichalKC.manylands.ui.view.InventoryView;
import com.github.MichalKC.manylands.ui.view.ShopView;
import com.github.MichalKC.manylands.ui.view.SkillsShopView;
import com.github.MichalKC.manylands.combat.CombatConfig;
import com.github.MichalKC.manylands.combat.CombatEnemyRegistry;
import com.github.MichalKC.manylands.combat.CombatResult;
import com.github.MichalKC.manylands.screen.CombatScreen;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {
    private final Engine engine;
    private final TiledService tiledService;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final KeyboardController keyboardController;
    private final GdxGame game;
    private final World physicWorld;
    private final AudioService audioService;
    private final Stage stage;
    private final Viewport uiViewport;
    private final GameViewModel viewModel;
    private final Skin skin;
    private final SessionRestoreSystem sessionRestoreSystem;
    private final InventoryViewModel inventoryViewModel;
    private InventoryView inventoryView;
    private ShopView shopView;
    private SkillsShopView skillsShopView;
    private GameView gameView;
    private boolean inventoryOpen;
    private boolean shopOpen;
    private boolean skillsShopOpen;
    private Entity openStorageEntity;
    private InteractableSystem interactableSystem;
    private DialogueSystem dialogueSystem;
    private Cursor cursorNormal;
    private Cursor cursorPressed;
    private CombatTriggerSystem combatTriggerSystem;
    private boolean pendingCombatDeath;
    private int combatTriggerCooldownFrames;

    private static final float EXCL_DURATION = 1.3f;
    private static final float HEART_MOVE_DURATION = 1.4f;
    private static final float FADE_DURATION = 0.8f;
    private boolean combatTransitionActive;
    private int combatTransPhase;
    private float combatTransTimer;
    private CombatScreen pendingCombatScreen;
    private float warningScreenX, warningScreenY;
    private float heartStartScreenX, heartStartScreenY;
    private float heartCurX, heartCurY;
    private ShapeRenderer transShapeRenderer;
    private com.badlogic.gdx.graphics.g2d.SpriteBatch transBatch;
    private BitmapFont transFont;

    private static final float GAMEOVER_FADE_DURATION = 1.2f;
    private static final float GAMEOVER_TEXT_DELAY = 0.5f;
    private static final float GAMEOVER_BUTTONS_DELAY = 1.5f;
    private static final String[] GAMEOVER_MESSAGES = {
        "You died",
        "Maybe next time",
        "Sometimes sacrifice is needed",
        "Rest in peace",
        "Not this time",
        "The journey continues...",
        "Death is just the beginning"
    };
    private boolean gameOverActive;
    private float gameOverTimer;
    private float gameOverGrayscale;
    private String gameOverMessage;
    private int gameOverSelectedButton;
    private boolean gameOverButtonsVisible;
    private boolean suppressGameOver;
    private float suppressGameOverTimer;
    private com.badlogic.gdx.graphics.g2d.SpriteBatch gameOverBatch;
    private ShapeRenderer gameOverShapeRenderer;
    private com.badlogic.gdx.graphics.g2d.BitmapFont gameOverFont;
    private com.badlogic.gdx.graphics.g2d.GlyphLayout gameOverLayout;
    private com.badlogic.gdx.InputAdapter gameOverInputAdapter;
    private boolean requestedMainMenuFromGameOver;

    private boolean resumeTransitionActive;
    private float resumeTransitionTimer;
    private boolean resumeTransitionRespawned;
    private boolean resumeTransitionShouldRespawn;
    private boolean resumeTransitionBlackDrawn;
    private static boolean startupTransitionPlayed = false;
    private static boolean startupInOnly = false;
    private static final float RESUME_TRANS_OUT = 0.5f;
    private static final float RESUME_TRANS_HOLD = 0.1f;
    private static final float RESUME_TRANS_IN = 0.5f;

    public GameScreen(GdxGame game) {
        this.game = game;
        this.physicWorld = new World(Vector2.Zero, true);
        this.physicWorld.setAutoClearForces(false);
        this.tiledService = new TiledService(game.getAssetService(), this.physicWorld, game);
        this.engine = new Engine();
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(
            this.engine, game.getAssetService(), this.tiledService, game, this.physicWorld
        );
        this.audioService = game.getAudioService();
        this.uiViewport = new FitViewport(320f, 180f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.viewModel = new GameViewModel(game);
        this.skin = game.getAssetService().get(SkinAsset.DEFAULT);
        this.inventoryViewModel = new InventoryViewModel(game);
        this.inventoryViewModel.setEquipChangedCallback(this::reapplyEquipBonuses);
        this.keyboardController = new KeyboardController(GameControllerState.class, engine, stage);
        GameControllerState controllerState = this.keyboardController.getState(GameControllerState.class);
        if (controllerState != null) {
            controllerState.setInventoryToggleCallback(this::toggleInventory);
            controllerState.setSkillActivateCallback(this::onSkillActivate);
            controllerState.setSpecialActivateCallback(this::onSpecialActivate);
            controllerState.setGameScreen(this);
        }
        DialogueControllerState dialogueControllerState = this.keyboardController.getState(DialogueControllerState.class);
        if (dialogueControllerState != null) {
            dialogueControllerState.setAdvanceCallback(() -> { if (dialogueSystem != null) dialogueSystem.advance(); });
        }
        ShopChoiceControllerState shopChoiceControllerState = this.keyboardController.getState(ShopChoiceControllerState.class);
        if (shopChoiceControllerState != null) {
            shopChoiceControllerState.setYesCallback(this::onShopChoiceYes);
            shopChoiceControllerState.setNoCallback(this::onShopChoiceNo);
        }

        CameraSystem cameraSystem = new CameraSystem(game.getCamera());
        this.engine.addSystem(new EnemyAISystem());
        this.engine.addSystem(new NpcAISystem());
        this.engine.addSystem(new PlayerMovementInputSystem());
        this.engine.addSystem(new PhysicMoveSystem());
        this.engine.addSystem(new PhysicSystem(physicWorld, 1 / 60f));
        this.engine.addSystem(new FacingSystem());
        this.engine.addSystem(new AttackSystem(physicWorld, game.getAudioService()));
        this.engine.addSystem(new FsmSystem());
        this.engine.addSystem(new DamagedSystem(viewModel));
        this.engine.addSystem(new DeadCleanupSystem(physicWorld));
        this.engine.addSystem(new RespawnSystem(physicWorld, viewModel));
        this.engine.addSystem(new DamageCooldownSystem());
        this.sessionRestoreSystem = new SessionRestoreSystem(this.engine, this.viewModel);
        this.sessionRestoreSystem.setPostRestoreCallback(this::reapplyEquipBonuses);
        this.engine.addSystem(new MapTransitionSystem(
            this.engine, this.tiledService, cameraSystem, this.viewModel, game, this.sessionRestoreSystem
        ));
        this.engine.addSystem(new TriggerSystem(game.getAudioService()));
        this.engine.addSystem(new HealZoneSystem(this.viewModel));
        this.engine.addSystem(new DamageZoneSystem());
        this.engine.addSystem(new ActiveSkillsSystem(this.viewModel));
        this.combatTriggerSystem = new CombatTriggerSystem(game.getWonCombats(), game.getAudioService());
        this.combatTriggerSystem.setCombatCallback(this::onCombatTriggered);
        this.engine.addSystem(this.combatTriggerSystem);
        this.engine.addSystem(new LifeSystem(this.viewModel));
        this.engine.addSystem(new AnimationSystem(game.getAssetService()));
        this.engine.addSystem(new EnemyDeathRemovalSystem());
        this.dialogueSystem = new DialogueSystem(viewModel);
        this.dialogueSystem.setCloseCallback(this::closeDialogue);
        this.dialogueSystem.setStorePromptCallback(this::onStorePrompt);
        this.engine.addSystem(this.dialogueSystem);
        this.interactableSystem = new InteractableSystem(this.engine, viewModel);
        this.interactableSystem.setStorageOpenCallback(this::openStorageInventory);
        this.interactableSystem.setDialogueCallback(this::openDialogue);
        this.interactableSystem.setGameScreen(this);
        this.engine.addSystem(this.interactableSystem);
        this.engine.addSystem(new TextDisplaySystem(this.engine, viewModel));
        this.engine.addSystem(new CoinSystem(this.engine, game.getAssetService(), viewModel, physicWorld));
        this.engine.addSystem(new ItemSystem(this.engine, game.getAssetService(), viewModel, physicWorld, tiledService));
        this.engine.addSystem(cameraSystem);
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));
        this.engine.addSystem(new PhysicDebugRenderSystem(physicWorld, game.getCamera()));
        ControllerSystem controllerSystem = new ControllerSystem(game);
        controllerSystem.setGameScreen(this);
        this.engine.addSystem(controllerSystem);
        this.engine.addSystem(this.sessionRestoreSystem);
    }

    @Override
    public void show() {
        initGameOverInputAdapter();
        game.setInputProcessors(gameOverInputAdapter, keyboardController, stage);
        keyboardController.setActiveState(GameControllerState.class);
        setupCursor();

        this.gameView = new GameView(stage, skin, this.viewModel);
        this.stage.addActor(this.gameView);
        this.inventoryView = new InventoryView(stage, skin, this.inventoryViewModel);
        this.inventoryView.setAssetService(game.getAssetService());
        this.inventoryView.setCloseCallback(this::closeInventory);
        this.inventoryView.setSkillSelectedCallback(hudIdx -> this.viewModel.setSelectedSkillHudIdx(hudIdx));
        this.inventoryView.setSkillLockSupplier(() -> this.viewModel.getSkillWidgetStateOrdinal() != 0);
        this.inventoryView.setEatCallback(this::onEatItem);
        this.inventoryView.hideInventory();
        this.stage.addActor(this.inventoryView);
        this.shopView = new ShopView(stage, skin, this.viewModel);
        this.shopView.setCloseCallback(this::closeShop);
        this.stage.addActor(this.shopView);
        this.skillsShopView = new SkillsShopView(stage, skin, this.viewModel);
        this.skillsShopView.setCloseCallback(this::closeSkillsShop);
        this.skillsShopView.setSkillUnlockCallback(this.inventoryViewModel::unlockSkill);
        this.skillsShopView.setSkillBoughtChecker(this.inventoryViewModel::isSkillUnlocked);
        this.stage.addActor(this.skillsShopView);

        Consumer<TiledMap> renderConsumer = this.engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = this.engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = audioService::setMap;

        this.tiledService.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        this.tiledService.setLoadObjectConsumer(this.tiledAshleyConfigurator::onLoadObject);
        this.tiledService.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);
        this.tiledService.setLoadTriggerConsumer(this.tiledAshleyConfigurator::onLoadTrigger);
        this.tiledService.setPostLoadConsumer(this.tiledAshleyConfigurator::flushPendingStorageItems);

        GameResumeState resume = game.takePendingGameResume();
        TiledMap mapToShow = null;
        float resumeX = 0f;
        float resumeY = 0f;
        if (resume != null) {
            if (resume.isFromMapFile()) {
                mapToShow = this.tiledService.loadMapFromFile(resume.getMapFile());
            } else if (resume.getMapAsset() != null) {
                mapToShow = this.tiledService.loadMap(resume.getMapAsset());
            }
            resumeX = resume.getWorldX();
            resumeY = resume.getWorldY();
            float px = resumeX / GdxGame.UNIT_SCALE;
            float py = resumeY / GdxGame.UNIT_SCALE;
            this.tiledService.setPendingPlayerSpawnPixels(px, py);
        }
        tiledService.setPendingPlayerSession(game.getPlayerSessionState());

        if (mapToShow != null) {
            this.tiledService.setMap(mapToShow);
            PlayerSpawnHelper.applyPlayerWorldPosition(engine, resumeX, resumeY, false);
            this.engine.getSystem(CameraSystem.class).setMap(mapToShow);
        } else {
            TiledMap tiledMap = this.tiledService.loadMap(MapAsset.MAIN);
            this.tiledService.setMap(tiledMap);
        }

        WorldStatePersistence.applyNpcDeadResume(engine, game.getMapWorldState(tiledService.getCurrentMapKey()));

        if (mapToShow == null) {
            PlayerSessionState sessPos = game.getPlayerSessionState();
            if (sessPos != null) {
                PlayerSpawnHelper.applyPlayerWorldPosition(engine, sessPos.getSpawnPositionX(), sessPos.getSpawnPositionY(), false);
            }
        }

        restoreDroppedCoins();
        handleCombatReturn();
        restorePlayerSessionAfterLoad();
        forceRespawnIfDead();
        initGameOverResources();
        resetGameOverState();
        resumeTransitionActive = false;
        if (!startupTransitionPlayed) {
            if (startupInOnly) {
                startStartupFadeInOnly();
            } else {
                startStartupTransition();
            }
            startupTransitionPlayed = true;
            startupInOnly = false;
        }

        String mapKey = tiledService.getCurrentMapKey();
        if (mapKey != null) {
            if (game.getCheckpointMapKey() == null || !game.getCheckpointMapKey().equals(mapKey)) {
                PlayerSessionState currentSession = game.getPlayerSessionState();
                if (currentSession == null) {
                    currentSession = PlayerStatePersistence.capture(engine, viewModel);
                    if (currentSession != null) {
                        game.setPlayerSessionState(currentSession);
                    }
                }
                game.saveCheckpoint(mapKey, currentSession);
            }
        }
    }

    private void setupCursor() {
        cursorNormal = game.getCursorNormal();
        cursorPressed = game.getCursorPressed();
        if (cursorNormal != null) Gdx.graphics.setCursor(cursorNormal);
    }

    private void restoreDroppedCoins() {
        MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
        if (worldState != null) {
            CoinSystem coinSystem = engine.getSystem(CoinSystem.class);
            if (coinSystem != null) {
                coinSystem.restoreCoins(worldState.getDroppedCoins());
            }
        }
    }

    private void restorePlayerSessionAfterLoad() {
        PlayerSessionState session = game.getPlayerSessionState();
        if (session != null) {
            sessionRestoreSystem.schedule(session);
        }
    }

    @Override
    public void hide() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class, Transform.class).get());
        if (players.size() > 0) {
            if (requestedMainMenuFromGameOver || gameOverActive) {
                game.restoreCheckpoint();

                PlayerSessionState cleanSession = game.getPlayerSessionState();
                if (cleanSession != null) {
                    MapAsset mapAsset = tiledService.getCurrentMap().getProperties().get("mapAsset", MapAsset.class);
                    String mapFile = tiledService.getCurrentMap().getProperties().get("mapFile", String.class);
                    GameResumeState resumeAtSpawn = new GameResumeState(
                        mapFile,
                        mapAsset,
                        cleanSession.getSpawnPositionX(),
                        cleanSession.getSpawnPositionY()
                    );
                    game.setPendingGameResume(resumeAtSpawn);
                } else {
                    game.setPendingGameResume(null);
                }
            } else {
                Transform transform = Transform.MAPPER.get(players.first());
                Vector2 pos = transform.getPosition();
                GameResumeState state = tiledService.buildResumeState(pos.x, pos.y);
                if (state != null) {
                    game.setPendingGameResume(state);
                }
                PlayerSessionState session = PlayerStatePersistence.capture(engine, viewModel);
                if (session != null) {
                    game.setPlayerSessionState(session);
                }
                String mapKey = tiledService.getCurrentMapKey();
                if (mapKey != null) {
                    MapWorldState prev = game.getMapWorldState(mapKey);
                    game.putMapWorldState(mapKey, WorldStatePersistence.captureForResume(engine, prev));
                }
            }
        }
        this.engine.removeAllEntities();
        this.stage.clear();
        disposeGameOverResources();
        requestedMainMenuFromGameOver = false;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.uiViewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        boolean frozen = inventoryOpen || shopOpen || combatTransitionActive;
        this.engine.update(frozen ? 0f : delta);

        if (combatTriggerCooldownFrames > 0) {
            combatTriggerCooldownFrames--;
            if (combatTriggerCooldownFrames <= 0) {
                combatTriggerSystem.setProcessing(true);
            }
        }

        if (pendingCombatDeath) {
            pendingCombatDeath = false;
            ImmutableArray<Entity> deathPlayers = engine.getEntitiesFor(Family.all(Player.class).get());
            if (deathPlayers.size() > 0) {
                Entity player = deathPlayers.first();
                Life life = Life.MAPPER.get(player);
                if (life != null) life.addLife(-life.getLife());
                if (!Dead.MAPPER.has(player)) player.add(new Dead());
            }
        }

        if (suppressGameOver) {
            suppressGameOverTimer = Math.max(0f, suppressGameOverTimer - delta);
            ImmutableArray<Entity> deadPlayers = engine.getEntitiesFor(Family.all(Player.class, Dead.class).get());
            if (deadPlayers.size() == 0 || suppressGameOverTimer <= 0f) suppressGameOver = false;
        }

        checkGameOverTrigger();
        updateGameOver(delta);

        uiViewport.apply();
        float gray = 1f - 0.5f * gameOverGrayscale;
        stage.getBatch().setColor(gray, gray, gray, 1f);
        stage.act(delta);
        stage.draw();

        if (combatTransitionActive) {
            renderCombatTransition(delta);
        }

        if (gameOverActive) {
            renderGameOver();
        }

        if (resumeTransitionActive) {
            updateResumeTransition(delta);
            renderResumeTransition();
        }
    }

    private void renderCombatTransition(float delta) {
        combatTransTimer += delta;
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        transShapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        switch (combatTransPhase) {
            case 0: {
                float t = combatTransTimer;
                int blinkCount = (int)(t / 0.22f);
                boolean visible = blinkCount % 2 == 0;
                if (visible) {
                    transFont.setColor(Color.RED);
                    transBatch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
                    transBatch.begin();
                    com.badlogic.gdx.graphics.g2d.GlyphLayout glyph = new com.badlogic.gdx.graphics.g2d.GlyphLayout(transFont, "!");
                    float gx = warningScreenX - glyph.width / 2f;
                    float gy = warningScreenY + glyph.height / 2f;
                    transFont.draw(transBatch, glyph, gx, gy);
                    transBatch.end();
                }
                if (t >= EXCL_DURATION) {
                    combatTransPhase = 1;
                    combatTransTimer = 0;
                    heartCurX = heartStartScreenX;
                    heartCurY = heartStartScreenY;
                }
                break;
            }
            case 1: {
                float t = Math.min(combatTransTimer / HEART_MOVE_DURATION, 1f);
                float ease = t * t * (3f - 2f * t);
                float targetX = sw / 2f;
                float targetY = sh * 0.18f;
                heartCurX = heartStartScreenX + (targetX - heartStartScreenX) * ease;
                heartCurY = heartStartScreenY + (targetY - heartStartScreenY) * ease;

                transShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                float r = 18f;
                transShapeRenderer.setColor(new Color(0.4f, 0.85f, 0.5f, 0.75f));
                transShapeRenderer.circle(heartCurX - r * 0.4f, heartCurY + r * 0.2f, r * 0.5f);
                transShapeRenderer.circle(heartCurX + r * 0.4f, heartCurY + r * 0.2f, r * 0.5f);
                transShapeRenderer.triangle(heartCurX - r * 0.85f, heartCurY,
                    heartCurX + r * 0.85f, heartCurY, heartCurX, heartCurY - r * 1.0f);
                transShapeRenderer.end();

                if (combatTransTimer >= HEART_MOVE_DURATION) {
                    combatTransPhase = 2;
                    combatTransTimer = 0;
                }
                break;
            }
            case 2: {
                float t = Math.min(combatTransTimer / FADE_DURATION, 1f);
                transShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                transShapeRenderer.setColor(new Color(0, 0, 0, t));
                transShapeRenderer.rect(0, 0, sw, sh);
                transShapeRenderer.end();

                if (combatTransTimer >= FADE_DURATION) {
                    finishCombatTransition();
                }
                break;
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void onSkillActivate() {
        if (gameView != null) gameView.activateSkill();
        com.badlogic.ashley.utils.ImmutableArray<com.badlogic.ashley.core.Entity> players =
            engine.getEntitiesFor(com.badlogic.ashley.core.Family.all(
                com.github.MichalKC.manylands.component.Player.class,
                com.github.MichalKC.manylands.component.Inventory.class,
                com.github.MichalKC.manylands.component.ActiveSkills.class
            ).get());
        if (players.size() > 0) {
            com.badlogic.ashley.core.Entity player = players.first();
            com.github.MichalKC.manylands.component.Inventory inv = com.github.MichalKC.manylands.component.Inventory.MAPPER.get(player);
            int slot = inv != null ? inv.getPersistedActivatedSkillSlot() : -1;
            com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(player);
            if (skills != null) {
                if (slot == 0) {
                    skills.tryActivateSkill1();
                } else if (slot == 1) {
                    skills.tryActivateSkill2();
                } else if (slot == 2) {
                    skills.tryActivateSkill3();
                } else if (slot == 3) {
                    skills.tryActivateSkill4();
                } else if (slot == 4) {
                    skills.tryActivateSkill5();
                } else if (slot == 5) {
                    skills.tryActivateSkill6();
                } else if (slot == 6) {
                    skills.tryActivateSkill7();
                } else if (slot == 7) {
                    skills.tryActivateSkill8();
                }
            }
        }
    }

    private void onSpecialActivate() {
        if (viewModel == null || !viewModel.canTriggerSpecial()) return;
        com.badlogic.ashley.utils.ImmutableArray<com.badlogic.ashley.core.Entity> players =
            engine.getEntitiesFor(com.badlogic.ashley.core.Family.all(com.github.MichalKC.manylands.component.Player.class,
                com.github.MichalKC.manylands.component.Attack.class).get());
        boolean started = false;
        if (players.size() > 0) {
            com.github.MichalKC.manylands.component.Attack attack =
                com.github.MichalKC.manylands.component.Attack.MAPPER.get(players.first());
            if (attack != null && attack.canAttack()) {
                attack.startAttack(com.github.MichalKC.manylands.component.Attack.AttackType.SPECIAL);
                started = true;
            }
        }
        if (started) {
            viewModel.setSpecialDraining(true);
        }
    }

    private void toggleInventory() {
        if (inventoryOpen) {
            closeInventory();
        } else {
            openInventory();
        }
    }

    private void openInventory() {
        if (inventoryView == null || inventoryOpen) return;
        openStorageEntity = null;
        inventoryOpen = true;
        setPlayerRooted(true);
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() > 0) {
            Inventory playerInv = Inventory.MAPPER.get(players.first());
            inventoryViewModel.setPlayerInventory(playerInv);
        }
        inventoryViewModel.setStorage(null);
        inventoryView.showInventory();
        reapplyEquipBonuses();
        if (gameView != null) gameView.pauseSkill(true);
        keyboardController.setActiveState(UiControllerState.class);
    }

    private void openStorageInventory(Entity storageEntity) {
        if (inventoryView == null) return;
        Storage storage = Storage.MAPPER.get(storageEntity);
        if (storage == null) return;
        openStorageEntity = storageEntity;
        inventoryOpen = true;
        setPlayerRooted(true);
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() > 0) {
            Inventory playerInv = Inventory.MAPPER.get(players.first());
            inventoryViewModel.setPlayerInventory(playerInv);
        }
        inventoryViewModel.setStorage(storage);

        com.github.MichalKC.manylands.component.Tiled tiled = com.github.MichalKC.manylands.component.Tiled.MAPPER.get(storageEntity);
        if (tiled != null && tiled.getLayerName() != null) {
            MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
            if (worldState != null) {
                String storageKey = WorldStatePersistence.objectKey(tiled.getLayerName(), tiled.getMapObjectRef());
                com.github.MichalKC.manylands.component.Item[] savedSlots = worldState.getStorageSlots(storageKey);
                if (savedSlots != null) {
                    System.arraycopy(savedSlots, 0, storage.getSlots(), 0, Math.min(storage.getSlots().length, savedSlots.length));
                }
            }
        }

        inventoryView.showStorage(storage);
        reapplyEquipBonuses();
        keyboardController.setActiveState(UiControllerState.class);
    }

    private static final int BASE_DEFENSE = 2;

    private void reapplyEquipBonuses() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() == 0) return;
        Entity player = players.first();
        Inventory playerInv = Inventory.MAPPER.get(player);
        if (playerInv == null) return;
        Item[] equip = playerInv.getEquipSlots();
        int totalHP = 0, totalAtk1 = 0, totalAtk2 = 0, totalAtk3 = 0, totalDef = 0, totalSpd = 0;
        for (Item item : equip) {
            if (item == null) continue;
            totalHP   += item.getPlusHP();
            totalAtk1 += item.getPlusAttack1();
            totalAtk2 += item.getPlusAttack2();
            totalAtk3 += item.getPlusAttack3();
            totalDef  += item.getPlusDefense();
            totalSpd  += item.getPlusSpeed();
        }
        Life life = Life.MAPPER.get(player);
        if (life != null) {
            life.setEquipMaxLifeBonus(totalHP);
            viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
        }
        Attack attack = Attack.MAPPER.get(player);
        if (attack != null) {
            attack.setEquipBonusPrimary(totalAtk1);
            attack.setEquipBonusSecondary(totalAtk2);
            attack.setEquipBonusSpecial(totalAtk3);
        }
        Move move = Move.MAPPER.get(player);
        if (move != null) move.setEquipSpeedBonus(totalSpd);
        playerInv.setEquipDefenseBonus(totalDef);
        refreshInventoryStats();
    }

    private void refreshInventoryStats() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() == 0) return;
        Entity player = players.first();
        Life life = Life.MAPPER.get(player);
        Attack attack = Attack.MAPPER.get(player);
        Move move = Move.MAPPER.get(player);
        Inventory playerInv = Inventory.MAPPER.get(player);
        int hp  = life   != null ? (int) life.getMaxLife()            : 0;
        int atk = attack != null ? (int) attack.getPrimaryDamage()    : 0;
        int spd = move   != null ? (int) move.getMaxSpeed()           : 0;
        int def = BASE_DEFENSE + (playerInv != null ? playerInv.getEquipDefenseBonus() : 0);
        inventoryViewModel.updateStats(hp, atk, def, spd, viewModel.getCoins());
    }

    private void closeInventory() {
        if (inventoryView == null || !inventoryOpen) return;
        closeOpenStorage();
        inventoryOpen = false;
        setPlayerRooted(false);
        inventoryView.hideInventory();
        if (gameView != null) gameView.pauseSkill(false);
        keyboardController.setActiveState(GameControllerState.class);
    }

    private void onEatItem(com.github.MichalKC.manylands.component.Item item) {
        if (item == null) return;
        int healAmount = item.getPlusHP();
        if (healAmount <= 0) return;
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() == 0) return;
        Entity player = players.first();
        Life life = Life.MAPPER.get(player);
        if (life != null) {
            life.addLife(healAmount);
            viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
        }
        audioService.playSound(SoundAsset.LIFE_REG);
        refreshInventoryStats();
    }

    private void openDialogue(Entity dialogueEntity) {
        if (dialogueSystem == null) return;
        dialogueSystem.startDialogue(dialogueEntity);
        interactableSystem.setDialogueLock(true);
        keyboardController.setActiveState(DialogueControllerState.class);
    }

    private void onStorePrompt(Entity storeEntity) {
        boolean isSkills = com.github.MichalKC.manylands.component.SkillsStore.MAPPER.get(storeEntity) != null;
        viewModel.showTalkingOptions(isSkills ? "[E] Skills shop   [Esc] No" : "[E] Enter shop   [Esc] No");
        keyboardController.setActiveState(ShopChoiceControllerState.class);
        this.openStorageEntity = storeEntity;
    }

    private void onShopChoiceYes() {
        Entity entity = this.openStorageEntity;
        this.openStorageEntity = null;
        if (dialogueSystem != null) dialogueSystem.endDialogue();
        if (entity != null) {
            com.github.MichalKC.manylands.component.SkillsStore skillsStore =
                com.github.MichalKC.manylands.component.SkillsStore.MAPPER.get(entity);
            if (skillsStore != null) {
                openSkillsShop();
                return;
            }
            com.github.MichalKC.manylands.component.Store store =
                com.github.MichalKC.manylands.component.Store.MAPPER.get(entity);
            String storeName = store != null ? store.getStoreName() : "SHOP";
            String storeType = store != null ? store.getStoreType() : "general";
            openShop(storeName, storeType);
        } else {
            keyboardController.setActiveState(GameControllerState.class);
        }
    }

    private void onShopChoiceNo() {
        this.openStorageEntity = null;
        if (dialogueSystem != null) dialogueSystem.endDialogue();
    }

    private void openShop(String storeName, String storeType) {
        if (shopView == null || shopOpen) return;
        shopOpen = true;
        setPlayerRooted(true);
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        Inventory playerInv = players.size() > 0 ? Inventory.MAPPER.get(players.first()) : null;
        java.util.List<com.github.MichalKC.manylands.component.Item> catalog =
            tiledAshleyConfigurator.getShopCatalog(storeType);
        shopView.showShop(storeName, playerInv, catalog);
        keyboardController.setActiveState(UiControllerState.class);
    }

    private void closeShop() {
        if (shopView == null || !shopOpen) return;
        shopOpen = false;
        setPlayerRooted(false);
        shopView.hideShop();
        keyboardController.setActiveState(GameControllerState.class);
    }

    private void openSkillsShop() {
        if (skillsShopView == null || skillsShopOpen) return;
        skillsShopOpen = true;
        setPlayerRooted(true);
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() > 0) {
            Inventory playerInv = Inventory.MAPPER.get(players.first());
            inventoryViewModel.setPlayerInventory(playerInv);
        }
        skillsShopView.showShop();
        keyboardController.setActiveState(UiControllerState.class);
    }

    private void closeSkillsShop() {
        if (skillsShopView == null || !skillsShopOpen) return;
        skillsShopOpen = false;
        setPlayerRooted(false);
        skillsShopView.hideShop();
        keyboardController.setActiveState(GameControllerState.class);
    }

    private void closeDialogue() {
        interactableSystem.setDialogueLock(false);
        keyboardController.setActiveState(GameControllerState.class);
    }

    private void setPlayerRooted(boolean rooted) {
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class, Move.class).get());
        for (int i = 0; i < players.size(); i++) {
            Move move = Move.MAPPER.get(players.get(i));
            if (move != null) {
                move.setRooted(rooted);
                if (rooted) {
                    move.getDirection().setZero();
                }
            }
        }
    }

    private void onCombatTriggered(Entity triggerEntity, String enemyId) {
        if (combatTransitionActive) return;
        if (!CombatEnemyRegistry.has(enemyId)) {
            Gdx.app.error("GameScreen", "Unknown combat enemy: " + enemyId);
            return;
        }
        CombatConfig config = CombatEnemyRegistry.get(enemyId);
        game.setPendingCombatEnemyId(enemyId);

        float playerCurrentHp = 20f;
        float playerMaxHp = 20f;
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() > 0) {
            Life life = Life.MAPPER.get(players.first());
            if (life != null) {
                playerCurrentHp = life.getLife();
                playerMaxHp = life.getMaxLife();
            }
        }

        pendingCombatScreen = new CombatScreen(game);
        pendingCombatScreen.configure(config, playerCurrentHp, playerMaxHp);
        if (players.size() > 0) {
            Inventory inv = Inventory.MAPPER.get(players.first());
            if (inv != null) pendingCombatScreen.setInventoryItems(inv.getSlots());
        }

        if (players.size() > 0) {
            Entity pe = players.first();
            Physic phys = Physic.MAPPER.get(pe);
            Camera cam = game.getCamera();
            Vector2 heartWorld = null, warningWorld = null;
            if (phys != null && phys.getBody() != null) {
                Body body = phys.getBody();
                for (Fixture f : body.getFixtureList()) {
                    String name = (f.getUserData() instanceof String) ? (String) f.getUserData() : null;
                    if ("heart".equals(name)) {
                        heartWorld = getFixtureWorldCenter(body, f);
                    } else if ("warning".equals(name)) {
                        warningWorld = getFixtureWorldCenter(body, f);
                    }
                }
            }
            if (heartWorld == null || warningWorld == null) {
                Transform t = Transform.MAPPER.get(pe);
                float cx = 0, cy = 0;
                if (phys != null && phys.getBody() != null) {
                    cx = phys.getBody().getPosition().x;
                    cy = phys.getBody().getPosition().y;
                } else if (t != null) {
                    cx = t.getPosition().x + t.getSize().x / 2f;
                    cy = t.getPosition().y + t.getSize().y / 2f;
                }
                if (heartWorld == null) heartWorld = new Vector2(cx, cy);
                if (warningWorld == null) warningWorld = new Vector2(cx, cy + 0.15f);
            }
            Vector3 tmp = new Vector3(heartWorld.x, heartWorld.y, 0);
            cam.project(tmp, game.getViewport().getScreenX(), game.getViewport().getScreenY(),
                game.getViewport().getScreenWidth(), game.getViewport().getScreenHeight());
            heartStartScreenX = tmp.x;
            heartStartScreenY = tmp.y;
            tmp.set(warningWorld.x, warningWorld.y, 0);
            cam.project(tmp, game.getViewport().getScreenX(), game.getViewport().getScreenY(),
                game.getViewport().getScreenWidth(), game.getViewport().getScreenHeight());
            warningScreenX = tmp.x;
            warningScreenY = tmp.y;

            game.setPendingCombatHeartScreen(heartStartScreenX, heartStartScreenY);
            int vx = game.getViewport().getScreenX();
            int vy = game.getViewport().getScreenY();
            int vw = game.getViewport().getScreenWidth();
            int vh = game.getViewport().getScreenHeight();
            if (vw > 0 && vh > 0) {
                float nx = (heartStartScreenX - vx) / (float) vw;
                float ny = (heartStartScreenY - vy) / (float) vh;
                game.setPendingCombatHeartNormalized(nx, ny);
            } else {
                game.clearPendingCombatHeartNormalized();
            }
        }

        setPlayerRooted(true);
        combatTransitionActive = true;
        combatTransPhase = 0;
        combatTransTimer = 0;
        heartCurX = heartStartScreenX;
        heartCurY = heartStartScreenY;

        if (transShapeRenderer == null) transShapeRenderer = new ShapeRenderer();
        if (transBatch == null) transBatch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        if (transFont == null) {
            transFont = new BitmapFont();
            transFont.getData().setScale(3.5f);
        }
    }

    private Vector2 getFixtureWorldCenter(Body body, Fixture fixture) {
        com.badlogic.gdx.physics.box2d.Shape shape = fixture.getShape();
        Vector2 bodyPos = body.getPosition();
        switch (shape.getType()) {
            case Circle: {
                com.badlogic.gdx.physics.box2d.CircleShape cs = (com.badlogic.gdx.physics.box2d.CircleShape) shape;
                Vector2 local = cs.getPosition();
                return new Vector2(bodyPos.x + local.x, bodyPos.y + local.y);
            }
            case Polygon: {
                com.badlogic.gdx.physics.box2d.PolygonShape ps = (com.badlogic.gdx.physics.box2d.PolygonShape) shape;
                float cx = 0, cy = 0;
                int count = ps.getVertexCount();
                Vector2 v = new Vector2();
                for (int i = 0; i < count; i++) {
                    ps.getVertex(i, v);
                    cx += v.x; cy += v.y;
                }
                cx /= count; cy /= count;
                return new Vector2(bodyPos.x + cx, bodyPos.y + cy);
            }
            default:
                return new Vector2(bodyPos.x, bodyPos.y);
        }
    }

    public boolean isCombatTransitionActive() {
        return combatTransitionActive;
    }

    private void finishCombatTransition() {
        combatTransitionActive = false;
        setPlayerRooted(false);
        game.addScreen(pendingCombatScreen);
        game.setScreen(CombatScreen.class);
        pendingCombatScreen = null;
    }

    private void handleCombatReturn() {
        CombatResult result = game.takePendingCombatResult();
        if (result == null) return;
        String enemyId = game.getPendingCombatEnemyId();
        game.setPendingCombatEnemyId(null);
        float combatPlayerHp = game.takePendingCombatPlayerHp();
        game.clearPendingCombatHeartScreen();
        game.clearPendingCombatHeartNormalized();

        CombatScreen combatScreen = game.getScreen(CombatScreen.class);
        if (combatScreen != null) {
            java.util.List<String> consumedIds = combatScreen.getConsumedItemIds();
            if (!consumedIds.isEmpty()) {
                PlayerSessionState session = game.getPlayerSessionState();
                if (session != null) {
                    Item[] slots = session.getInventorySlots();
                    if (slots != null) {
                        for (String consumedId : consumedIds) {
                            for (int i = 0; i < slots.length; i++) {
                                if (slots[i] != null && consumedId.equals(slots[i].getId())) {
                                    slots[i] = null;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        combatTriggerCooldownFrames = 3;
        combatTriggerSystem.setProcessing(false);

        switch (result) {
            case VICTORY:
                if (enemyId != null) game.getWonCombats().add(enemyId);
                ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
                if (players.size() > 0) {
                    Life life = Life.MAPPER.get(players.first());
                    if (life != null) {
                        life.restore(life.getBaseMaxLife(), combatPlayerHp);
                    }
                }
                PlayerSessionState session = game.getPlayerSessionState();
                if (session != null) {
                    game.setPlayerSessionState(session.withLife(combatPlayerHp));
                }
                break;
            case DEFEAT:
                pendingCombatDeath = true;
                break;
        }
    }

    private void closeOpenStorage() {
        if (openStorageEntity == null) return;
        Storage storage = Storage.MAPPER.get(openStorageEntity);
        if (storage != null) {
            com.github.MichalKC.manylands.component.Tiled tiled = com.github.MichalKC.manylands.component.Tiled.MAPPER.get(openStorageEntity);
            if (tiled != null && tiled.getLayerName() != null) {
                MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
                if (worldState != null) {
                    String storageKey = WorldStatePersistence.objectKey(tiled.getLayerName(), tiled.getMapObjectRef());
                    worldState.putStorageSlots(storageKey, storage.getSlots().clone());
                }
            }
        }
        Animation2D anim = Animation2D.MAPPER.get(openStorageEntity);
        Interactable interactable = Interactable.MAPPER.get(openStorageEntity);
        if (anim != null && interactable != null && interactable.isActivated()) {
            anim.setType(Animation2D.AnimationType.TO_IDLE);
            anim.setPlayMode(Animation.PlayMode.NORMAL);
        }
        openStorageEntity = null;
    }


    private void initGameOverInputAdapter() {
        if (gameOverInputAdapter == null) {
            gameOverInputAdapter = new com.badlogic.gdx.InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (gameOverActive && gameOverButtonsVisible) {
                        return handleGameOverInput(keycode);
                    }
                    return false;
                }

                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                    if (button != 0) return false;
                    if (gameOverActive && gameOverButtonsVisible) {
                        return handleGameOverClick(screenX, screenY);
                    }
                    return false;
                }
            };
        }
    }

    private void initGameOverResources() {
        if (gameOverBatch == null) {
            gameOverBatch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        }
        if (gameOverShapeRenderer == null) {
            gameOverShapeRenderer = new ShapeRenderer();
        }
        if (gameOverFont == null) {
            gameOverFont = skin.getFont("hercules_gameover");
        }
        if (gameOverLayout == null) {
            gameOverLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        }
    }

    private void disposeGameOverResources() {
        if (gameOverBatch != null) {
            gameOverBatch.dispose();
            gameOverBatch = null;
        }
        if (gameOverShapeRenderer != null) {
            gameOverShapeRenderer.dispose();
            gameOverShapeRenderer = null;
        }
        gameOverFont = null;
        gameOverLayout = null;
    }

    private void resetGameOverState() {
        gameOverActive = false;
        gameOverTimer = 0f;
        gameOverGrayscale = 0f;
        gameOverMessage = null;
        gameOverSelectedButton = 0;
        gameOverButtonsVisible = false;
        RespawnSystem respawnSystem = engine.getSystem(RespawnSystem.class);
        if (respawnSystem != null) {
            respawnSystem.setPaused(false);
        }
    }

    private void checkGameOverTrigger() {
        if (gameOverActive || suppressGameOver) return;

        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class, Dead.class, Animation2D.class).get());
        if (players.size() == 0) return;

        Entity player = players.first();
        Animation2D anim = Animation2D.MAPPER.get(player);
        if (anim == null) return;

        if (anim.isFinished()) {
            startGameOver();
        }
    }

    private void startGameOver() {
        gameOverActive = true;
        gameOverTimer = 0f;
        gameOverGrayscale = 0f;
        gameOverMessage = GAMEOVER_MESSAGES[(int)(Math.random() * GAMEOVER_MESSAGES.length)];
        gameOverSelectedButton = 0;
        gameOverButtonsVisible = false;

        RespawnSystem respawnSystem = engine.getSystem(RespawnSystem.class);
        if (respawnSystem != null) {
            respawnSystem.setPaused(true);
        }

        keyboardController.setActiveState(UiControllerState.class);
    }

    private void updateGameOver(float delta) {
        if (!gameOverActive) return;

        gameOverTimer += delta;

        if (gameOverTimer < GAMEOVER_FADE_DURATION) {
            gameOverGrayscale = gameOverTimer / GAMEOVER_FADE_DURATION;
        } else {
            gameOverGrayscale = 1f;
        }

        gameOverButtonsVisible = gameOverTimer >= (GAMEOVER_TEXT_DELAY + GAMEOVER_BUTTONS_DELAY);
    }

    private void renderGameOver() {
        if (!gameOverActive) return;

        float vw = uiViewport.getWorldWidth();
        float vh = uiViewport.getWorldHeight();

        if (gameOverGrayscale > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            gameOverShapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
            gameOverShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float gray = 0.3f * gameOverGrayscale;
            gameOverShapeRenderer.setColor(gray, gray, gray, 0.6f * gameOverGrayscale);
            gameOverShapeRenderer.rect(0, 0, vw, vh);
            gameOverShapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (gameOverTimer >= GAMEOVER_TEXT_DELAY) {
            gameOverBatch.setProjectionMatrix(uiViewport.getCamera().combined);
            gameOverBatch.begin();

            float textAlpha = Math.min(1f, (gameOverTimer - GAMEOVER_TEXT_DELAY) / 0.5f);

            if (gameOverMessage != null) {
                gameOverFont.getData().setScale(0.45f);
                gameOverFont.setColor(1f, 1f, 1f, textAlpha);
                gameOverLayout.setText(gameOverFont, gameOverMessage);
                float x = (vw - gameOverLayout.width) / 2f;
                float y = vh * 0.65f + gameOverLayout.height / 2f;
                gameOverFont.draw(gameOverBatch, gameOverLayout, x, y);
            }

            gameOverBatch.end();

            if (gameOverButtonsVisible) {
                float buttonsStartTime = GAMEOVER_TEXT_DELAY + GAMEOVER_BUTTONS_DELAY;
                float buttonAlpha = Math.min(1f, (gameOverTimer - buttonsStartTime) / 0.5f);

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                renderGameOverButtons(vw, vh, buttonAlpha);
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }
    }

    private void renderGameOverButtons(float sw, float sh, float alpha) {
        String label = "Resume";
        float btnW = 80f;
        float btnH = 24f;
        float x = (sw - btnW) / 2f;
        float y = sh * 0.32f;

        gameOverShapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);

        drawGameOverButton(x, y, btnW, btnH, true, alpha);

        gameOverBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        gameOverBatch.begin();
        Color textCol = new Color(0f, 0f, 0f, alpha);
        gameOverFont.setColor(textCol);
        gameOverFont.getData().setScale(0.35f);
        gameOverLayout.setText(gameOverFont, label);
        float tx = x + (btnW - gameOverLayout.width) / 2f;
        float ty = y + (btnH + gameOverLayout.height) / 2f;
        gameOverFont.draw(gameOverBatch, label, tx, ty);
        gameOverBatch.end();
    }

    private void drawGameOverButton(float x, float y, float w, float h, boolean active, float alpha) {
        gameOverShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Color fillCol = active ? new Color(1f, 1f, 1f, alpha) : new Color(0.07f, 0.07f, 0.07f, alpha);
        gameOverShapeRenderer.setColor(fillCol);
        gameOverShapeRenderer.rect(x, y, w, h);

        Color border = active ? new Color(0f, 0f, 0f, alpha) : new Color(0.55f, 0.55f, 0.55f, alpha);
        gameOverShapeRenderer.setColor(border);

        for (float i = x + 2; i < x + w - 2; i++) gameOverShapeRenderer.rect(i, y + h - 1, 1, 1);
        for (float i = x + 2; i < x + w - 2; i++) gameOverShapeRenderer.rect(i, y, 1, 1);
        for (float j = y + 2; j < y + h - 2; j++) gameOverShapeRenderer.rect(x, j, 1, 1);
        for (float j = y + 2; j < y + h - 2; j++) gameOverShapeRenderer.rect(x + w - 1, j, 1, 1);

        gameOverShapeRenderer.rect(x + 1, y + h - 2, 1, 1);
        gameOverShapeRenderer.rect(x + 1, y + 1, 1, 1);
        gameOverShapeRenderer.rect(x + w - 2, y + h - 2, 1, 1);
        gameOverShapeRenderer.rect(x + w - 2, y + 1, 1, 1);

        if (active) {
            for (float i = x + 2; i < x + w - 2; i++) gameOverShapeRenderer.rect(i, y + h - 2, 1, 1);
            for (float i = x + 2; i < x + w - 2; i++) gameOverShapeRenderer.rect(i, y + 1, 1, 1);
            for (float j = y + 2; j < y + h - 2; j++) gameOverShapeRenderer.rect(x + 1, j, 1, 1);
            for (float j = y + 2; j < y + h - 2; j++) gameOverShapeRenderer.rect(x + w - 2, j, 1, 1);
        }

        gameOverShapeRenderer.end();
    }

    private boolean handleGameOverInput(int keycode) {
        if (!gameOverActive || !gameOverButtonsVisible) return false;
        if (resumeTransitionActive) return true;

        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            onGameOverButtonSelected();
            return true;
        }
        return false;
    }

    private boolean handleGameOverClick(int screenX, int screenY) {
        if (!gameOverActive || !gameOverButtonsVisible) return false;
        if (resumeTransitionActive) return true;

        com.badlogic.gdx.math.Vector2 stageCoords = stage.screenToStageCoordinates(new com.badlogic.gdx.math.Vector2(screenX, screenY));
        float wx = stageCoords.x;
        float wy = stageCoords.y;

        float btnW = 80f;
        float btnH = 24f;
        float sw = uiViewport.getWorldWidth();
        float sh = uiViewport.getWorldHeight();
        float x = (sw - btnW) / 2f;
        float y = sh * 0.32f;

        if (wx >= x && wx <= x + btnW && wy >= y && wy <= y + btnH) {
            onGameOverButtonSelected();
            return true;
        }
        return false;
    }

    private void onGameOverButtonSelected() {
        startResumeTransition();
    }

    private void doGameOverRespawn() {
        RespawnSystem respawnSystem = engine.getSystem(RespawnSystem.class);
        if (respawnSystem != null) {
            respawnSystem.setPaused(false);
        }

        reloadMapFromCheckpoint();

        suppressGameOver = true;
        suppressGameOverTimer = 0.75f;
        resetGameOverState();
        keyboardController.setActiveState(GameControllerState.class);
    }

    private void reloadMapFromCheckpoint() {
        game.restoreCheckpoint();

        engine.removeAllEntities();

        TiledMap map;
        MapAsset mapAsset = tiledService.getCurrentMap().getProperties().get("mapAsset", MapAsset.class);
        if (mapAsset != null) {
            map = tiledService.loadMap(mapAsset);
        } else {
            String mapFile = tiledService.getCurrentMap().getProperties().get("mapFile", String.class);
            map = tiledService.loadMapFromFile(mapFile);
        }
        tiledService.setMap(map);

        PlayerSessionState session = game.getPlayerSessionState();
        if (session != null) {
            PlayerSpawnHelper.applyPlayerWorldPosition(engine, session.getSpawnPositionX(), session.getSpawnPositionY());

            PlayerStatePersistence.apply(engine, session, viewModel);
        }

        restoreDroppedCoins();

        CameraSystem camSys = this.engine.getSystem(CameraSystem.class);
        if (camSys != null) {
            camSys.setMap(map);
            camSys.requestSnap();
        }

        resetPlayerMotion();
    }

    private void resetPlayerMotion() {
        for (Entity e : engine.getEntitiesFor(Family.all(Player.class, Physic.class).get())) {
            Physic physic = Physic.MAPPER.get(e);
            com.badlogic.gdx.physics.box2d.Body body = physic.getBody();
            body.setLinearVelocity(0f, 0f);
            body.setAngularVelocity(0f);
            Move move = Move.MAPPER.get(e);
            if (move != null) {
                move.getDirection().setZero();
            }
            break;
        }
    }

    private void forceRespawnIfDead() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class, Dead.class).get());
        if (players.size() == 0) return;

        Entity player = players.first();
        Respawn respawn = Respawn.MAPPER.get(player);
        if (respawn == null) return;

        respawn.restoreCountdown(0f);
        suppressGameOver = true;
        suppressGameOverTimer = 0.75f;
    }

    private void startResumeTransition() {
        resumeTransitionActive = true;
        resumeTransitionTimer = 0f;
        resumeTransitionRespawned = false;
        resumeTransitionShouldRespawn = true;
        resumeTransitionBlackDrawn = false;
        gameOverButtonsVisible = false;
    }

    private void startStartupTransition() {
        resumeTransitionActive = true;
        resumeTransitionTimer = 0f;
        resumeTransitionRespawned = false;
        resumeTransitionShouldRespawn = false;
        resumeTransitionBlackDrawn = false;
    }

    private void startStartupFadeInOnly() {
        resumeTransitionActive = true;
        resumeTransitionTimer = RESUME_TRANS_OUT + RESUME_TRANS_HOLD;
        resumeTransitionRespawned = true;
        resumeTransitionShouldRespawn = false;
        resumeTransitionBlackDrawn = true;
    }

    public static void requestStartupFadeInOnly() {
        startupInOnly = true;
    }

    private void updateResumeTransition(float delta) {
        if (!resumeTransitionActive) return;

        resumeTransitionTimer += delta;

        if (resumeTransitionTimer >= RESUME_TRANS_OUT && !resumeTransitionRespawned) {
            if (resumeTransitionShouldRespawn) {
                if (resumeTransitionBlackDrawn) {
                    doGameOverRespawn();
                    resumeTransitionRespawned = true;
                }
            } else {
                resumeTransitionRespawned = true;
            }
        }

        if (resumeTransitionTimer >= (RESUME_TRANS_OUT + RESUME_TRANS_HOLD + RESUME_TRANS_IN)) {
            resumeTransitionActive = false;
        }
    }

    private void renderResumeTransition() {
        if (!resumeTransitionActive) return;

        float vw = uiViewport.getWorldWidth();
        float vh = uiViewport.getWorldHeight();

        float xc = vw / 2f;
        float yc = vh / 2f;

        float r;
        if (resumeTransitionTimer < RESUME_TRANS_OUT) {
            float t = resumeTransitionTimer / RESUME_TRANS_OUT;
            r = 200f * (1f - t);
            gameOverShapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
            drawCircularCutout(xc, yc, r, Color.BLACK);
        } else if (resumeTransitionTimer < RESUME_TRANS_OUT + RESUME_TRANS_HOLD) {
            resumeTransitionBlackDrawn = true;
            gameOverShapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
            gameOverShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            gameOverShapeRenderer.setColor(Color.BLACK);
            gameOverShapeRenderer.rect(0, 0, vw, vh);
            gameOverShapeRenderer.end();
        } else {
            float t = (resumeTransitionTimer - (RESUME_TRANS_OUT + RESUME_TRANS_HOLD)) / RESUME_TRANS_IN;
            r = 200f * Math.min(1f, t);
            gameOverShapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
            drawCircularCutout(xc, yc, r, Color.BLACK);
        }
    }

    private void drawCircularCutout(float xc, float yc, float r, Color color) {
        float vw = uiViewport.getWorldWidth();
        float vh = uiViewport.getWorldHeight();

        if (r <= 0f) {
            gameOverShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            gameOverShapeRenderer.setColor(color);
            gameOverShapeRenderer.rect(0, 0, vw, vh);
            gameOverShapeRenderer.end();
            return;
        }

        gameOverShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameOverShapeRenderer.setColor(color);
        int segments = 40;
        float rOut = 1000f;
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

            gameOverShapeRenderer.triangle(x1, y1, x2, y2, x3, y3);
            gameOverShapeRenderer.triangle(x1, y1, x3, y3, x4, y4);
        }
        gameOverShapeRenderer.end();
    }

    @Override
    public void dispose() {
        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }
        if (inventoryView != null) {
            inventoryView.disposeOwnedTextures();
        }
        if (shopView != null) {
            shopView.disposeOwnedTextures();
        }
        this.physicWorld.dispose();
        this.stage.dispose();
        if (transShapeRenderer != null) transShapeRenderer.dispose();
        if (transBatch != null) transBatch.dispose();
        if (transFont != null) transFont.dispose();
        cursorNormal = null;
        cursorPressed = null;
    }
}
