package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Dead implements Component {
    public static final ComponentMapper<Dead> MAPPER = ComponentMapper.getFor(Dead.class);
}
