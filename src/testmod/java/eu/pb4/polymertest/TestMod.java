package eu.pb4.polymertest;

import eu.pb4.polymer.block.BasicVirtualBlock;
import eu.pb4.polymer.item.BasicVirtualItem;
import eu.pb4.polymer.item.VirtualBlockItem;
import eu.pb4.polymer.item.VirtualHeadBlockItem;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestMod implements ModInitializer {
    public static BasicVirtualItem item = new TestItem(new FabricItemSettings().fireproof().maxCount(5), Items.IRON_HOE);
    public static BasicVirtualItem item2 = new BasicVirtualItem(new FabricItemSettings().fireproof().maxCount(99), Items.DIAMOND_BLOCK);

    public static Block block = new TestBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(2f));
    public static BlockItem blockItem = new VirtualBlockItem(block, new FabricItemSettings(), Items.STONE);
    public static Block block2 = new BasicVirtualBlock(AbstractBlock.Settings.of(Material.STONE).strength(2f), Blocks.TNT);
    public static BlockItem blockItem2 = new VirtualBlockItem(block2, new FabricItemSettings(), Items.TNT);
    public static TinyPotatoBlock blockTater = new TinyPotatoBlock(AbstractBlock.Settings.of(Material.STONE).strength(10f));
    public static BlockItem blockItemTater = new VirtualHeadBlockItem(blockTater, new FabricItemSettings());
    public static TestPickaxeItem pickaxe = new TestPickaxeItem(ToolMaterials.NETHERITE, 10, -3.9f, new FabricItemSettings());
    public static TestHelmetItem helmet = new TestHelmetItem(new FabricItemSettings());

    public static TestBowItem bow1 = new TestBowItem(new FabricItemSettings(), "bow");
    public static TestBowItem bow2 = new TestBowItem(new FabricItemSettings(), "bow2");

    public static Enchantment enchantment;

    public static final EntityType<TestEntity> entity = FabricEntityTypeBuilder.<TestEntity>create(SpawnGroup.CREATURE, TestEntity::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();
    public static final EntityType<TestEntity2> entity2 = FabricEntityTypeBuilder.<TestEntity2>create(SpawnGroup.CREATURE, TestEntity2::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();

    @Override
    public void onInitialize() {
        ResourcePackUtils.addModAsAssetsSource("polymertest");
        Registry.register(Registry.ITEM, new Identifier("test", "item"), item);
        Registry.register(Registry.ITEM, new Identifier("test", "item2"), item2);
        Registry.register(Registry.BLOCK, new Identifier("test", "block"), block);
        Registry.register(Registry.ITEM, new Identifier("test", "block"), blockItem);
        Registry.register(Registry.BLOCK, new Identifier("test", "block2"), block2);
        Registry.register(Registry.ITEM, new Identifier("test", "block2"), blockItem2);
        Registry.register(Registry.BLOCK, new Identifier("test", "potato_block"), blockTater);
        Registry.register(Registry.ITEM, new Identifier("test", "potato_block"), blockItemTater);
        Registry.register(Registry.ITEM, new Identifier("test", "pickaxe"), pickaxe);
        Registry.register(Registry.ITEM, new Identifier("test", "helmet"), helmet);
        Registry.register(Registry.ITEM, new Identifier("test", "bow1"), bow1);
        Registry.register(Registry.ITEM, new Identifier("test", "bow2"), bow2);

        enchantment = Registry.register(Registry.ENCHANTMENT, new Identifier("test", "enchantment"), new TestEnchantment());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity"), entity);
        FabricDefaultAttributeRegistry.register(entity, TestEntity.createCreeperAttributes());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity2"), entity2);
        FabricDefaultAttributeRegistry.register(entity2, TestEntity2.createCreeperAttributes());
    }
}
