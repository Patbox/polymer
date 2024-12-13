package eu.pb4.polymer.core.mixin.item;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ComponentChanges.class)
public interface ComponentChangesAccessor {
    @Invoker("<init>")
    static ComponentChanges createComponentChanges(Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    Reference2ObjectMap<ComponentType<?>, Optional<?>> getChangedComponents();
}
