package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.impl.other.ActionSource;
import net.minecraft.util.ActionResult;

public interface LastActionResultStorer {
    void polymer$setLastActionResult(ActionResult result);
    void polymer$setLastActionSource(ActionSource source);
}
