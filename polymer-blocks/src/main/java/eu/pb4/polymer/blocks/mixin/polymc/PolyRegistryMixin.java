package eu.pb4.polymer.blocks.mixin.polymc;

import eu.pb4.polymer.blocks.impl.BlockExtBlockMapper;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PolyRegistry.class)
public abstract class PolyRegistryMixin {
    @Shadow public abstract <T> T getSharedValues(SharedValuesKey<T> key);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_block_catchRegistry(CallbackInfo ci) {
        var blockStateManager = this.getSharedValues(BlockStateManager.KEY);
        for (var takenState : BlockExtBlockMapper.INSTANCE.stateMap.keySet()) {
            try {
                // Request this specific state from PolyMc
                // This will cause PolyMc to mark it as used
                blockStateManager.requestBlockState(state -> state == takenState, new Block[]{takenState.getBlock()}, (block, registry) -> {

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
