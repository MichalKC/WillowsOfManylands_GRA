package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.component.Animation2D;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Controller;
import com.github.MichalKC.manylands.component.Interactable;
import com.github.MichalKC.manylands.component.TextDisplay;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Dialogue;
import com.github.MichalKC.manylands.component.Inventory;
import com.github.MichalKC.manylands.component.Item;
import com.github.MichalKC.manylands.component.Npc;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Storage;
import com.github.MichalKC.manylands.component.Transform;
import com.github.MichalKC.manylands.input.Command;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

import java.util.function.Consumer;

public class InteractableSystem extends EntitySystem {
    private static final float TEXT_ONLY_RADIUS = 56f * GdxGame.UNIT_SCALE;
    private final Engine engine;
    private final GameViewModel viewModel;
    private com.github.MichalKC.manylands.screen.GameScreen gameScreen;
    private final Family interactableFamily =
        Family.all(Interactable.class, Animation2D.class, Transform.class).get();
    private final Family textOnlyFamily =
        Family.all(TextDisplay.class, Transform.class).exclude(Interactable.class).get();
    private final Family textTriggerFamily =
        Family.all(TextDisplay.class).exclude(Transform.class).get();
    private final Family playerFamily =
        Family.all(Player.class, Controller.class, Transform.class).get();
    private final Vector2 tmpPlayer = new Vector2();
    private final Vector2 tmpInteractablePoint = new Vector2();
    private Consumer<Entity> storageOpenCallback;
    private Consumer<Entity> dialogueCallback;
    private boolean dialogueLock = false;

    public InteractableSystem(Engine engine, GameViewModel viewModel) {
        this.engine = engine;
        this.viewModel = viewModel;
    }

    public void setStorageOpenCallback(Consumer<Entity> storageOpenCallback) {
        this.storageOpenCallback = storageOpenCallback;
    }

    public void setDialogueCallback(Consumer<Entity> dialogueCallback) {
        this.dialogueCallback = dialogueCallback;
    }

    public void setDialogueLock(boolean locked) {
        this.dialogueLock = locked;
    }

    public void setGameScreen(com.github.MichalKC.manylands.screen.GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void update(float deltaTime) {
        completeTransitions();
        if (dialogueLock || (gameScreen != null && gameScreen.isCombatTransitionActive())) {
            viewModel.setInteractHintVisible(false);
            return;
        }
        updatePrompt();
        processInteractInput();
    }

    private void completeTransitions() {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(interactableFamily);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Animation2D anim = Animation2D.MAPPER.get(entity);
            Interactable interactable = Interactable.MAPPER.get(entity);
            if (anim.getAnimation() == null) {
                continue;
            }
            if (anim.getType() == AnimationType.TO_ACTIVATED && anim.isFinished()) {
                anim.setType(AnimationType.ACTIVATED);
                anim.setPlayMode(Animation.PlayMode.LOOP);
                interactable.setActivated(true);
                interactable.setActivatedStage(anim.getStage());
                openStorage(entity);
                openDialogue(entity);
            } else if (anim.getType() == AnimationType.TO_IDLE && anim.isFinished()) {
                anim.setType(AnimationType.IDLE);
                anim.setStage(1);
                anim.setPlayMode(Animation.PlayMode.LOOP);
                interactable.setActivated(false);
                interactable.setActivatedStage(0);
            }
        }
    }

