package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$17", priority = 500)
public abstract class PacketCodecsEntriesMixin {

    @ModifyVariable(method = "encode(Lio/netty/buffer/ByteBuf;Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true)
    private Object polymer$changeData(Object val, ByteBuf buf) {
        var player = PacketContext.get();

        var polymerSyncedObject = PolymerSyncedObject.getSyncedObject(null, val);
        if (polymerSyncedObject != null) {
            var obj = polymerSyncedObject.getPolymerReplacement(player);

            if (obj != null) {
                return obj;
            }
        }
        if (val instanceof BlockState state) {
            return PolymerBlockUtils.getPolymerBlockState(state, player);
        }

        return val;
    }
}