package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.util.ModelIdentifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModels.class)
public class BlockModelsMixin {
    @Inject(method = "getModelId(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/util/ModelIdentifier;", at = @At("HEAD"), cancellable = true)
    private static void polymer_skipVirtualModels(BlockState state, CallbackInfoReturnable<ModelIdentifier> cir) {
        if (state.getBlock() instanceof VirtualBlock) {
            cir.setReturnValue(new ModelIdentifier("minecraft:air"));
        }
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"))
    private BlockState polymer_replaceBlockState(BlockState state) {
        return state.getBlock() instanceof VirtualBlock block ? BlockHelper.getBlockStateSafely(block, state) : state;
    }
}
