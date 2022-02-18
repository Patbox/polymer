package eu.pb4.polymer.ext.blocks.mixin.polymc;


import eu.pb4.polymer.ext.blocks.impl.PolymerBlocksInternal;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.resource.JsonBlockState;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;

@Mixin(ResourcePackGenerator.class)
public class ResourcePackGeneratorMixin {
    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lio/github/theepicblock/polymc/api/PolyMap;getBlockPolys()Lcom/google/common/collect/ImmutableMap;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private static void polymer_blocks_generateModels(PolyMap tempDir, String tempPath, SimpleLogger logger, CallbackInfo ci, Path gameDir, Path clientDir, ResourcePackMaker pack) {
        for (var entry : PolymerBlocksInternal.modelMap.entrySet()) {
            Identifier clientBlockId = Registry.BLOCK.getId(entry.getKey().getBlock());
            JsonBlockState clientBlockState = pack.getOrDefaultPendingBlockState(clientBlockId);

            var stateName = PolymerBlocksInternal.generateStateName(entry.getKey());
            var array = PolymerBlocksInternal.createJsonElement(entry.getValue());

            clientBlockState.variants.put(stateName, array);
        }
    }
}
