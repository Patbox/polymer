package eu.pb4.polymer.core.impl;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class ClientMetadataKeys {
    public static final Identifier MINECRAFT_PROTOCOL = ServerMetadataKeys.MINECRAFT_PROTOCOL;
    public static final Identifier BLOCKSTATE_BITS = id("core/blockstate_bits");
    public static final Identifier ADVANCED_TOOLTIP = id("core/advanced_tooltip");
}
