package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$18", priority = 500)
public abstract class PacketCodecsRegistryEntryMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntry;)V", at = @At("HEAD"), argsOnly = true)
    private RegistryEntry<?> polymer$changeData(RegistryEntry<?> val, RegistryByteBuf buf) {
        var player = PolymerUtils.getPlayerContext();
        
        if (player != null) {
            if (val.value() instanceof PolymerSoundEvent syncedObject) {
                var replacement = syncedObject.getPolymerReplacement(PolymerUtils.getPlayerContext());

                if (replacement instanceof PolymerSoundEvent) {
                    return RegistryEntry.of(replacement);
                }


                return Registries.SOUND_EVENT.getEntry(replacement);
            }
        }

        return val;
    }

}