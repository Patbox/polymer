package eu.pb4.polymer.blocks.mixin.polymc;

import eu.pb4.polymer.blocks.impl.BlockExtBlockMapper;
import io.github.theepicblock.polymc.api.PolyMap;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(PolyMap.class)
public interface PolyMapImplMixin {
    @Inject(method = "getClientState", at = @At("HEAD"), cancellable = true, require = 0)
    private void skipPolymerHandledBlocks(BlockState serverBlock, ServerPlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (BlockExtBlockMapper.INSTANCE.stateMap.containsKey(serverBlock)) {
            cir.setReturnValue(serverBlock);
        }
    }
}
