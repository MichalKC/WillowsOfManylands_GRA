package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.component.Animation2D;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Coin;
import com.github.MichalKC.manylands.component.CoinDropper;
import com.github.MichalKC.manylands.component.Facing;
import com.github.MichalKC.manylands.component.Facing.FacingDirection;
import com.github.MichalKC.manylands.component.Graphic;
import com.github.MichalKC.manylands.component.Interactable;
import com.github.MichalKC.manylands.component.Physic;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Transform;
import com.github.MichalKC.manylands.ui.model.GameViewModel;
import com.github.MichalKC.manylands.world.DroppedCoinState;

public class CoinSystem extends EntitySystem {
    private static final String COIN_ATLAS_KEY = "coin";
    private static final float COIN_SIZE_PIXELS = 24f;
    private static final float COIN_SIZE_WORLD = COIN_SIZE_PIXELS * GdxGame.UNIT_SCALE;
    private static final float COIN_SENSOR_RADIUS = COIN_SIZE_WORLD * 0.35f;
    private static final float COIN_DROP_SPREAD_WORLD = 18f * GdxGame.UNIT_SCALE;
    private static final float COIN_DROP_Y_OFFSET = -10f * GdxGame.UNIT_SCALE;
    private static final float COIN_DROP_ANIM_SPEED = 1.5f;

    private final Engine engine;
    private final AssetService assetService;
    private final GameViewModel viewModel;
    private final World physicWorld;
    private final Family dropperFamily =
        Family.all(CoinDropper.class, Interactable.class, Transform.class).get();
    private final Family coinFamily =
        Family.all(Coin.class, Transform.class, Animation2D.class).get();
    private TextureRegion idleFrame;

    public CoinSystem(Engine engine, AssetService assetService, GameViewModel viewModel, World physicWorld) {
        this.engine = engine;
        this.assetService = assetService;
        this.viewModel = viewModel;
        this.physicWorld = physicWorld;
    }

    @Override
    public void update(float deltaTime) {
        spawnCoinsForActivatedDroppers();
        advanceCoinAnimations();
        processPickups();
    }

    private void spawnCoinsForActivatedDroppers() {
        ImmutableArray<Entity> droppers = engine.getEntitiesFor(dropperFamily);
        for (int i = 0; i < droppers.size(); i++) {
            Entity entity = droppers.get(i);
            CoinDropper dropper = CoinDropper.MAPPER.get(entity);
            if (dropper.isDropped() || dropper.getAmount() <= 0) {
                continue;
            }
            Interactable interactable = Interactable.MAPPER.get(entity);
            if (!interactable.isActivated()) {
                continue;
            }
            Vector2 origin = Transform.MAPPER.get(entity).getPosition();
            spawnCoins(origin.x, origin.y, dropper.getAmount());
            dropper.setDropped(true);
        }
    }

    private void spawnCoins(float originX, float originY, int amount) {
        for (int i = 0; i < amount; i++) {
            float angle;
            float radius;
            if (amount == 1) {
                angle = MathUtils.random(0f, MathUtils.PI2);
                radius = COIN_DROP_SPREAD_WORLD * 0.25f;
            } else {
                angle = (MathUtils.PI2 * i) / amount + MathUtils.random(-0.25f, 0.25f);
                radius = COIN_DROP_SPREAD_WORLD * MathUtils.random(0.4f, 1f);
            }
            float ox = MathUtils.cos(angle) * radius;
            float oy = MathUtils.sin(angle) * radius * 0.5f;
            createCoinEntity(originX + ox, originY + oy + COIN_DROP_Y_OFFSET);
        }
    }

    private void createCoinEntity(float worldX, float worldY) {
        Entity entity = engine.createEntity();

        Vector2 position = new Vector2(worldX, worldY);
        Vector2 size = new Vector2(COIN_SIZE_WORLD, COIN_SIZE_WORLD);
        Vector2 scaling = new Vector2(1f, 1f);
        entity.add(new Transform(position, 1, size, scaling, 0f, 0f));

        entity.add(new Facing(FacingDirection.RIGHT));

        Animation2D animation = new Animation2D(
            AtlasAsset.OBJECTS,
            COIN_ATLAS_KEY,
            AnimationType.DROP,
            PlayMode.NORMAL,
            COIN_DROP_ANIM_SPEED
        );
        entity.add(animation);

        TextureRegion region = idleFrame();
        entity.add(new Graphic(Color.WHITE.cpy(), new TextureRegion(region)));

        entity.add(new Coin(Coin.DEFAULT_VALUE));

        addCoinSensorBody(entity, worldX, worldY);

        engine.addEntity(entity);
    }

