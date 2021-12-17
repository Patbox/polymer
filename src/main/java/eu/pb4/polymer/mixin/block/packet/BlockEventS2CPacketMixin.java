package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEventS2CPacket.class)
public abstract class BlockEventS2CPacketMixin {
    @Shadow
    @Mutable
    private Block block;

    @Shadow
    public abstract Block getBlock();

    @Environment(EnvType.CLIENT)
    @Inject(method = "getBlock", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceBlockClient(CallbackInfoReturnable<Block> cir) {
        if (ClientUtils.isSingleplayer() && this.block instanceof PolymerBlock virtualBlock && !(this.block instanceof PolymerClientDecoded)) {
            cir.setReturnValue(PolymerBlockUtils.getBlockSafely(virtualBlock, this.block.getDefaultState(), ClientUtils.getPlayer()));
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(DefaultedRegistry instance, int index) {
        var object = InternalClientRegistry.BLOCKS.get(index);

        if (object.realServerBlock() != null) {
            return object.realServerBlock();
        }

        return instance.get(index);
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private Object polymer_replaceBlockLocal(Object block) {
        if (block instanceof PolymerBlock virtualBlock) {
            return PolymerBlockUtils.getBlockSafely(virtualBlock, ((Block) block).getDefaultState(), PolymerUtils.getPlayer());
        }
        return block;
    }
}
