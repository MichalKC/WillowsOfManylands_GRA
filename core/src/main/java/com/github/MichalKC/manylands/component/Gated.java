package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import java.util.Collections;
import java.util.List;

/**
 * Optional gating for any trigger-driven action. The action only fires when ALL listed
 * Tiled object names are currently {@link Interactable#isActivated() activated}.
 * <p>
 * Declared on a {@code trigger} (or any {@link Trigger}-bearing object) via the
 * {@code requiresActivated} string property: comma- or semicolon-separated list of
 * Tiled object names from the same map (e.g. {@code "front_door"} or {@code "lever_a, lever_b"}).
 */
public class Gated implements Component {
    public static final ComponentMapper<Gated> MAPPER = ComponentMapper.getFor(Gated.class);

    private final List<String> requiredActivatedObjectNames;

    public Gated(List<String> requiredActivatedObjectNames) {
        this.requiredActivatedObjectNames = requiredActivatedObjectNames == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(requiredActivatedObjectNames);
    }

    public List<String> getRequiredActivatedObjectNames() {
        return requiredActivatedObjectNames;
    }

    public boolean isEmpty() {
        return requiredActivatedObjectNames.isEmpty();
    }
}
