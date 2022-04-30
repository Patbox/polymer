package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldEventS2CPacket.class)
public class WorldEventS2CPacketMixin {

    @Shadow @Final private int eventId;

    @Shadow @Final private int data;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lio/netty/buffer/ByteBuf;", ordinal = 1))
    private int polymer_replaceValue(int data) {
        if (this.eventId == WorldEvents.BLOCK_BROKEN) {
            return Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId(data), PolymerUtils.getPlayer()));
        }

        return data;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getData", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceClientData(CallbackInfoReturnable<Integer> cir) {
        if (this.eventId == WorldEvents.BLOCK_BROKEN) {
            var state = InternalClientRegistry.decodeState(this.data);
            cir.setReturnValue(Block.getRawIdFromState(state));
        }
    }
}
