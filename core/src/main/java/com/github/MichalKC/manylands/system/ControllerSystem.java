package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.component.Attack;
import com.github.MichalKC.manylands.component.Controller;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Move;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Attack.AttackType;
import com.github.MichalKC.manylands.input.Command;
import com.github.MichalKC.manylands.screen.MainMenuScreen;

public class ControllerSystem extends IteratingSystem {
    private final GdxGame game;
    private com.github.MichalKC.manylands.screen.GameScreen gameScreen;

    public ControllerSystem(GdxGame game) {
        super(Family.all(Controller.class).get());
        this.game = game;
    }

    public void setGameScreen(com.github.MichalKC.manylands.screen.GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (Dead.MAPPER.get(entity) != null) {
            Controller controller = Controller.MAPPER.get(entity);
            controller.getPressedCommands().clear();
            controller.getReleasedCommands().clear();
            return;
        }

        Controller controller = Controller.MAPPER.get(entity);
        if(controller.getPressedCommands().isEmpty() && controller.getReleasedCommands().isEmpty()) {
            return;
        }

        for (Command command : controller.getPressedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, 1f);
                case DOWN -> moveEntity(entity, 0f, -1f);
                case LEFT -> moveEntity(entity, -1f, 0f);
                case RIGHT -> moveEntity(entity, 1f, 0f);
                case SELECT -> {
                    if (gameScreen == null || !gameScreen.isCombatTransitionActive()) {
                        startEntityAttack(entity, AttackType.PRIMARY);
                    }
                }
                case ATTACK2 -> {
                    if (gameScreen == null || !gameScreen.isCombatTransitionActive()) {
                        startEntityAttack(entity, AttackType.SECONDARY);
                    }
                }
                case SPECIAL -> {
                    if (gameScreen == null || !gameScreen.isCombatTransitionActive()) {
                        startEntityAttack(entity, AttackType.SPECIAL);
                    }
                }
                case INTERACT -> {
                }
                case CANCEL -> game.setScreen(MainMenuScreen.class);
            }
        }
        controller.getPressedCommands().clear();

        for (Command command : controller.getReleasedCommands()) {
            switch (command) {
                case UP -> moveEntity(entity, 0f, -1f);
                case DOWN -> moveEntity(entity, 0f, 1f);
                case LEFT -> moveEntity(entity, 1f, 0f);
                case RIGHT -> moveEntity(entity, -1f, 0f);
            }
        }
        controller.getReleasedCommands().clear();
    }

    private void startEntityAttack(Entity entity, AttackType attackType) {
        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null && attack.canAttack()) {
            attack.startAttack(attackType);
        }
    }

    private void moveEntity(Entity entity, float directionX, float directionY) {
        if (Player.MAPPER.get(entity) != null) {
            return;
        }
        Move move = Move.MAPPER.get(entity);
        if (move == null) return;

        move.getDirection().x += directionX;
        move.getDirection().y += directionY;
    }
}
