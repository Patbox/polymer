package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$20", priority = 500)
public abstract class PacketCodecsRegistryEntryListMixin {
    @Shadow @Final private RegistryKey field_54511;

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntryList;)V", at = @At("HEAD"), argsOnly = true)
    private RegistryEntryList polymer$changeData(RegistryEntryList registryEntryList, RegistryByteBuf registryByteBuf) {
        if (registryEntryList.getTagKey().isEmpty()) {
            var player = PacketContext.get();

            var arr = new ArrayList<RegistryEntry>();
            var reg = registryByteBuf.getRegistryManager().getOrThrow(this.field_54511);
            for (var i = 0; i < registryEntryList.size(); i++) {
                var val = registryEntryList.get(i);

                var obj = PolymerSyncedObject.getSyncedObjectDefinition(reg, val);
                if (obj instanceof PolymerSoundEvent syncedObject) {
                    var replacement = syncedObject.getPolymerReplacement(player);
                    if (replacement != null) {
                        arr.add(reg.getEntry(replacement));
                    }
                } else if (val.value() instanceof SoundEvent soundEvent && RegistrySyncUtils.isServerEntry(Registries.SOUND_EVENT, soundEvent)) {
                    arr.add(RegistryEntry.of(val.value()));
                } else if ((obj != null && obj.canSyncRawToClient(player)) || !(val.value() instanceof PolymerObject)) {
                    arr.add(val);
                }
            }

            return RegistryEntryList.of((List) arr);
        }

        return registryEntryList;
    }

}