package eu.pb4.polymer.core.mixin.item;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentPatch.class)
public interface DataComponentPatchAccessor {
    @Invoker("<init>")
    static DataComponentPatch createComponentChanges(Reference2ObjectMap<DataComponentType<?>, Optional<?>> changedComponents) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    Reference2ObjectMap<DataComponentType<?>, Optional<?>> getMap();
}
