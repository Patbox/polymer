package eu.pb4.polymer.blocks.impl;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateVariant;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class PolyMcResourceEntrypoint implements PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        for (var block : Registries.BLOCK) {
            if (block instanceof PolymerTexturedBlock) {
                registry.registerBlockPoly(block, new PolymerTextureBlockPoly());
            }
        }
    }

    @Override
    public void registerModSpecificResources(ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        for (var entry : PolymerBlocksInternal.modelMap.entrySet()) {
            Identifier clientBlockId = Registries.BLOCK.getId(entry.getKey().getBlock());
            var clientBlockState = pack.getOrDefaultBlockState(clientBlockId.getNamespace(), clientBlockId.getPath());

            var stateName = PolymerBlocksInternal.generateStateName(entry.getKey());
            var array = PolymerBlocksInternal.createJsonElement(entry.getValue());

            clientBlockState.setVariant(stateName, pack.getGson().fromJson(array, JBlockStateVariant[].class));
        }
    }
}
