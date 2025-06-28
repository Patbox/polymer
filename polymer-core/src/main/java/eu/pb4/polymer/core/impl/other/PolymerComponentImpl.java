package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.component.ComponentType;
import net.minecraft.item.consume.ConsumeEffect;

import java.util.Set;

public class PolymerComponentImpl {
    public static final Set<ComponentType<?>> UNSYNCED_COMPONENTS = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
    public static final Set<ConsumeEffect.Type<?>> UNSYNCED_CONSUME_EFFECTS = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
}
