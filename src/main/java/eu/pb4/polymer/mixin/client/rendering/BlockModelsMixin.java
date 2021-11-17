package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.util.ModelIdentifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(BlockModels.class)
public class BlockModelsMixin {
    @Inject(method = "getModelId(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/util/ModelIdentifier;", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polymer_skipVirtualModels(BlockState state, CallbackInfoReturnable<ModelIdentifier> cir) {
        if (state.getBlock() instanceof PolymerBlock) {
            cir.setReturnValue(new ModelIdentifier("minecraft:air"));
        }
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), require = 0)
    private BlockState polymer_replaceBlockState(BlockState state) {
        return state.getBlock() instanceof PolymerBlock block ? PolymerBlockUtils.getBlockStateSafely(block, state) : state;
    }
}
