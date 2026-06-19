package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.maps.MapObject;
import com.github.MichalKC.manylands.component.Gated;
import com.github.MichalKC.manylands.component.Interactable;
import com.github.MichalKC.manylands.component.Tiled;

/**
 * Universal evaluator for {@link Gated}: returns {@code true} only when every
 * required Tiled object name resolves to an entity carrying an
 * {@link Interactable} that is currently activated.
 */
public final class GateEvaluator {

    private static final Family GATED_LOOKUP_FAMILY = Family.all(Tiled.class, Interactable.class).get();

    private GateEvaluator() {
    }

    public static boolean isOpen(Engine engine, Gated gated) {
        if (gated == null || gated.isEmpty()) {
            return true;
        }
        ImmutableArray<Entity> entities = engine.getEntitiesFor(GATED_LOOKUP_FAMILY);
        for (String requiredName : gated.getRequiredActivatedObjectNames()) {
            if (requiredName == null || requiredName.isBlank()) continue;
            if (!findActivated(entities, requiredName.trim())) {
                return false;
            }
        }
        return true;
    }

    private static boolean findActivated(ImmutableArray<Entity> entities, String name) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            MapObject ref = tiled.getMapObjectRef();
            if (ref == null) continue;
            if (!name.equals(ref.getName())) continue;
            Interactable interactable = Interactable.MAPPER.get(entity);
            if (interactable != null && interactable.isActivated()) {
                return true;
            }
        }
        return false;
    }
}
