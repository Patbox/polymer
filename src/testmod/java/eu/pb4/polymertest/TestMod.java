package eu.pb4.polymertest;

import eu.pb4.polymer.item.BasicVirtualItem;
import eu.pb4.polymer.item.VirtualBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestMod implements ModInitializer {
    public static BasicVirtualItem item = new TestItem(new FabricItemSettings().fireproof().maxCount(5), Items.IRON_HOE);
    public static Block block = new TestBlock(AbstractBlock.Settings.of(Material.STONE).breakInstantly());
    public static BlockItem blockItem = new VirtualBlockItem(block, new FabricItemSettings(), Items.STONE);
    public static Block blockTater = new TinyPotatoBlock(AbstractBlock.Settings.of(Material.STONE));
    public static BlockItem blockItemTater = new VirtualBlockItem(blockTater, new FabricItemSettings(), Items.STONE);
    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier("test", "item"), item);
        Registry.register(Registry.BLOCK, new Identifier("test", "block"), block);
        Registry.register(Registry.ITEM, new Identifier("test", "block_item"), blockItem);
        Registry.register(Registry.BLOCK, new Identifier("test", "potato_block"), blockTater);
        Registry.register(Registry.ITEM, new Identifier("test", "potato_block_item"), blockItemTater);
    }
}
