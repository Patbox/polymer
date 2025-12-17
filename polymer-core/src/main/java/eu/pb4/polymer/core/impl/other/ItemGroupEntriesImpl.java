package eu.pb4.polymer.core.impl.other;

import java.util.Collection;
import java.util.Set;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

public class ItemGroupEntriesImpl implements CreativeModeTab.Output {
        public final Collection<ItemStack> parentTabStacks = ItemStackLinkedSet.createTypeAndComponentsSet();
        public final Set<ItemStack> searchTabStacks = ItemStackLinkedSet.createTypeAndComponentsSet();
        private final CreativeModeTab group;
        private final FeatureFlagSet enabledFeatures;

        public ItemGroupEntriesImpl(CreativeModeTab group, FeatureFlagSet enabledFeatures) {
            this.group = group;
            this.enabledFeatures = enabledFeatures;
        }

        public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
            if (stack.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            } else {
                boolean bl = this.parentTabStacks.contains(stack) && visibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
                if (bl) {
                    String var10002 = stack.getDisplayName().getString();
                    throw new IllegalStateException("Accidentally adding the same item stack twice " + var10002 + " to a Creative Mode Tab: " + this.group.getDisplayName().getString());
                } else {
                    if (stack.getItem().isEnabled(this.enabledFeatures)) {
                        switch (visibility.ordinal()) {
                            case 0:
                                this.parentTabStacks.add(stack);
                                this.searchTabStacks.add(stack);
                                break;
                            case 1:
                                this.parentTabStacks.add(stack);
                                break;
                            case 2:
                                this.searchTabStacks.add(stack);
                        }
                    }

                }
            }
        }
    }