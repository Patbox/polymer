package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.util.Util;
import net.minecraft.world.item.CreativeModeTab;
import java.util.Set;

public class InternalServerRegistry {
    public static final ImplPolymerRegistry<CreativeModeTab> ITEM_GROUPS = new ImplPolymerRegistry<>("item_group", "ig");
}
