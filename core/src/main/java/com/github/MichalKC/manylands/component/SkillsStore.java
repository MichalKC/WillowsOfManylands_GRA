package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class SkillsStore implements Component {
    public static final ComponentMapper<SkillsStore> MAPPER = ComponentMapper.getFor(SkillsStore.class);
}
