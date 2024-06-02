package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.component.ComponentType;

import java.util.Set;

public class PolymerComponentImpl {
    public static final Set<ComponentType<?>> UNSYNCED_COMPONENTS = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
}
