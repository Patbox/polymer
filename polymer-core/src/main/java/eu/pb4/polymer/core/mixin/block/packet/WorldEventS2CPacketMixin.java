package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
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
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(WorldEventS2CPacket.class)
public class WorldEventS2CPacketMixin {
    @Shadow @Final private int eventId;
    @Shadow @Final private int data;
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lnet/minecraft/network/PacketByteBuf;", ordinal = 1))
    private int polymer$replaceValue(int data) {
        if (this.eventId == WorldEvents.BLOCK_BROKEN) {
            var state = Block.getStateFromRawId(data);
            var player = PacketContext.get();

            if (state.getBlock() instanceof PolymerBlock polymerBlock) {
                state =  PolymerBlockUtils.getBlockBreakBlockStateSafely(polymerBlock, state,
                        PolymerBlockUtils.NESTED_DEFAULT_DISTANCE, player);
            }

            return Block.getRawIdFromState(PolymerBlockUtils.getServerSideBlockState(state, player));
        }

        return data;
    }
}
