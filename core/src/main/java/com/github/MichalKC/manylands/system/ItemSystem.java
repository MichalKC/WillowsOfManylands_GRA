package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.World;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.component.Inventory;
import com.github.MichalKC.manylands.component.Item;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.ui.model.GameViewModel;
import com.github.MichalKC.manylands.world.MapWorldState;
import com.github.MichalKC.manylands.tiled.TiledService;

public class ItemSystem extends EntitySystem {
    private final Engine engine;
    private final AssetService assetService;
    private final GameViewModel viewModel;
    private final World physicWorld;
    private final TiledService tiledService;

    private final Family itemFamily = Family.all(Item.class).get();
    private final Family playerFamily = Family.all(Player.class, Inventory.class).get();

    public ItemSystem(Engine engine, AssetService assetService, GameViewModel viewModel, World physicWorld, TiledService tiledService) {
        this.engine = engine;
        this.assetService = assetService;
        this.viewModel = viewModel;
        this.physicWorld = physicWorld;
        this.tiledService = tiledService;
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> players = engine.getEntitiesFor(playerFamily);
        if (players.size() == 0) return;
        Entity player = players.first();
        Inventory playerInv = Inventory.MAPPER.get(player);

        ImmutableArray<Entity> items = engine.getEntitiesFor(itemFamily);
        for (int i = items.size() - 1; i >= 0; i--) {
            Entity entity = items.get(i);
            Item item = Item.MAPPER.get(entity);
            if (item.isMarkedForPickup()) {
                if (playerInv != null && playerInv.addItem(item)) {
                    viewModel.getGame().getAudioService().playSound(SoundAsset.LIFE_REG);
                    
                    MapWorldState worldState = viewModel.getGame().getMapWorldState(tiledService.getCurrentMapKey());
                    if (worldState != null) {
                        worldState.addPickedUpItem(item.getId());
                    }
                    
                    engine.removeEntity(entity);
                } else {
                    item.setMarkedForPickup(false);
                }
            }
        }
    }
}
