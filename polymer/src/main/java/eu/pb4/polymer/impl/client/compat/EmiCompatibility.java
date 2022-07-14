package eu.pb4.polymer.impl.client.compat;

import dev.emi.emi.EmiReloadManager;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.function.Predicate;

public class EmiCompatibility implements EmiPlugin {
    private static final Predicate<EmiStack> SHOULD_REMOVE = (stack) -> (PolymerItemUtils.isPolymerServerItem(stack.getItemStack()) || PolymerItemUtils.getServerIdentifier(stack.getItemStack()) != null || PolymerUtils.isServerOnly(stack.getEntry().getValue()));

    static {
        if (PolymerImpl.IS_CLIENT) {
            PolymerClientUtils.ON_CLEAR.register(() -> EmiReloadManager.reload());
            PolymerClientUtils.ON_SEARCH_REBUILD.register(() -> EmiReloadManager.reload());
        }
    }

    @Override
    public void register(EmiRegistry registry) {
        if (PolymerImpl.IS_CLIENT) {
            update(registry);
        }
    }

    private static void update(EmiRegistry registry) {
        if (registry == null) {
            return;
        }
        synchronized (registry) {
            try {
                EmiStackList.stacks.removeIf(SHOULD_REMOVE);

                for (var group : ItemGroup.GROUPS) {
                    if (group == ItemGroup.SEARCH) {
                        continue;
                    }

                    Collection<ItemStack> stacks;

                    if (group instanceof InternalClientItemGroup clientItemGroup) {
                        stacks = clientItemGroup.getStacks();
                    } else {
                        stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
                    }

                    if (stacks != null) {
                        for (var stack : stacks) {
                            registry.addEmiStack(new PolymerItemEmiStack(stack));
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class PolymerItemEmiStack extends ItemEmiStack {
        public PolymerItemEmiStack(ItemStack stack) {
            super(stack);
            this.setAmount(stack.getCount());
        }

        @Override
        public EmiStack copy() {
            return new PolymerItemEmiStack(this.getItemStack());
        }

        @Override
        public Identifier getId() {
            var id = PolymerItemUtils.getPolymerIdentifier(this.getItemStack());
            return  id != null ? id : Registry.ITEM.getId(this.getItemStack().getItem());
        }

        @Override
        public boolean isEqual(EmiStack stack, Comparison comparison) {
            if (this.getId().equals(PolymerItemUtils.getPolymerIdentifier(stack.getItemStack()))) {
                return super.isEqual(stack, comparison);
            }

            return false;
        }

        @Override
        public boolean isEqual(EmiStack stack) {
            if (this.getId() != null && this.getId().equals(PolymerItemUtils.getPolymerIdentifier(stack.getItemStack()))) {
                return super.isEqual(stack);
            }

            return false;
        }
    }
}
