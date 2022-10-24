package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.client.PolymerKeepModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
        if (PolymerKeepModel.useServerModel(state.getBlock())) {
            cir.setReturnValue(new ModelIdentifier("minecraft", "air", ""));
        }
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), require = 0)
    private BlockState polymer_replaceBlockState(BlockState state) {
        return state.getBlock() instanceof PolymerBlock block && !PolymerKeepModel.is(block) ? Blocks.AIR.getDefaultState() : state;
    }
}
