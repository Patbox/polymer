package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.RegistryEntryRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Registry;

@Mixin(targets = "net/minecraft/core/Registry$1")
public class RegistryEntryIndexMixin<T> implements RegistryEntryRegistry<T> {

    @Shadow
    @Final
    private Registry this$0;

    @Override
    public Registry<T> polymer$getRegistry() {
        //noinspection unchecked
        return this.this$0;
    }
}
