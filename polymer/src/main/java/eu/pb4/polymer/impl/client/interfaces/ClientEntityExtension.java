package eu.pb4.polymer.impl.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public interface ClientEntityExtension {
    void polymer$setId(Identifier id);
    Identifier polymer$getId();
}
