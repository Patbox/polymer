package eu.pb4.polymer.core.impl.client.compat;

import dev.emi.emi.EmiReloadManager;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.Internal
public class EmiCompatibility implements EmiPlugin {
    private static final Predicate<EmiStack> SHOULD_REMOVE = (stack) -> PolymerImplUtils.isPolymerControlled(stack.getItemStack());

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
