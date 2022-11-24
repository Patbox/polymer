package eu.pb4.polymer.impl;

import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import net.minecraft.item.ItemGroup;

public class InternalServerRegistry {
    public static final ImplPolymerRegistry<ItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("server_item_group", "IG");
}
