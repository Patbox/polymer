package eu.pb4.polymertest;

import eu.pb4.polymer.block.BasicVirtualBlock;
import eu.pb4.polymer.item.BasicVirtualItem;
import eu.pb4.polymer.item.ItemHelper;
import eu.pb4.polymer.item.VirtualBlockItem;
import eu.pb4.polymer.item.VirtualHeadBlockItem;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
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
    public static Block block_wrapped = new BasicVirtualBlock(AbstractBlock.Settings.copy(block), block);
    public static Block self_block = new SelfReferenceBlock(AbstractBlock.Settings.copy(Blocks.STONE));
    public static Item item_wrapped = new BasicVirtualItem(new FabricItemSettings(), item);

    public static TestBowItem bow1 = new TestBowItem(new FabricItemSettings(), "bow");
    public static TestBowItem bow2 = new TestBowItem(new FabricItemSettings(), "bow2");

    public static Enchantment enchantment;

    public static final EntityType<TestEntity> entity = FabricEntityTypeBuilder.<TestEntity>create(SpawnGroup.CREATURE, TestEntity::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();
    public static final EntityType<TestEntity2> entity2 = FabricEntityTypeBuilder.<TestEntity2>create(SpawnGroup.CREATURE, TestEntity2::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();

    @Override
    public void onInitialize() {
        ResourcePackUtils.addModAsAssetsSource("polymertest");
        //ResourcePackUtils.markAsRequired();
        //ResourcePackUtils.addModAsAssetsSource("promenade");

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
        Registry.register(Registry.BLOCK, new Identifier("test", "wrapped_block"), block_wrapped);
        Registry.register(Registry.BLOCK, new Identifier("test", "self_block"), self_block);
        Registry.register(Registry.ITEM, new Identifier("test", "wrapped_item"), item_wrapped);

        enchantment = Registry.register(Registry.ENCHANTMENT, new Identifier("test", "enchantment"), new TestEnchantment());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity"), entity);
        FabricDefaultAttributeRegistry.register(entity, TestEntity.createCreeperAttributes());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity2"), entity2);
        FabricDefaultAttributeRegistry.register(entity2, TestEntity2.createCreeperAttributes());

        ItemHelper.VIRTUAL_ITEM_CHECK.register((itemStack) -> itemStack.hasTag() && itemStack.getTag().contains("Test", NbtElement.STRING_TYPE));

        ItemHelper.VIRTUAL_ITEM_MODIFICATION_EVENT.register((original, virtual, player) -> {
            if (original.hasTag() && original.getTag().contains("Test", NbtElement.STRING_TYPE)) {
                ItemStack out = new ItemStack(Items.DIAMOND_SWORD, virtual.getCount());
                out.setTag(virtual.getTag());
                out.setCustomName(new LiteralText("TEST VALUE: " + original.getTag().getString("Test")).formatted(Formatting.WHITE));
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
