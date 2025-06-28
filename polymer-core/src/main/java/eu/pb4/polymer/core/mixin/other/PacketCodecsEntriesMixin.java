package eu.pb4.polymer.core.mixin.other;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.RegistryEntryRegistry;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs", priority = 500)
public interface PacketCodecsEntriesMixin {
    @ModifyReturnValue(method = "entryOf", at = @At("TAIL"))
    private static <T> PacketCodec<ByteBuf, T> polymer$changeData(PacketCodec<ByteBuf, T> original, @Local(argsOnly = true) IndexedIterable<T> iterable) {
        if (iterable instanceof Registry<T> registry) {
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();

                var polymerSyncedObject = PolymerSyncedObject.getSyncedObject(registry, val);
                if (polymerSyncedObject != null) {
                    var obj = polymerSyncedObject.getPolymerReplacement(val, player);

                    if (obj != null) {
                        return obj;
                    } else {
                        return registry.get(0);
                    }
                }
                return val;
            });
        }
        if (iterable instanceof RegistryEntryRegistry<?> tmp) {
            //noinspection unchecked
            var registry = (Registry<Object>) tmp.polymer$getRegistry();
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();

                //noinspection unchecked
                var polymerSyncedObject = PolymerSyncedObject.getSyncedObject(registry, ((RegistryEntry<Object>) val).value());

                if (polymerSyncedObject != null) {
                    //noinspection unchecked
                    var obj = polymerSyncedObject.getPolymerReplacement(((RegistryEntry<Object>) val).value(), player);

                    if (obj != null) {
                        //noinspection unchecked
                        return (T) registry.getEntry(obj);
                    } else {
                        //noinspection unchecked
                        return (T) registry.getEntry(0);
                    }
                }
                return val;
            });
        } else if (iterable == Block.STATE_IDS) {
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();

                //noinspection unchecked
                return (T) PolymerBlockUtils.getPolymerBlockState((BlockState) val, player);
            });
        }

        return original;
    }
}