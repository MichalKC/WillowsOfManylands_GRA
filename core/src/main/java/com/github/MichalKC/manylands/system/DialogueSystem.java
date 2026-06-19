package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.MichalKC.manylands.component.Animation2D;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Dialogue;
import com.github.MichalKC.manylands.component.Interactable;
import com.github.MichalKC.manylands.component.Move;
import com.github.MichalKC.manylands.component.Npc;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.SkillsStore;
import com.github.MichalKC.manylands.component.Store;
import com.github.MichalKC.manylands.ui.model.GameViewModel;
import java.util.function.Consumer;

public class DialogueSystem extends EntitySystem {

    private final GameViewModel viewModel;
    private final Family playerFamily = Family.all(Player.class, Move.class).get();
    private Runnable closeCallback;
    private Consumer<Entity> storePromptCallback;

    private Entity activeEntity;
    private String[] activeLines;
    private int currentLine;
    private boolean active;

    public DialogueSystem(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.active = false;
    }

    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void setStorePromptCallback(Consumer<Entity> callback) {
        this.storePromptCallback = callback;
    }

    public boolean isActive() {
        return active;
    }

    public void startDialogue(Entity entity) {
        Dialogue dialogue = Dialogue.MAPPER.get(entity);
        if (dialogue == null) return;
        String[][] variants = dialogue.getVariants();
        if (variants == null || variants.length == 0) return;
        this.activeEntity = entity;
        int idx = (int) Math.floor(Math.random() * variants.length);
        if (idx < 0 || idx >= variants.length) idx = 0;
        this.activeLines = variants[idx];
        this.currentLine = 0;
        this.active = true;
        setPlayerRooted(true);
        setNpcInDialogue(entity, true);
        viewModel.showTalkingText(activeLines[0]);
        if (activeLines.length == 1
                && (Store.MAPPER.get(entity) != null || SkillsStore.MAPPER.get(entity) != null)
                && storePromptCallback != null) {
            storePromptCallback.accept(entity);
        }
    }

    public void advance() {
        if (!active) return;
        currentLine++;
        if (currentLine < activeLines.length) {
            viewModel.showTalkingText(activeLines[currentLine]);
            if (currentLine == activeLines.length - 1
                    && activeEntity != null
                    && (Store.MAPPER.get(activeEntity) != null || SkillsStore.MAPPER.get(activeEntity) != null)
                    && storePromptCallback != null) {
                storePromptCallback.accept(activeEntity);
            }
        } else {
            endDialogue();
        }
    }

    public void endDialogue() {
        if (activeEntity != null) {
            Animation2D anim = Animation2D.MAPPER.get(activeEntity);
            Interactable interactable = Interactable.MAPPER.get(activeEntity);
            if (anim != null && interactable != null && interactable.isActivated()) {
                anim.setType(AnimationType.TO_IDLE);
                anim.setPlayMode(Animation.PlayMode.NORMAL);
            }
            setNpcInDialogue(activeEntity, false);
        }
        active = false;
        activeEntity = null;
        activeLines = null;
        currentLine = 0;
        setPlayerRooted(false);
        viewModel.hideTalkingText();
        if (closeCallback != null) {
            closeCallback.run();
        }
    }

    private void setPlayerRooted(boolean rooted) {
        Engine engine = getEngine();
        if (engine == null) return;
        ImmutableArray<Entity> players = engine.getEntitiesFor(playerFamily);
        for (int i = 0; i < players.size(); i++) {
            Move move = Move.MAPPER.get(players.get(i));
            if (move != null) move.setRooted(rooted);
        }
    }

    private void setNpcInDialogue(Entity entity, boolean inDialogue) {
        Npc npc = Npc.MAPPER.get(entity);
        if (npc != null) {
            npc.setInDialogue(inDialogue);
        }
    }

    @Override
    public void update(float deltaTime) {
    }
}
