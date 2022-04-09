package eu.pb4.polymer.impl;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import net.minecraft.util.Identifier;

public class InternalServerRegistry {
    public static final Identifier POLYMER_ITEM_GROUP = PolymerImplUtils.id("items");

    public static final ImplPolymerRegistry<PolymerItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("server_item_group", "IG");
}
