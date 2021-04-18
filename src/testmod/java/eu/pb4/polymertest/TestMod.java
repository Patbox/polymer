package eu.pb4.polymertest;

import eu.pb4.polymer.item.BasicVirtualItem;
import eu.pb4.polymer.item.VirtualBlockItem;
import eu.pb4.polymer.item.VirtualHeadBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestMod implements ModInitializer {
    public static BasicVirtualItem item = new TestItem(new FabricItemSettings().fireproof().maxCount(5), Items.IRON_HOE);
    public static Block block = new TestBlock(AbstractBlock.Settings.of(Material.STONE).breakInstantly());
    public static BlockItem blockItem = new VirtualBlockItem(block, new FabricItemSettings(), Items.STONE);
    public static TinyPotatoBlock blockTater = new TinyPotatoBlock(AbstractBlock.Settings.of(Material.STONE).strength(10f));
    public static BlockItem blockItemTater = new VirtualHeadBlockItem(blockTater, new FabricItemSettings());
    public static TestPickaxeItem pickaxe = new TestPickaxeItem(ToolMaterials.NETHERITE, 10, -3.9f, new FabricItemSettings());

    public static Enchantment enchantment;

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier("test", "item"), item);
        Registry.register(Registry.BLOCK, new Identifier("test", "block"), block);
        Registry.register(Registry.ITEM, new Identifier("test", "block"), blockItem);
        Registry.register(Registry.BLOCK, new Identifier("test", "potato_block"), blockTater);
        Registry.register(Registry.ITEM, new Identifier("test", "potato_block"), blockItemTater);
        Registry.register(Registry.ITEM, new Identifier("test", "pickaxe"), pickaxe);
        enchantment = Registry.register(Registry.ENCHANTMENT, new Identifier("test", "enchantment"), new TestEnchantment());

    }
}
