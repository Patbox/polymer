package eu.pb4.polymertest;

import eu.pb4.polymer.api.block.SimplePolymerBlock;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.item.*;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymertest.mixin.EntityAccessor;
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
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer {
    public static final PolymerItemGroup ITEM_GROUP = PolymerItemGroup.create(
            new Identifier("polymer", "test"),
            new TranslatableText("testmod.itemgroup").formatted(Formatting.AQUA));

    public static final PolymerItemGroup ITEM_GROUP_2 = PolymerItemGroup.createPrivate(
            new Identifier("polymer", "test2"),
            new TranslatableText("testmod.itemgroup2").formatted(Formatting.AQUA));

    public static SimplePolymerItem ITEM = new TestItem(new FabricItemSettings().fireproof().maxCount(5).group(ITEM_GROUP), Items.IRON_HOE);
    public static SimplePolymerItem ITEM_2 = new SimplePolymerItem(new FabricItemSettings().fireproof().maxCount(99).group(ITEM_GROUP), Items.DIAMOND_BLOCK);
    public static Block BLOCK = new TestBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(2f));
    public static BlockItem BLOCK_ITEM = new PolymerBlockItem(BLOCK, new FabricItemSettings().group(ITEM_GROUP), Items.STONE);
    public static Block BLOCK_FENCE = new SimplePolymerBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(2f), Blocks.NETHER_BRICK_FENCE);
    public static BlockItem BLOCK_FENCE_ITEM = new PolymerBlockItem(BLOCK_FENCE, new FabricItemSettings().group(ITEM_GROUP), Items.NETHER_BRICK_FENCE);
    public static Block BLOCK_2 = new SimplePolymerBlock(AbstractBlock.Settings.of(Material.STONE).strength(2f), Blocks.TNT);
    public static BlockItem BLOCK_ITEM_2 = new PolymerBlockItem(BLOCK_2, new FabricItemSettings().group(ITEM_GROUP), Items.TNT);
    public static TinyPotatoBlock TATER_BLOCK = new TinyPotatoBlock(AbstractBlock.Settings.of(Material.STONE).strength(10f));
    public static BlockItem TATER_BLOCK_ITEM = new PolymerHeadBlockItem(TATER_BLOCK, new FabricItemSettings().group(ITEM_GROUP));
    public static TestPickaxeItem PICKAXE = new TestPickaxeItem(ToolMaterials.NETHERITE, 10, -3.9f, new FabricItemSettings().group(ITEM_GROUP));
    public static TestHelmetItem HELMET = new TestHelmetItem(new FabricItemSettings().group(ITEM_GROUP));
    public static Block WRAPPED_BLOCK = new SimplePolymerBlock(AbstractBlock.Settings.copy(BLOCK), BLOCK);
    public static Block SELF_REFERENCE_BLOCK = new SelfReferenceBlock(AbstractBlock.Settings.copy(Blocks.STONE));
    public static Item WRAPPED_ITEM = new SimplePolymerItem(new FabricItemSettings().group(ITEM_GROUP), ITEM);

    public static Block WEAK_GLASS_BLOCK = new WeakGlassBlock(AbstractBlock.Settings.copy(Blocks.GLASS));
    public static Item WEAK_GLASS_BLOCK_ITEM = new PolymerBlockItem(WEAK_GLASS_BLOCK, new Item.Settings(), Items.GLASS);

    public static TestBowItem BOW_1 = new TestBowItem(new FabricItemSettings().group(ITEM_GROUP), "bow");
    public static TestBowItem BOW_2 = new TestBowItem(new FabricItemSettings().group(ITEM_GROUP), "bow2");

    public static Enchantment ENCHANTMENT;

    public static final EntityType<TestEntity> ENTITY = FabricEntityTypeBuilder.<TestEntity>create(SpawnGroup.CREATURE, TestEntity::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();
    public static final EntityType<TestEntity2> ENTITY_2 = FabricEntityTypeBuilder.<TestEntity2>create(SpawnGroup.CREATURE, TestEntity2::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();

    public static final Item TEST_ENTITY_EGG = new PolymerSpawnEggItem(ENTITY, Items.COW_SPAWN_EGG, new Item.Settings().group(ITEM_GROUP));
    public static final Item TEST_FOOD = new SimplePolymerItem(new Item.Settings().group(ITEM_GROUP).food(new FoodComponent.Builder().hunger(10).saturationModifier(20).build()), Items.POISONOUS_POTATO);
    public static final Item TEST_FOOD_2 = new SimplePolymerItem(new Item.Settings().group(ITEM_GROUP).food(new FoodComponent.Builder().hunger(1).saturationModifier(2).build()), Items.CAKE);

    public static SimplePolymerItem ICE_ITEM = new ClickItem(new FabricItemSettings().group(ITEM_GROUP), Items.SNOWBALL, (player, hand) -> {
        var tracker = new DataTracker(null);
        tracker.startTracking(EntityAccessor.getFROZEN_TICKS(), Integer.MAX_VALUE);
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), tracker, true));

        var attributes = player.getAttributes().getAttributesToSend();
        var tmp = new EntityAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED, (x) -> {});
        tmp.setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        tmp.addPersistentModifier(new EntityAttributeModifier(UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4cf"), "Powder snow slow", 0.05d, EntityAttributeModifier.Operation.ADDITION));
        attributes.add(tmp);

        player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), attributes));
    });

    private void regArmor(EquipmentSlot slot, String main, String id) {
        Registry.register(Registry.ITEM, new Identifier("test", main + "_" + id), new TestArmor(slot, new Identifier("polymertest", "item/" + main + "_" + id), new Identifier("polymertest", main)));
    }

    @Override
    public void onInitialize() {
        ITEM_GROUP.setIcon(new ItemStack(TATER_BLOCK_ITEM));
        PolymerRPUtils.addAssetSource("apolymertest");
        PolymerRPUtils.requestArmor(new Identifier("polymertest", "shulker"));
        //PolymerRPUtils.markAsRequired();
        //PolymerRPUtils.addModAsAssetsSource("promenade");

        Registry.register(Registry.ITEM, new Identifier("test", "item"), ITEM);
        Registry.register(Registry.ITEM, new Identifier("test", "item2"), ITEM_2);
        Registry.register(Registry.BLOCK, new Identifier("test", "block"), BLOCK);
        Registry.register(Registry.ITEM, new Identifier("test", "block"), BLOCK_ITEM);
        Registry.register(Registry.BLOCK, new Identifier("test", "block_fence"), BLOCK_FENCE);
        Registry.register(Registry.ITEM, new Identifier("test", "block_fence"), BLOCK_FENCE_ITEM);
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
        Registry.register(Registry.ITEM, new Identifier("test", "ice_item"), ICE_ITEM);
        Registry.register(Registry.ITEM, new Identifier("test", "spawn_egg"), TEST_ENTITY_EGG);
        Registry.register(Registry.ITEM, new Identifier("test", "food"), TEST_FOOD);
        Registry.register(Registry.ITEM, new Identifier("test", "food2"), TEST_FOOD_2);

        regArmor(EquipmentSlot.HEAD, "shulker", "helmet");
        regArmor(EquipmentSlot.CHEST, "shulker", "chestplate");
        regArmor(EquipmentSlot.LEGS, "shulker", "leggings");
        regArmor(EquipmentSlot.FEET, "shulker", "boots");

        regArmor(EquipmentSlot.HEAD, "titan", "helmet");
        regArmor(EquipmentSlot.CHEST, "titan", "chestplate");
        regArmor(EquipmentSlot.LEGS, "titan", "leggings");
        regArmor(EquipmentSlot.FEET, "titan", "boots");

        ENCHANTMENT = Registry.register(Registry.ENCHANTMENT, new Identifier("test", "enchantment"), new TestEnchantment());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity"), ENTITY);
        FabricDefaultAttributeRegistry.register(ENTITY, TestEntity.createCreeperAttributes());

        Registry.register(Registry.ENTITY_TYPE, new Identifier("test", "entity2"), ENTITY_2);
        FabricDefaultAttributeRegistry.register(ENTITY_2, TestEntity2.createCreeperAttributes());

        PolymerEntityUtils.registerType(ENTITY, ENTITY_2);

        PolymerItemUtils.ITEM_CHECK.register((itemStack) -> itemStack.hasNbt() && itemStack.getNbt().contains("Test", NbtElement.STRING_TYPE));

        PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((original, virtual, player) -> {
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
                ctx.getSource().sendFeedback(new LiteralText("" + PolymerRPUtils.hasPack(ctx.getSource().getPlayer())), false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        })));


        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        CommandRegistrationCallback.EVENT.register((d, b) -> d.register(literal("test2").executes((ctx) -> {
            try {
                var player = ctx.getSource().getPlayer();
                if (atomicBoolean.get()) {
                    PolymerSyncUtils.sendCreativeTab(ITEM_GROUP_2, player.networkHandler);
                } else {
                    PolymerSyncUtils.removeCreativeTab(ITEM_GROUP_2, player.networkHandler);
                }
                PolymerSyncUtils.rebuildCreativeSearch(player.networkHandler);
                atomicBoolean.set(!atomicBoolean.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        })));

        PolymerItemGroup.SYNC_EVENT.register((p, s) -> {
            if (atomicBoolean.get()) {
                s.send(ITEM_GROUP_2);
            }
        });

        var id = Block.STATE_IDS.getRawId(BLOCK.getDefaultState());
        System.out.println(id);
        System.out.println(Block.STATE_IDS.get(id));
    }
}