    private void updatePrompt() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(playerFamily);
        if (players.size() == 0) {
            viewModel.setInteractHintVisible(false);
            return;
        }
        Entity player = players.first();
        interactionSamplePoint(Transform.MAPPER.get(player), tmpPlayer);
        boolean anyInRange = findClosestInteractable(tmpPlayer, false) != null
            || findClosestTextOnly(tmpPlayer) != null;
        viewModel.setInteractHintVisible(anyInRange);
    }

    private void processInteractInput() {
        ImmutableArray<Entity> players = engine.getEntitiesFor(playerFamily);
        if (players.size() == 0) {
            return;
        }
        Entity player = players.first();
        Controller controller = Controller.MAPPER.get(player);
        if (!controller.getPressedCommands().contains(Command.INTERACT)) {
            return;
        }
        interactionSamplePoint(Transform.MAPPER.get(player), tmpPlayer);
        Entity target = findClosestInteractable(tmpPlayer, true);
        if (target != null) {
            com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(player);
            if (skills != null && skills.isSkill4Active()) {
                skills.cancelSkill4Early();
            }
            Item item = Item.MAPPER.get(target);
            if (item != null) {
                Inventory playerInv = Inventory.MAPPER.get(player);
                if (playerInv != null) {
                    if (playerInv.addItem(item)) {
                        viewModel.getGame().getAudioService().playSound(com.github.MichalKC.manylands.asset.SoundAsset.COIN);
                        engine.removeEntity(target);
                    }
                }
                controller.getPressedCommands().remove(Command.INTERACT);
                return;
            }
            Animation2D anim = Animation2D.MAPPER.get(target);
            Interactable interactable = Interactable.MAPPER.get(target);
            Dialogue dialogue = Dialogue.MAPPER.get(target);
            if (dialogue != null) {
                if (Npc.MAPPER.get(target) != null) {
                    openDialogue(target);
                } else if (!interactable.isActivated()) {
                    anim.setStage(1);
                    anim.setType(AnimationType.TO_ACTIVATED);
                    anim.setPlayMode(Animation.PlayMode.NORMAL);
                } else {
                    openDialogue(target);
                }
                controller.getPressedCommands().remove(Command.INTERACT);
                return;
            }
            TextDisplay textDisplay = TextDisplay.MAPPER.get(target);
            if (textDisplay != null) {
                clearTextTriggers();
                Entity textEntity = new Entity();
                textEntity.add(new TextDisplay(textDisplay.getText(), textDisplay.getDisplayDuration()));
                engine.addEntity(textEntity);
            }
            Storage storage = Storage.MAPPER.get(target);
            if (!interactable.isActivated()) {
                anim.setStage(1);
                anim.setType(AnimationType.TO_ACTIVATED);
            } else if (interactable.getActivatedStage() < interactable.getActivatedCount()) {
                anim.setStage(interactable.getActivatedStage() + 1);
                anim.setType(AnimationType.TO_ACTIVATED);
            } else if (storage != null) {
                openStorage(target);
                controller.getPressedCommands().remove(Command.INTERACT);
                return;
            } else {
                anim.setType(AnimationType.TO_IDLE);
            }
            anim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            Entity textTarget = findClosestTextOnly(tmpPlayer);
            if (textTarget != null) {
                com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(player);
                if (skills != null && skills.isSkill4Active()) {
                    skills.cancelSkill4Early();
                }
                TextDisplay textDisplay = TextDisplay.MAPPER.get(textTarget);
                clearTextTriggers();
                Entity textEntity = new Entity();
                textEntity.add(new TextDisplay(textDisplay.getText(), textDisplay.getDisplayDuration()));
                engine.addEntity(textEntity);
            }
        }
        controller.getPressedCommands().remove(Command.INTERACT);
    }

    private void openStorage(Entity entity) {
        if (storageOpenCallback != null && Storage.MAPPER.get(entity) != null) {
            storageOpenCallback.accept(entity);
        }
    }

    private void openDialogue(Entity entity) {
        if (dialogueCallback != null && Dialogue.MAPPER.get(entity) != null) {
            dialogueCallback.accept(entity);
        }
    }

    private Entity findClosestInteractable(Vector2 playerPos, boolean requireStableToInteract) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(interactableFamily);
        Entity best = null;
        float bestDistSq = Float.MAX_VALUE;
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Interactable interactable = Interactable.MAPPER.get(entity);
            Transform transform = Transform.MAPPER.get(entity);
            if (Npc.MAPPER.get(entity) != null && Dead.MAPPER.get(entity) != null) {
                continue;
            }
            interactionSamplePoint(transform, tmpInteractablePoint);
            float distSq = playerPos.dst2(tmpInteractablePoint);
            if (distSq > interactable.getRadiusSq()) {
                continue;
            }
            if (requireStableToInteract && !canToggle(entity, Animation2D.MAPPER.get(entity), interactable)) {
                continue;
            }
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = entity;
            }
        }
        return best;
    }

    private boolean canToggle(Entity entity, Animation2D anim, Interactable interactable) {
        if (Npc.MAPPER.get(entity) != null) {
            if (Dead.MAPPER.get(entity) != null) return false;
            return true;
        }
        if (anim.getPlayMode() != Animation.PlayMode.LOOP) {
            return false;
        }
        AnimationType type = anim.getType();
        if (!interactable.isActivated() && type == AnimationType.IDLE) {
            return true;
        }
        return interactable.isActivated() && type == AnimationType.ACTIVATED;
    }

    private void clearTextTriggers() {
        ImmutableArray<Entity> triggers = engine.getEntitiesFor(textTriggerFamily);
        for (int i = triggers.size() - 1; i >= 0; i--) {
            engine.removeEntity(triggers.get(i));
        }
    }

    private Entity findClosestTextOnly(Vector2 playerPos) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(textOnlyFamily);
        Entity best = null;
        float bestDistSq = Float.MAX_VALUE;
        float radiusSq = TEXT_ONLY_RADIUS * TEXT_ONLY_RADIUS;
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Transform transform = Transform.MAPPER.get(entity);
            interactionSamplePoint(transform, tmpInteractablePoint);
            float distSq = playerPos.dst2(tmpInteractablePoint);
            if (distSq <= radiusSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                best = entity;
            }
        }
        return best;
    }

    private void interactionSamplePoint(Transform transform, Vector2 out) {
        out.set(transform.getPosition());
    }
}
