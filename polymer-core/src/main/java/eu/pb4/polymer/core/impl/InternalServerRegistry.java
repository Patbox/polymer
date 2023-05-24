package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Util;

import java.util.Set;

public class InternalServerRegistry {
    public static final ImplPolymerRegistry<ItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("item_group", "ig");
}
