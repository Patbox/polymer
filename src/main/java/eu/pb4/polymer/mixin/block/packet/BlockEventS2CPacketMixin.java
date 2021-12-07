package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEventS2CPacket.class)
public abstract class BlockEventS2CPacketMixin {
    @Shadow @Mutable
    private Block block;

    @Shadow public abstract Block getBlock();

    @Unique private Block polymer_oldBlock = null;

    @Environment(EnvType.CLIENT)
    @Inject(method = "getBlock", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceBlockClient(CallbackInfoReturnable<Block> cir) {
        if (ClientUtils.isSingleplayer() && this.polymer_oldBlock == null && this.block instanceof PolymerBlock virtualBlock) {
            this.polymer_oldBlock = this.block;
            this.block = PolymerBlockUtils.getBlockSafely(virtualBlock, this.polymer_oldBlock.getDefaultState(), PolymerUtils.getPlayer());
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceBlockNetworking(DefaultedRegistry instance, int index) {
        if (PolymerClientUtils.isEnabled() && this.block == Blocks.AIR && index != 0) {
            var block = ClientPolymerBlock.REGISTRY.get(index);

            if (block != ClientPolymerBlock.NONE && block != null && PolymerClientDecoded.checkDecode(block.realServerBlock())) {
                this.block = block.realServerBlock();
            }
        }
        return instance.get(index);
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void polymer_replaceBlockLocal(PacketByteBuf byteBuf, CallbackInfo ci) {
        if (polymer_oldBlock == null && this.block instanceof PolymerBlock virtualBlock && !PolymerClientDecoded.checkDecode(this.block)) {
            this.polymer_oldBlock = block;
            this.block = PolymerBlockUtils.getBlockSafely(virtualBlock, this.polymer_oldBlock.getDefaultState(), PolymerUtils.getPlayer());
        }
    }
}
