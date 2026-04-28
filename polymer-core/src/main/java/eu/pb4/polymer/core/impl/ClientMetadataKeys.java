package eu.pb4.polymer.core.impl;

import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

import net.minecraft.resources.Identifier;

@ApiStatus.Internal
public class ClientMetadataKeys {
    public static final Identifier MINECRAFT_PROTOCOL = ServerMetadataKeys.MINECRAFT_PROTOCOL;
    public static final Identifier BLOCKSTATE_BITS = id("core/blockstate_bits");
    public static final Identifier ADVANCED_TOOLTIP = id("core/advanced_tooltip");
    public static final Identifier EXTENDED_RECIPE_INGREDIENTS = id("core/extended_recipe_ingredients");
}
