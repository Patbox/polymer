package eu.pb4.polymer.core.mixin.client.rendering;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(BlockModelShaper.class)
public class BlockModelsMixin {
    /*@Inject(method = "getModelId(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/util/ModelIdentifier;", at = @At("HEAD"), cancellable = true, require = 0)
    private static void polymer$skipModels(BlockState state, CallbackInfoReturnable<ModelIdentifier> cir) {
        if (PolymerKeepModel.useServerModel(state.getBlock())) {
            cir.setReturnValue(new ModelIdentifier(Identifier.of("minecraft", "air"), ""));
        }
    }*/

    @ModifyVariable(method = "getBlockModel", at = @At("HEAD"), require = 0, argsOnly = true)
    private BlockState polymer$replaceBlockState(BlockState state) {
        return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock block && !PolymerKeepModel.is(block) ? Blocks.AIR.defaultBlockState() : state;
    }
}
