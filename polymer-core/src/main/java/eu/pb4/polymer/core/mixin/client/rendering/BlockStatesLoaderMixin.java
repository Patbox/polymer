package eu.pb4.polymer.core.mixin.client.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(BlockStatesLoader.class)
public class BlockStatesLoaderMixin {
    @Shadow @Final private BlockStatesLoader.BlockModel missingModel;


    // Todo fix this, needs relocation
    /*@Inject(method = "loadBlockStates", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/resource/ResourceFinder;toResourcePath(Lnet/minecraft/util/Identifier;)Lnet/minecraft/util/Identifier;", ordinal = 0))
    private void setDefaultModelsForPolymer(Identifier id, StateManager<Block, BlockState> stateManager, CallbackInfo ci,
                                            @Local(ordinal = 1) Map<BlockState, BlockStatesLoader.BlockModel> modelMap) {
        if (PolymerKeepModel.useServerModel(Registries.BLOCK.get(id))) {
            for (var state : stateManager.getStates()) {
                modelMap.put(state, this.missingModel);
            }
        }
    }
     */
}
