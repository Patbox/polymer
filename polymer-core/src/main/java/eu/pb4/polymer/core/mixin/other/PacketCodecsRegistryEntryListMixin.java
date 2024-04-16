package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$18", priority = 500)
public abstract class PacketCodecsRegistryEntryListMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntryList;)V", at = @At("HEAD"), argsOnly = true)
    private RegistryEntryList polymer$changeData(RegistryEntryList registryEntryList, RegistryByteBuf registryByteBuf) {
        if (registryEntryList.getTagKey().isEmpty()) {
            var player = PolymerUtils.getPlayerContext();

            if (player != null) {
                var arr = new ArrayList<RegistryEntry>();
                for (var i = 0; i < registryEntryList.size(); i++) {
                    var val = registryEntryList.get(i);
                    if (val.value() instanceof PolymerSoundEvent syncedObject) {
                        var replacement = syncedObject.getPolymerReplacement(PolymerUtils.getPlayerContext());

                        if (replacement instanceof PolymerSoundEvent) {
                            arr.add(RegistryEntry.of(replacement));
                        }

                        arr.add(Registries.SOUND_EVENT.getEntry(replacement));
                    } else if ((val.value() instanceof PolymerSyncedObject<?> s && s.canSyncRawToClient(player)) || !(val.value() instanceof PolymerObject)) {
                        arr.add(val);
                    }
                }
            }
        }

        return registryEntryList;
    }

}