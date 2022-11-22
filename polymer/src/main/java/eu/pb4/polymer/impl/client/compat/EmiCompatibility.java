package eu.pb4.polymer.impl.client.compat;

import dev.emi.emi.EmiReloadManager;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.PolymerImpl;

import java.util.function.Predicate;

public class EmiCompatibility implements EmiPlugin {
    private static final Predicate<EmiStack> SHOULD_REMOVE = (stack) -> (PolymerItemUtils.isPolymerServerItem(stack.getItemStack()) || PolymerItemUtils.getServerIdentifier(stack.getItemStack()) != null || PolymerUtils.isServerOnly(stack.getEntry().getValue()));

    static {
        CompatUtils.registerReload(() -> EmiReloadManager.reload());
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
                CompatUtils.iterateItems(stack -> registry.addEmiStack(EmiStack.of(stack)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
