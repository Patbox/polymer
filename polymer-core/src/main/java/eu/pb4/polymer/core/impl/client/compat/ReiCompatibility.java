package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class ReiCompatibility implements REIClientPlugin {
    private static final Predicate<? extends EntryStack<?>> SHOULD_REMOVE = (x) -> x.getValue() instanceof ItemStack stack && (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getServerIdentifier(stack) != null);

    private static final EntryComparator<ItemStack> ITEM_STACK_ENTRY_COMPARATOR = (c, i) -> {
        var polymerId = PolymerItemUtils.getServerIdentifier(i);

        if (polymerId != null) {
            return polymerId.hashCode();
        }

        return 0;
    };

    static {
        CompatUtils.registerReload(() -> update(EntryRegistry.getInstance()));
    }

    private static void update(EntryRegistry registry) {
        try {
            registry.removeEntryIf(SHOULD_REMOVE);
            CompatUtils.iterateItems(stack -> registry.addEntry(EntryStack.of(VanillaEntryTypes.ITEM, stack)));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void registerEvents() {}

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        try {
            registry.registerGlobal(ITEM_STACK_ENTRY_COMPARATOR);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        if (PolymerImpl.USE_FULL_REI_COMPAT_CLIENT) {
            update(registry);
        } else {
            try {
                registry.removeEntryIf(SHOULD_REMOVE);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
