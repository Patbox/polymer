package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$14", priority = 500)
public abstract class PacketCodecsEntriesMixin {

    @ModifyVariable(method = "encode(Lio/netty/buffer/ByteBuf;Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true)
    private Object polymer$changeData(Object val, ByteBuf buf) {
        var player = PolymerUtils.getPlayerContext();
        
        if (player != null) {
            if (val instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                var obj = polymerSyncedObject.getPolymerReplacement(player);

                if (obj != null) {
                    return obj;
                }
            }
            if (val instanceof BlockState state) {
                return PolymerBlockUtils.getPolymerBlockState(state, player);
            }
        }

        return val;
    }
}