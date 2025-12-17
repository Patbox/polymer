package eu.pb4.polymer.core.mixin.other;


import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$28", priority = 500)
public abstract class ByteBufCodecsHolderSetMixin {
    /*@ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntryList;)V", at = @At("HEAD"), argsOnly = true)
    private RegistryEntryList polymer$changeData(RegistryEntryList registryEntryList, RegistryByteBuf registryByteBuf) {
        if (registryEntryList.getTagKey().isEmpty()) {
            var player = PacketContext.get();

            var arr = new ArrayList<RegistryEntry>();
            for (var i = 0; i < registryEntryList.size(); i++) {
                var val = registryEntryList.get(i);
                /*if (val.value() instanceof PolymerSoundEvent syncedObject) {
                    var replacement = syncedObject.getPolymerReplacement(player);

                    if (replacement instanceof PolymerSoundEvent) {
                        arr.add(RegistryEntry.of(replacement));
                    }

                    arr.add(Registries.SOUND_EVENT.getEntry(replacement));
                } *//*

                if (val.value() instanceof SoundEvent soundEvent && RegistrySyncUtils.isServerEntry(Registries.SOUND_EVENT, soundEvent)) {
                    arr.add(RegistryEntry.of(val.value()));
                } else if ((val.value() instanceof PolymerSyncedObject<?> s && s.canSyncRawToClient(player)) || !(val.value() instanceof PolymerObject)) {
                    arr.add(val);
                }
            }
        }

        return registryEntryList;
    }*/

}