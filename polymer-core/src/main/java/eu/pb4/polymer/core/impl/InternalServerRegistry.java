package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Util;

import java.util.Set;

public class InternalServerRegistry {
    public static final Set<ItemGroup> ITEM_GROUPS = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());
}
