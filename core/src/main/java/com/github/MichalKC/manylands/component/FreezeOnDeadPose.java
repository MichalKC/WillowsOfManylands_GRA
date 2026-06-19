package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class FreezeOnDeadPose implements Component {
    public static final ComponentMapper<FreezeOnDeadPose> MAPPER = ComponentMapper.getFor(FreezeOnDeadPose.class);
}
