package eu.pb4.polymer.core.mixin.other;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.RegistryEntryRegistry;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs", priority = 500)
public interface ByteBufCodecsEntriesMixin {
    @ModifyReturnValue(method = "idMapper(Lnet/minecraft/core/IdMap;)Lnet/minecraft/network/codec/StreamCodec;", at = @At("TAIL"))
    private static <T> StreamCodec<ByteBuf, T> polymer$changeData(StreamCodec<ByteBuf, T> original, @Local(argsOnly = true) IdMap<T> iterable) {
        if (iterable instanceof Registry<T> registry) {
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();

                var polymerSyncedObject = PolymerSyncedObject.getSyncedObject(registry, val);
                if (polymerSyncedObject != null) {
                    var obj = polymerSyncedObject.getPolymerReplacement(val, player);

                    if (obj != null) {
                        return obj;
                    } else {
                        return registry.byId(0);
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
                var polymerSyncedObject = PolymerSyncedObject.getSyncedObject(registry, ((Holder<Object>) val).value());

                if (polymerSyncedObject != null) {
                    //noinspection unchecked
                    var obj = polymerSyncedObject.getPolymerReplacement(((Holder<Object>) val).value(), player);

                    if (obj != null) {
                        //noinspection unchecked
                        return (T) registry.wrapAsHolder(obj);
                    } else {
                        //noinspection unchecked
                        return (T) registry.get(0);
                    }
                }
                return val;
            });
        } else if (iterable == Block.BLOCK_STATE_REGISTRY) {
            return TransformingPacketCodec.encodeOnly(original, (byteBuf, val) -> {
                var player = PacketContext.get();

                //noinspection unchecked
                return (T) PolymerBlockUtils.getPolymerBlockState((BlockState) val, player);
            });
        }

        return original;
    }
}