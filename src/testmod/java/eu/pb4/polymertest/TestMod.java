package eu.pb4.polymertest;

import eu.pb4.polymer.block.BasicVirtualBlock;
import eu.pb4.polymer.entity.EntityHelper;
import eu.pb4.polymer.item.BasicVirtualItem;
import eu.pb4.polymer.item.ItemHelper;
import eu.pb4.polymer.item.VirtualBlockItem;
import eu.pb4.polymer.item.VirtualHeadBlockItem;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.impl.registry.sync.FabricRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer {
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier("test", "general"),
            () -> new ItemStack(TestMod.ITEM));

    public static BasicVirtualItem ITEM = new TestItem(new FabricItemSettings().fireproof().maxCount(5).group(ITEM_GROUP), Items.IRON_HOE);
    public static BasicVirtualItem ITEM_2 = new BasicVirtualItem(new FabricItemSettings().fireproof().maxCount(99).group(ITEM_GROUP), Items.DIAMOND_BLOCK);
    public static Block BLOCK = new TestBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(2f));
    public static BlockItem BLOCK_ITEM = new VirtualBlockItem(BLOCK, new FabricItemSettings(), Items.STONE);
    public static Block BLOCK_2 = new BasicVirtualBlock(AbstractBlock.Settings.of(Material.STONE).strength(2f), Blocks.TNT);
    public static BlockItem BLOCK_ITEM_2 = new VirtualBlockItem(BLOCK_2, new FabricItemSettings().group(ITEM_GROUP), Items.TNT);
    public static TinyPotatoBlock TATER_BLOCK = new TinyPotatoBlock(AbstractBlock.Settings.of(Material.STONE).strength(10f));
    public static BlockItem TATER_BLOCK_ITEM = new VirtualHeadBlockItem(TATER_BLOCK, new FabricItemSettings().group(ITEM_GROUP));
    public static TestPickaxeItem PICKAXE = new TestPickaxeItem(ToolMaterials.NETHERITE, 10, -3.9f, new FabricItemSettings().group(ITEM_GROUP));
    public static TestHelmetItem HELMET = new TestHelmetItem(new FabricItemSettings().group(ITEM_GROUP));
    public static Block WRAPPED_BLOCK = new BasicVirtualBlock(AbstractBlock.Settings.copy(BLOCK), BLOCK);
    public static Block SELF_REFERENCE_BLOCK = new SelfReferenceBlock(AbstractBlock.Settings.copy(Blocks.STONE));
    public static Item WRAPPED_ITEM = new BasicVirtualItem(new FabricItemSettings().group(ITEM_GROUP), ITEM);

    public static Block WEAK_GLASS_BLOCK = new WeakGlassBlock(AbstractBlock.Settings.copy(Blocks.GLASS));
    public static Item WEAK_GLASS_BLOCK_ITEM = new VirtualBlockItem(WEAK_GLASS_BLOCK, new Item.Settings(), Items.GLASS);

    public static TestBowItem BOW_1 = new TestBowItem(new FabricItemSettings().group(ITEM_GROUP), "bow");
    public static TestBowItem BOW_2 = new TestBowItem(new FabricItemSettings().group(ITEM_GROUP), "bow2");

    public static Enchantment ENCHANTMENT;

    public static final EntityType<TestEntity> ENTITY = FabricEntityTypeBuilder.<TestEntity>create(SpawnGroup.CREATURE, TestEntity::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();
    public static final EntityType<TestEntity2> ENTITY_2 = FabricEntityTypeBuilder.<TestEntity2>create(SpawnGroup.CREATURE, TestEntity2::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();

    @Override
    public void onInitialize() {
        ResourcePackUtils.addModAsAssetsSource("polymertest");
        //ResourcePackUtils.markAsRequired();
        //ResourcePackUtils.addModAsAssetsSource("promenade");

        Registry.register(Registry.ITEM, new Identifier("test", "item"), ITEM);
        Registry.register(Registry.ITEM, new Identifier("test", "item2"), ITEM_2);
        Registry.register(Registry.BLOCK, new Identifier("test", "block"), BLOCK);
        Registry.register(Registry.ITEM, new Identifier("test", "block"), BLOCK_ITEM);
        Registry.register(Registry.BLOCK, new Identifier("test", "block2"), BLOCK_2);
        Registry.register(Registry.ITEM, new Identifier("test", "block2"), BLOCK_ITEM_2);
        Registry.register(Registry.BLOCK, new Identifier("test", "potato_block"), TATER_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("test", "potato_block"), TATER_BLOCK_ITEM);
        Registry.register(Registry.ITEM, new Identifier("test", "pickaxe"), PICKAXE);
        Registry.register(Registry.ITEM, new Identifier("test", "helmet"), HELMET);
        Registry.register(Registry.ITEM, new Identifier("test", "bow1"), BOW_1);
        Registry.register(Registry.ITEM, new Identifier("test", "bow2"), BOW_2);
        Registry.register(Registry.BLOCK, new Identifier("test", "wrapped_block"), WRAPPED_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier("test", "self_block"), SELF_REFERENCE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("test", "wrapped_item"), WRAPPED_ITEM);
        Registry.register(Registry.BLOCK, new Identifier("test", "weak_glass"), WEAK_GLASS_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("test", "weak_glass"), WEAK_GLASS_BLOCK_ITEM);
        ENCHANTMENT = Registry.register(Registry.ENCHANTMENT, new Identifier("test", "enchantment"), new TestEnchantment());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity"), ENTITY);
        FabricDefaultAttributeRegistry.register(ENTITY, TestEntity.createCreeperAttributes());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity2"), ENTITY_2);
        FabricDefaultAttributeRegistry.register(ENTITY_2, TestEntity2.createCreeperAttributes());

        EntityHelper.registerVirtualEntityType(ENTITY, ENTITY_2);

        ItemHelper.VIRTUAL_ITEM_CHECK.register((itemStack) -> itemStack.hasNbt() && itemStack.getNbt().contains("Test", NbtElement.STRING_TYPE));

        ItemHelper.VIRTUAL_ITEM_MODIFICATION_EVENT.register((original, virtual, player) -> {
            if (original.hasNbt() && original.getNbt().contains("Test", NbtElement.STRING_TYPE)) {
                ItemStack out = new ItemStack(Items.DIAMOND_SWORD, virtual.getCount());
                out.setNbt(virtual.getNbt());
                out.setCustomName(new LiteralText("TEST VALUE: " + original.getNbt().getString("Test")).formatted(Formatting.WHITE));
                return out;
            }
            return virtual;
        });

        CommandRegistrationCallback.EVENT.register((d, b) -> d.register(literal("test").executes((ctx) -> {
            try {
                ctx.getSource().sendFeedback(new LiteralText("" + ResourcePackUtils.hasPack(ctx.getSource().getPlayer())), false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        })));
    }
}
