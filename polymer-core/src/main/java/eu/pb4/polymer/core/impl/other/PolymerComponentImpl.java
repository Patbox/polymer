package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Set;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

public class PolymerComponentImpl {
    public static final Set<DataComponentType<?>> UNSYNCED_COMPONENTS = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
    public static final Set<ConsumeEffect.Type<?>> UNSYNCED_CONSUME_EFFECTS = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
}
