package com.github.MichalKC.manylands.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.MichalKC.manylands.component.Animation2D;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Attack;
import com.github.MichalKC.manylands.component.Attack.AttackType;
import com.github.MichalKC.manylands.component.Damaged;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Fsm;
import com.github.MichalKC.manylands.component.Life;
import com.github.MichalKC.manylands.component.Move;

public enum AnimationState implements State<Entity> {
    IDLE {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.IDLE);
        }

        @Override
        public void update(Entity entity) {
            if (isDead(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            if (hasDamage(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
                return;
            }

            Move move = Move.MAPPER.get(entity);
            if (move != null && !move.isRooted() && !move.getDirection().isZero()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(RUN);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack != null && attack.isAttacking()) {
                if (attack.getActiveAttackType() == AttackType.SECONDARY) {
                    Fsm.MAPPER.get(entity).getAnimationFsm().changeState(ATTACK2);
                } else if (attack.getActiveAttackType() == AttackType.SPECIAL) {
                    Fsm.MAPPER.get(entity).getAnimationFsm().changeState(SPECIAL);
                } else {
                    Fsm.MAPPER.get(entity).getAnimationFsm().changeState(ATTACK);
                }
                return;
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    RUN {
        @Override
        public void enter (Entity entity){
            Animation2D.MAPPER.get(entity).setType(AnimationType.RUN);
        }

        @Override
        public void update (Entity entity){
            if (isDead(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            if (hasDamage(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
                return;
            }

            Move move = Move.MAPPER.get(entity);
            if(move == null || move.getDirection().isZero() || move.isRooted()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit (Entity entity){
        }

        @Override
        public boolean onMessage (Entity entity, Telegram telegram){
            return false;
        }
    },

    ATTACK {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.ATTACK);
        }

        @Override
        public void update(Entity entity) {
            if (isDead(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            if (hasDamage(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack.canAttack()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    ATTACK2 {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.ATTACK2);
        }

        @Override
        public void update(Entity entity) {
            if (isDead(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            if (hasDamage(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack.canAttack()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    SPECIAL {
        @Override
        public void enter(Entity entity) {
            Animation2D.MAPPER.get(entity).setType(AnimationType.SPECIAL);
        }

        @Override
        public void update(Entity entity) {
            if (isDead(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            if (hasDamage(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DAMAGED);
                return;
            }

            Attack attack = Attack.MAPPER.get(entity);
            if (attack.canAttack()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    DAMAGED {
        @Override
        public void enter(Entity entity) {
            Animation2D animation2D = Animation2D.MAPPER.get(entity);
            animation2D.setType(AnimationType.DAMAGED);
            animation2D.setPlayMode(Animation.PlayMode.NORMAL);
            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.setRooted(true);
            }
        }

        @Override
        public void update(Entity entity) {
            if (isDead(entity)) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(DEAD);
                return;
            }
            Animation2D animation2D = Animation2D.MAPPER.get(entity);
            if (animation2D.isFinished()) {
                Fsm.MAPPER.get(entity).getAnimationFsm().changeState(IDLE);
            }
        }

        @Override
        public void exit(Entity entity) {
            Animation2D.MAPPER.get(entity).setPlayMode(Animation.PlayMode.LOOP);
            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.setRooted(false);
            }
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    },

    DEAD {
        @Override
        public void enter(Entity entity) {
            Animation2D animation2D = Animation2D.MAPPER.get(entity);
            animation2D.setType(AnimationType.DEAD);
            animation2D.setPlayMode(Animation.PlayMode.NORMAL);
        }

        @Override
        public void update(Entity entity) {
        }

        @Override
        public void exit(Entity entity) {
            Animation2D.MAPPER.get(entity).setPlayMode(Animation.PlayMode.LOOP);
        }

        @Override
        public boolean onMessage(Entity entity, Telegram telegram) {
            return false;
        }
    };

    private static boolean hasDamage(Entity entity) {
        return Damaged.MAPPER.get(entity) != null;
    }

    private static boolean isDead(Entity entity) {
        if (Dead.MAPPER.get(entity) != null) {
            return true;
        }
        Life life = Life.MAPPER.get(entity);
        return life != null && life.getLife() <= 0f;
    }
}
