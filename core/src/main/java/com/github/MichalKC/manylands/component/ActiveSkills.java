package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ActiveSkills implements Component {
    public static final ComponentMapper<ActiveSkills> MAPPER = ComponentMapper.getFor(ActiveSkills.class);

    private static final float SKILL1_ACTIVE_DURATION = 10f;
    private static final float SKILL1_COOLDOWN_DURATION = 30f;
    private static final float SKILL2_ACTIVE_DURATION = 10f;
    private static final float SKILL2_COOLDOWN_DURATION = 30f;
    private static final float SKILL3_ACTIVE_DURATION = 10f;
    private static final float SKILL3_COOLDOWN_DURATION = 30f;
    private static final float SKILL4_ACTIVE_DURATION = 10f;
    private static final float SKILL4_COOLDOWN_DURATION = 30f;
    private static final float SKILL5_ACTIVE_DURATION = 10f;
    private static final float SKILL5_COOLDOWN_DURATION = 30f;
    private static final float SKILL6_ACTIVE_DURATION = 10f;
    private static final float SKILL6_COOLDOWN_DURATION = 30f;
    private static final float SKILL7_ACTIVE_DURATION = 10f;
    private static final float SKILL7_COOLDOWN_DURATION = 30f;
    private static final float SKILL8_ACTIVE_DURATION = 10f;
    private static final float SKILL8_COOLDOWN_DURATION = 30f;

    private float skill1ActiveRemaining = 0f;
    private float skill1CooldownRemaining = 0f;
    private float skill2ActiveRemaining = 0f;
    private float skill2CooldownRemaining = 0f;
    private float skill3ActiveRemaining = 0f;
    private float skill3CooldownRemaining = 0f;
    private float skill4ActiveRemaining = 0f;
    private float skill4CooldownRemaining = 0f;
    private float skill5ActiveRemaining = 0f;
    private float skill5CooldownRemaining = 0f;
    private float skill6ActiveRemaining = 0f;
    private float skill6CooldownRemaining = 0f;
    private float skill7ActiveRemaining = 0f;
    private float skill7CooldownRemaining = 0f;
    private float skill8ActiveRemaining = 0f;
    private float skill8CooldownRemaining = 0f;

    public void update(float delta) {
        if (skill1ActiveRemaining > 0f) {
            skill1ActiveRemaining = Math.max(0f, skill1ActiveRemaining - delta);
            if (skill1ActiveRemaining <= 0f) {
                skill1CooldownRemaining = SKILL1_COOLDOWN_DURATION;
            }
        } else if (skill1CooldownRemaining > 0f) {
            skill1CooldownRemaining = Math.max(0f, skill1CooldownRemaining - delta);
        }

        if (skill2ActiveRemaining > 0f) {
            skill2ActiveRemaining = Math.max(0f, skill2ActiveRemaining - delta);
            if (skill2ActiveRemaining <= 0f) {
                skill2CooldownRemaining = SKILL2_COOLDOWN_DURATION;
            }
        } else if (skill2CooldownRemaining > 0f) {
            skill2CooldownRemaining = Math.max(0f, skill2CooldownRemaining - delta);
        }

        if (skill3ActiveRemaining > 0f) {
            skill3ActiveRemaining = Math.max(0f, skill3ActiveRemaining - delta);
            if (skill3ActiveRemaining <= 0f) {
                skill3CooldownRemaining = SKILL3_COOLDOWN_DURATION;
            }
        } else if (skill3CooldownRemaining > 0f) {
            skill3CooldownRemaining = Math.max(0f, skill3CooldownRemaining - delta);
        }

        if (skill4ActiveRemaining > 0f) {
            skill4ActiveRemaining = Math.max(0f, skill4ActiveRemaining - delta);
            if (skill4ActiveRemaining <= 0f) {
                skill4CooldownRemaining = SKILL4_COOLDOWN_DURATION;
            }
        } else if (skill4CooldownRemaining > 0f) {
            skill4CooldownRemaining = Math.max(0f, skill4CooldownRemaining - delta);
        }

        if (skill5ActiveRemaining > 0f) {
            skill5ActiveRemaining = Math.max(0f, skill5ActiveRemaining - delta);
            if (skill5ActiveRemaining <= 0f) {
                skill5CooldownRemaining = SKILL5_COOLDOWN_DURATION;
            }
        } else if (skill5CooldownRemaining > 0f) {
            skill5CooldownRemaining = Math.max(0f, skill5CooldownRemaining - delta);
        }

        if (skill6ActiveRemaining > 0f) {
            skill6ActiveRemaining = Math.max(0f, skill6ActiveRemaining - delta);
            if (skill6ActiveRemaining <= 0f) {
                skill6CooldownRemaining = SKILL6_COOLDOWN_DURATION;
            }
        } else if (skill6CooldownRemaining > 0f) {
            skill6CooldownRemaining = Math.max(0f, skill6CooldownRemaining - delta);
        }

        if (skill7ActiveRemaining > 0f) {
            skill7ActiveRemaining = Math.max(0f, skill7ActiveRemaining - delta);
            if (skill7ActiveRemaining <= 0f) {
                skill7CooldownRemaining = SKILL7_COOLDOWN_DURATION;
            }
        } else if (skill7CooldownRemaining > 0f) {
            skill7CooldownRemaining = Math.max(0f, skill7CooldownRemaining - delta);
        }

        if (skill8ActiveRemaining > 0f) {
            skill8ActiveRemaining = Math.max(0f, skill8ActiveRemaining - delta);
            if (skill8ActiveRemaining <= 0f) {
                skill8CooldownRemaining = SKILL8_COOLDOWN_DURATION;
            }
        } else if (skill8CooldownRemaining > 0f) {
            skill8CooldownRemaining = Math.max(0f, skill8CooldownRemaining - delta);
        }
    }

    public boolean tryActivateSkill1() {
        if (skill1ActiveRemaining > 0f || skill1CooldownRemaining > 0f) {
            return false;
        }
        skill1ActiveRemaining = SKILL1_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill1Active() {
        return skill1ActiveRemaining > 0f;
    }

    public float getSkill1ActiveRemaining() { return skill1ActiveRemaining; }
    public float getSkill1CooldownRemaining() { return skill1CooldownRemaining; }

    public boolean tryActivateSkill2() {
        if (skill2ActiveRemaining > 0f || skill2CooldownRemaining > 0f) {
            return false;
        }
        skill2ActiveRemaining = SKILL2_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill2Active() {
        return skill2ActiveRemaining > 0f;
    }

    public float getSkill2ActiveRemaining() { return skill2ActiveRemaining; }
    public float getSkill2CooldownRemaining() { return skill2CooldownRemaining; }

    public boolean tryActivateSkill3() {
        if (skill3ActiveRemaining > 0f || skill3CooldownRemaining > 0f) {
            return false;
        }
        skill3ActiveRemaining = SKILL3_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill3Active() { return skill3ActiveRemaining > 0f; }
    public float getSkill3ActiveRemaining() { return skill3ActiveRemaining; }
    public float getSkill3CooldownRemaining() { return skill3CooldownRemaining; }

    public boolean tryActivateSkill4() {
        if (skill4ActiveRemaining > 0f || skill4CooldownRemaining > 0f) {
            return false;
        }
        skill4ActiveRemaining = SKILL4_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill4Active() { return skill4ActiveRemaining > 0f; }
    public float getSkill4ActiveRemaining() { return skill4ActiveRemaining; }
    public float getSkill4CooldownRemaining() { return skill4CooldownRemaining; }

    public boolean tryActivateSkill5() {
        if (skill5ActiveRemaining > 0f || skill5CooldownRemaining > 0f) {
            return false;
        }
        skill5ActiveRemaining = SKILL5_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill5Active() { return skill5ActiveRemaining > 0f; }
    public float getSkill5ActiveRemaining() { return skill5ActiveRemaining; }
    public float getSkill5CooldownRemaining() { return skill5CooldownRemaining; }

    public boolean tryActivateSkill6() {
        if (skill6ActiveRemaining > 0f || skill6CooldownRemaining > 0f) {
            return false;
        }
        skill6ActiveRemaining = SKILL6_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill6Active() { return skill6ActiveRemaining > 0f; }
    public float getSkill6ActiveRemaining() { return skill6ActiveRemaining; }
    public float getSkill6CooldownRemaining() { return skill6CooldownRemaining; }

    public boolean tryActivateSkill7() {
        if (skill7ActiveRemaining > 0f || skill7CooldownRemaining > 0f) {
            return false;
        }
        skill7ActiveRemaining = SKILL7_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill7Active() { return skill7ActiveRemaining > 0f; }
    public float getSkill7ActiveRemaining() { return skill7ActiveRemaining; }
    public float getSkill7CooldownRemaining() { return skill7CooldownRemaining; }

    public boolean tryActivateSkill8() {
        if (skill8ActiveRemaining > 0f || skill8CooldownRemaining > 0f) {
            return false;
        }
        skill8ActiveRemaining = SKILL8_ACTIVE_DURATION;
        return true;
    }

    public boolean isSkill8Active() { return skill8ActiveRemaining > 0f; }
    public float getSkill8ActiveRemaining() { return skill8ActiveRemaining; }
    public float getSkill8CooldownRemaining() { return skill8CooldownRemaining; }

    // Restore helpers (used by persistence)
    public void setSkill1State(float active, float cooldown) {
        this.skill1ActiveRemaining = Math.max(0f, active);
        this.skill1CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill2State(float active, float cooldown) {
        this.skill2ActiveRemaining = Math.max(0f, active);
        this.skill2CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill3State(float active, float cooldown) {
        this.skill3ActiveRemaining = Math.max(0f, active);
        this.skill3CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill4State(float active, float cooldown) {
        this.skill4ActiveRemaining = Math.max(0f, active);
        this.skill4CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill5State(float active, float cooldown) {
        this.skill5ActiveRemaining = Math.max(0f, active);
        this.skill5CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill6State(float active, float cooldown) {
        this.skill6ActiveRemaining = Math.max(0f, active);
        this.skill6CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill7State(float active, float cooldown) {
        this.skill7ActiveRemaining = Math.max(0f, active);
        this.skill7CooldownRemaining = Math.max(0f, cooldown);
    }
    public void setSkill8State(float active, float cooldown) {
        this.skill8ActiveRemaining = Math.max(0f, active);
        this.skill8CooldownRemaining = Math.max(0f, cooldown);
    }

    public void cancelSkill4Early() {
        if (skill4ActiveRemaining > 0f) {
            skill4ActiveRemaining = 0f;
            skill4CooldownRemaining = SKILL4_COOLDOWN_DURATION;
        }
    }

    public void cancelSkill7Early() {
        if (skill7ActiveRemaining > 0f) {
            skill7ActiveRemaining = 0f;
            skill7CooldownRemaining = SKILL7_COOLDOWN_DURATION;
        }
    }
}
