package eu.pb4.polymer.common.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Holder.Reference.class)
public interface ReferenceAccessor {
    @Invoker
    void callBindKey(ResourceKey<?> registryKey);
}
