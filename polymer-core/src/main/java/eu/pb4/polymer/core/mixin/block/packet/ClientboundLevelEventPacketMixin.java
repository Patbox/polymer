package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(ClientboundLevelEventPacket.class)
public class ClientboundLevelEventPacketMixin {
    @Shadow @Final private int type;
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeInt(I)Lnet/minecraft/network/FriendlyByteBuf;", ordinal = 1))
    private int polymer$replaceValue(int data) {
        if (this.type == LevelEvent.PARTICLES_DESTROY_BLOCK) {
            var state = Block.stateById(data);
            var player = PacketContext.get();

            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock polymerBlock) {
                state =  PolymerBlockUtils.getBlockBreakBlockStateSafely(polymerBlock, state,
                        PolymerBlockUtils.NESTED_DEFAULT_DISTANCE, player);
            }

            return Block.getId(PolymerBlockUtils.getPolymerBlockState(state, player));
        }

        return data;
    }
}
