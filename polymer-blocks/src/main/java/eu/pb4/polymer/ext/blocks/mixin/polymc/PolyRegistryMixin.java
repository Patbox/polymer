package eu.pb4.polymer.ext.blocks.mixin.polymc;

import eu.pb4.polymer.ext.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.mixin.compat.polymc_BlockStateManagerAccessor;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PolyRegistry.class)
public abstract class PolyRegistryMixin {
    @Shadow public abstract BlockStateManager getBlockStateManager();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_block_catchRegistry(CallbackInfo ci) {
        for (var takenState : BlockExtBlockMapper.INSTANCE.stateMap.keySet()) {
            try {
                ((polymc_BlockStateManagerAccessor) this.getBlockStateManager()).callRequestBlockState(takenState.getBlock(), state -> state == takenState, (x, y) -> { });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
