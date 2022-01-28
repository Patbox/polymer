package eu.pb4.blocktest;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockResourceUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        PolymerRPUtils.markAsRequired();
        PolymerRPUtils.addAssetSource("apolymertestblocks");

        Registry.register(Registry.BLOCK, new Identifier("blocktest", "noteblock"),
                new TestBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK), PolymerBlockResourceUtils.Type.FULL_BLOCK, "block/chlorophyte_block"));

        Registry.register(Registry.BLOCK, new Identifier("blocktest", "leaves"),
                new TestBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK), PolymerBlockResourceUtils.Type.TRANSPARENT_BLOCK, "block/chair"));
    }
}
