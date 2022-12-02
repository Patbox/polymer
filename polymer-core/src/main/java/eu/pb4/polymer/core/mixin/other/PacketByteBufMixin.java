package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = PacketByteBuf.class, priority = 500)
public abstract class PacketByteBufMixin {
    @ModifyVariable(method = "writeRegistryValue", at = @At("HEAD"), argsOnly = true)
    private Object polymer$changeData(Object val, IndexedIterable<?> registry) {
        var player = PolymerUtils.getPlayerContext();
        
        if (player != null) {
            if (val instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                var obj = polymerSyncedObject.getPolymerReplacement(player);

                if (obj != null) {
                    return obj;
                }
            }

            if (registry == Block.STATE_IDS) {
                return PolymerBlockUtils.getPolymerBlockState((BlockState) val, player);
            }
        }

        return val;
    }

    @ModifyVariable(method = "writeRegistryEntry", at = @At("HEAD"))
    private RegistryEntry polymer$writeOptional(RegistryEntry registryEntry, IndexedIterable<RegistryEntry> indexedIterable) {
        if (registryEntry.value() instanceof PolymerSoundEvent syncedObject) {
            var replacement = syncedObject.getPolymerReplacement(PolymerUtils.getPlayerContext());

            if (replacement instanceof PolymerSoundEvent) {
                return RegistryEntry.of(replacement);
            }


            return Registries.SOUND_EVENT.getEntry(replacement);
        }

        return registryEntry;
    }
}