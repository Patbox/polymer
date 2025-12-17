package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.impl.other.ActionSource;
import net.minecraft.world.InteractionResult;

public interface LastActionResultStorer {
    void polymer$setLastActionResult(InteractionResult result);
    void polymer$setLastActionSource(ActionSource source);
}
