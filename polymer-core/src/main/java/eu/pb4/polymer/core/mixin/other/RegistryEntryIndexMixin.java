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
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Registry;

@Mixin(targets = "net/minecraft/core/Registry$1")
public class RegistryEntryIndexMixin<T> implements RegistryEntryRegistry<T> {

    @SuppressWarnings("rawtypes")
    @Shadow @Final private Registry field_40939;

    @Override
    public Registry<T> polymer$getRegistry() {
        //noinspection unchecked
        return this.field_40939;
    }
}