    private void addCoinSensorBody(Entity entity, float bottomLeftX, float bottomLeftY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(bottomLeftX, bottomLeftY);
        bodyDef.fixedRotation = true;

        Body body = physicWorld.createBody(bodyDef);
        body.setUserData(entity);

        CircleShape shape = new CircleShape();
        shape.setPosition(new Vector2(COIN_SIZE_WORLD * 0.5f, COIN_SIZE_WORLD * 0.5f));
        shape.setRadius(COIN_SENSOR_RADIUS);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        entity.add(new Physic(body, new Vector2(body.getPosition())));
    }

    private TextureRegion idleFrame() {
        if (idleFrame == null) {
            TextureAtlas atlas = assetService.get(AtlasAsset.OBJECTS);
            TextureAtlas.AtlasRegion region = atlas.findRegion("coin/idle_right", 0);
            if (region == null) {
                region = atlas.findRegion("coin/drop_right", 0);
            }
            if (region == null) {
                throw new GdxRuntimeException(
                    "No coin regions in objects atlas. Re-run TexturePackerTool to include assets_raw/objects/coin/."
                );
            }
            idleFrame = region;
        }
        return idleFrame;
    }

    private void advanceCoinAnimations() {
        ImmutableArray<Entity> coins = engine.getEntitiesFor(coinFamily);
        for (int i = 0; i < coins.size(); i++) {
            Entity entity = coins.get(i);
            Coin coin = Coin.MAPPER.get(entity);
            if (coin.isDropAnimationFinished()) {
                continue;
            }
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim.getType() == AnimationType.DROP && anim.isFinished()) {
                anim.setType(AnimationType.IDLE);
                anim.setPlayMode(PlayMode.LOOP);
                coin.setDropAnimationFinished(true);
            }
        }
    }

    private void processPickups() {
        ImmutableArray<Entity> coins = engine.getEntitiesFor(coinFamily);
        for (int i = coins.size() - 1; i >= 0; i--) {
            Entity entity = coins.get(i);
            Coin coin = Coin.MAPPER.get(entity);
            if (!coin.isMarkedForPickup()) {
                continue;
            }
            viewModel.addCoins(coin.getValue());
            viewModel.getGame().getAudioService().playSound(SoundAsset.COIN);
            engine.removeEntity(entity);
        }
    }

    public void restoreCoins(Array<DroppedCoinState> savedCoins) {
        if (savedCoins == null || savedCoins.size == 0) {
            return;
        }
        for (int i = 0; i < savedCoins.size; i++) {
            DroppedCoinState state = savedCoins.get(i);
            createRestoredCoinEntity(state.getX(), state.getY(), state.getValue());
        }
    }

    private void createRestoredCoinEntity(float worldX, float worldY, int value) {
        Entity entity = engine.createEntity();

        Vector2 position = new Vector2(worldX, worldY);
        Vector2 size = new Vector2(COIN_SIZE_WORLD, COIN_SIZE_WORLD);
        Vector2 scaling = new Vector2(1f, 1f);
        entity.add(new Transform(position, 1, size, scaling, 0f, 0f));

        entity.add(new Facing(FacingDirection.RIGHT));

        Animation2D animation = new Animation2D(
            AtlasAsset.OBJECTS,
            COIN_ATLAS_KEY,
            AnimationType.IDLE,
            PlayMode.LOOP,
            COIN_DROP_ANIM_SPEED
        );
        entity.add(animation);

        TextureRegion region = idleFrame();
        entity.add(new Graphic(Color.WHITE.cpy(), new TextureRegion(region)));

        Coin coin = new Coin(value);
        coin.setDropAnimationFinished(true);
        entity.add(coin);

        addCoinSensorBody(entity, worldX, worldY);

        engine.addEntity(entity);
    }
}
