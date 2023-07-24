package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.impl.other.world.PlayerEyedWorld;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerExtension {
    PlayerEyedWorld polymerCore$getOrCreatePlayerEyedWorld();

    @Nullable
    PlayerEyedWorld polymerCore$getPlayerEyedWorld();
}
