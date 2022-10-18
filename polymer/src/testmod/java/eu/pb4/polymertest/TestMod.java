package eu.pb4.polymertest;

import eu.pb4.polymer.api.block.SimplePolymerBlock;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.item.*;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.other.PolymerSoundEvent;
import eu.pb4.polymer.api.other.PolymerStat;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymertest.mixin.EntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer, ClientModInitializer {
    public static final PolymerItemGroup ITEM_GROUP = PolymerItemGroup.create(
            new Identifier("polymer", "test"),
            Text.translatable("testmod.itemgroup").formatted(Formatting.AQUA));

    public static final PolymerItemGroup ITEM_GROUP_2 = PolymerItemGroup.createPrivate(
            new Identifier("polymer", "test2"),
            Text.translatable("testmod.itemgroup2").formatted(Formatting.AQUA));
    public static Block FLUID_BLOCK;
    public static TestFluid.Flowing FLOWING_FLUID;
    public static TestFluid.Still STILL_FLUID;
    public static BucketItem FLUID_BUCKET;

    public static SimplePolymerItem ITEM = new TestItem(new FabricItemSettings().fireproof().maxCount(5).group(ITEM_GROUP), Items.IRON_HOE);
    public static SimplePolymerItem ITEM_2 = new SimplePolymerItem(new FabricItemSettings().fireproof().maxCount(99).group(ITEM_GROUP), Items.DIAMOND_BLOCK);
    public static SimplePolymerItem ITEM_3 = new SimplePolymerItem(new FabricItemSettings().fireproof().maxCount(99).group(ITEM_GROUP), Items.CHAINMAIL_CHESTPLATE);
    public static Block BLOCK = new TestBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(2f));
    public static BlockItem BLOCK_ITEM = new PolymerBlockItem(BLOCK, new FabricItemSettings().group(ITEM_GROUP), Items.STONE);
    public static Block BLOCK_PLAYER = new TestPerPlayerBlock(AbstractBlock.Settings.of(Material.STONE).strength(2f));
    public static BlockItem BLOCK_PLAYER_ITEM = new PolymerBlockItem(BLOCK_PLAYER, new FabricItemSettings().group(ITEM_GROUP), Items.WHITE_CARPET);
    public static Block BLOCK_CLIENT = new TestClientBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 3).strength(2f));
    public static BlockItem BLOCK_CLIENT_ITEM = new TestClientBlockItem(BLOCK_CLIENT, new FabricItemSettings().group(ITEM_GROUP));
    public static Block BLOCK_FENCE = new SimplePolymerBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(2f), Blocks.NETHER_BRICK_FENCE);
    public static BlockItem BLOCK_FENCE_ITEM = new PolymerBlockItem(BLOCK_FENCE, new FabricItemSettings().group(ITEM_GROUP), Items.NETHER_BRICK_FENCE);
    public static Block BLOCK_2 = new SimplePolymerBlock(AbstractBlock.Settings.of(Material.STONE).strength(2f), Blocks.TNT);
    public static Block BLOCK_3 = new Test3Block(AbstractBlock.Settings.of(Material.STONE).strength(2f));
    public static BlockItem BLOCK_ITEM_2 = new PolymerBlockItem(BLOCK_2, new FabricItemSettings().group(ITEM_GROUP), Items.TNT);
    public static BlockItem BLOCK_ITEM_3 = new PolymerBlockItem(BLOCK_3, new FabricItemSettings().group(ITEM_GROUP), Items.COBWEB);
    public static TinyPotatoBlock TATER_BLOCK = new TinyPotatoBlock(AbstractBlock.Settings.of(Material.STONE).strength(10f));
    public static BlockItem TATER_BLOCK_ITEM = new PolymerHeadBlockItem(TATER_BLOCK, new FabricItemSettings().group(ITEM_GROUP));
    public static BlockItem TATER_BLOCK_ITEM2 = new PolymerBlockItem(TATER_BLOCK, new FabricItemSettings().group(ITEM_GROUP), Items.RAW_IRON_BLOCK);
    public static TestPickaxeItem PICKAXE = new TestPickaxeItem(Items.WOODEN_PICKAXE, ToolMaterials.NETHERITE, 10, -3.9f, new FabricItemSettings().group(ITEM_GROUP));
    public static TestPickaxeItem PICKAXE2 = new TestPickaxeItem(Items.NETHERITE_PICKAXE, ToolMaterials.WOOD, 10, -3.9f, new FabricItemSettings().group(ITEM_GROUP));
    public static TestHelmetItem HELMET = new TestHelmetItem(new FabricItemSettings().group(ITEM_GROUP));
    public static Block WRAPPED_BLOCK = new SimplePolymerBlock(AbstractBlock.Settings.copy(BLOCK), BLOCK);
    public static Block SELF_REFERENCE_BLOCK = new SelfReferenceBlock(AbstractBlock.Settings.copy(Blocks.STONE));
    public static Item WRAPPED_ITEM = new SimplePolymerItem(new FabricItemSettings().group(ITEM_GROUP), ITEM);

    public static Block WEAK_GLASS_BLOCK = new WeakGlassBlock(AbstractBlock.Settings.copy(Blocks.GLASS));
    public static Item WEAK_GLASS_BLOCK_ITEM = new PolymerBlockItem(WEAK_GLASS_BLOCK, new Item.Settings(), Items.GLASS);

    public static TestBowItem BOW_1 = new TestBowItem(new FabricItemSettings().group(ITEM_GROUP), "bow");
    public static TestBowItem BOW_2 = new TestBowItem(new FabricItemSettings().group(ITEM_GROUP), "bow2");

    public static Item CAMERA_ITEM = new SimplePolymerItem(new FabricItemSettings().fireproof().maxCount(5).group(ITEM_GROUP), Items.IRON_DOOR) {
        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new SetCameraEntityS2CPacket(entity));
            }

            return super.useOnEntity(stack, user, entity, hand);
        }
    };

    public static Enchantment ENCHANTMENT;

    public static Identifier CUSTOM_STAT;

    public static final RecipeType<TestRecipe> TEST_RECIPE_TYPE = RecipeType.register("test:test");
    public static final RecipeSerializer<TestRecipe> TEST_RECIPE_SERIALIZER = new TestRecipe.Serializer();

    public static final StatusEffect STATUS_EFFECT = new TestStatusEffect();
    public static final StatusEffect STATUS_EFFECT_2 = new Test2StatusEffect();
    public static final Potion POTION = new Potion(new StatusEffectInstance(STATUS_EFFECT, 300));
    public static final Potion POTION_2 = new Potion(new StatusEffectInstance(STATUS_EFFECT_2, 300));
    public static final Potion LONG_POTION = new Potion("potion", new StatusEffectInstance(STATUS_EFFECT, 600));
    public static final Potion LONG_POTION_2 = new Potion("potion", new StatusEffectInstance(STATUS_EFFECT_2, 600));

    public static final EntityType<TestEntity> ENTITY = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TestEntity::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();
    public static final EntityType<TestEntity2> ENTITY_2 = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TestEntity2::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();
    public static final EntityType<TestEntity3> ENTITY_3 = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TestEntity3::new).dimensions(EntityDimensions.fixed(0.75f, 1.8f)).build();

    public static final Item TEST_ENTITY_EGG = new PolymerSpawnEggItem(ENTITY, Items.COW_SPAWN_EGG, new Item.Settings().group(ITEM_GROUP));
    public static final Item TEST_FOOD = new SimplePolymerItem(new Item.Settings().group(ITEM_GROUP).food(new FoodComponent.Builder().hunger(10).saturationModifier(20).build()), Items.POISONOUS_POTATO);
    public static final Item TEST_FOOD_2 = new SimplePolymerItem(new Item.Settings().group(ITEM_GROUP).food(new FoodComponent.Builder().hunger(1).saturationModifier(2).build()), Items.CAKE);

    public static final SoundEvent GHOST_HURT = new PolymerSoundEvent(new Identifier("polymertest", "ghosthurt"), SoundEvents.ENTITY_GHAST_HURT);
    
    private static final Map<Registry<?>, List<Pair<Identifier, ?>>> REG_CACHE = new HashMap<>();

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

    public static SimplePolymerItem MARKER_TEST = new ClickItem(new FabricItemSettings().group(ITEM_GROUP), Items.BLAZE_ROD, (player, hand) -> {
        if (hand == Hand.OFF_HAND) {
            DebugInfoSender.clearGameTestMarkers(player.getWorld());
        } else {
            // Red Blue Green Alpha
            // Blue Alpha Green Red

            DebugInfoSender.addGameTestMarker(player.getWorld(), player.getBlockPos(), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb(0xFF, 0, 0, 0),
                    Integer.MAX_VALUE);

            DebugInfoSender.addGameTestMarker(player.getWorld(), player.getBlockPos().up(), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb(0, 0x22, 0, 0xEE),
                    Integer.MAX_VALUE);

            DebugInfoSender.addGameTestMarker(player.getWorld(), player.getBlockPos().up(2), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb( 0xFF, 0xFF, 0xFF, 0x22),
                    Integer.MAX_VALUE);
        }
    });

    private void regArmor(EquipmentSlot slot, String main, String id) {
        register(Registry.ITEM, new Identifier("test", main + "_" + id), new TestArmor(slot, new Identifier("polymertest", "item/" + main + "_" + id), new Identifier("polymertest", main)));
    }

    @Override
    public void onInitialize() {
        ITEM_GROUP.setIcon(new ItemStack(TATER_BLOCK_ITEM));
        PolymerRPUtils.addAssetSource("apolymertest");
        PolymerRPUtils.requestArmor(new Identifier("polymertest", "shulker"));
        PolymerRPUtils.getInstance().setPackDescription(Text.literal("TEST REPLACED DESCRIPTION").formatted(Formatting.GREEN));
        //PolymerRPUtils.markAsRequired();
        //PolymerRPUtils.addModAsAssetsSource("promenade");

        register(Registry.ITEM, new Identifier("test", "item"), ITEM);
        register(Registry.ITEM, new Identifier("test", "item2"), ITEM_2);
        register(Registry.ITEM, new Identifier("test", "item3"), ITEM_3);
        register(Registry.BLOCK, new Identifier("test", "block"), BLOCK);
        register(Registry.ITEM, new Identifier("test", "block"), BLOCK_ITEM);
        register(Registry.BLOCK, new Identifier("test", "block_client"), BLOCK_CLIENT);
        register(Registry.ITEM, new Identifier("test", "block_client"), BLOCK_CLIENT_ITEM);
        register(Registry.BLOCK, new Identifier("test", "block_player"), BLOCK_PLAYER);
        register(Registry.ITEM, new Identifier("test", "block_player"), BLOCK_PLAYER_ITEM);
        register(Registry.BLOCK, new Identifier("test", "block_fence"), BLOCK_FENCE);
        register(Registry.ITEM, new Identifier("test", "block_fence"), BLOCK_FENCE_ITEM);
        register(Registry.BLOCK, new Identifier("test", "block2"), BLOCK_2);
        register(Registry.ITEM, new Identifier("test", "block2"), BLOCK_ITEM_2);
        register(Registry.BLOCK, new Identifier("test", "block3"), BLOCK_3);
        register(Registry.ITEM, new Identifier("test", "block3"), BLOCK_ITEM_3);
        register(Registry.BLOCK, new Identifier("test", "potato_block"), TATER_BLOCK);
        register(Registry.ITEM, new Identifier("test", "potato_block"), TATER_BLOCK_ITEM);
        register(Registry.ITEM, new Identifier("test", "potato_block2"), TATER_BLOCK_ITEM2);
        register(Registry.ITEM, new Identifier("test", "pickaxe"), PICKAXE);
        register(Registry.ITEM, new Identifier("test", "pickaxe2"), PICKAXE2);
        register(Registry.ITEM, new Identifier("test", "helmet"), HELMET);
        register(Registry.ITEM, new Identifier("test", "bow1"), BOW_1);
        register(Registry.ITEM, new Identifier("test", "bow2"), BOW_2);
        register(Registry.BLOCK, new Identifier("test", "wrapped_block"), WRAPPED_BLOCK);
        register(Registry.BLOCK, new Identifier("test", "self_block"), SELF_REFERENCE_BLOCK);
        register(Registry.ITEM, new Identifier("test", "wrapped_item"), WRAPPED_ITEM);
        register(Registry.BLOCK, new Identifier("test", "weak_glass"), WEAK_GLASS_BLOCK);
        register(Registry.ITEM, new Identifier("test", "weak_glass"), WEAK_GLASS_BLOCK_ITEM);
        register(Registry.ITEM, new Identifier("test", "ice_item"), ICE_ITEM);
        register(Registry.ITEM, new Identifier("test", "spawn_egg"), TEST_ENTITY_EGG);
        register(Registry.ITEM, new Identifier("test", "food"), TEST_FOOD);
        register(Registry.ITEM, new Identifier("test", "food2"), TEST_FOOD_2);
        register(Registry.ITEM, new Identifier("test", "camera"), CAMERA_ITEM);
        register(Registry.ITEM, new Identifier("test", "cmarker_test"), MARKER_TEST);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            var t = new SimplePolymerBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN), Blocks.TINTED_GLASS);
            register(Registry.BLOCK, new Identifier("test", "server_block"), t);
            register(Registry.ITEM, new Identifier("test", "server_block"), new PolymerBlockItem(t, new Item.Settings(), Items.TINTED_GLASS));
        }

        STILL_FLUID = register(Registry.FLUID, new Identifier("test", "fluid"), new TestFluid.Still());
        FLOWING_FLUID = register(Registry.FLUID, new Identifier("test", "flowing_fluid"), new TestFluid.Flowing());
        FLUID_BUCKET = register(Registry.ITEM, new Identifier("test", "fluid_bucket"),
                new TestBucketItem(STILL_FLUID, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1), Items.LAVA_BUCKET));
        FLUID_BLOCK = register(Registry.BLOCK, new Identifier("test", "fluid_block"), new TestFluidBlock(STILL_FLUID, FabricBlockSettings.copy(Blocks.WATER)));

        regArmor(EquipmentSlot.HEAD, "shulker", "helmet");
        regArmor(EquipmentSlot.CHEST, "shulker", "chestplate");
        regArmor(EquipmentSlot.LEGS, "shulker", "leggings");
        regArmor(EquipmentSlot.FEET, "shulker", "boots");

        regArmor(EquipmentSlot.CHEST, "tater", "chestplate");
        regArmor(EquipmentSlot.HEAD, "titan", "helmet");
        regArmor(EquipmentSlot.CHEST, "titan", "chestplate");
        regArmor(EquipmentSlot.LEGS, "titan", "leggings");
        regArmor(EquipmentSlot.FEET, "titan", "boots");
        regArmor(EquipmentSlot.HEAD, "titan2", "helmet");
        regArmor(EquipmentSlot.CHEST, "titan2", "chestplate");

        ENCHANTMENT = register(Registry.ENCHANTMENT, new Identifier("test", "enchantment"), new TestEnchantment());

        CUSTOM_STAT = PolymerStat.registerStat("test:custom_stat", StatFormatter.DEFAULT);

        register(Registry.RECIPE_SERIALIZER, new Identifier("test", "test"), TEST_RECIPE_SERIALIZER);

        register(Registry.STATUS_EFFECT, new Identifier("test", "effect"), STATUS_EFFECT);
        register(Registry.STATUS_EFFECT, new Identifier("test", "effect2"), STATUS_EFFECT_2);
        register(Registry.POTION, new Identifier("test", "potion"), POTION);
        register(Registry.POTION, new Identifier("test", "potion2"), POTION_2);
        register(Registry.POTION, new Identifier("test", "long_potion"), LONG_POTION);
        register(Registry.POTION, new Identifier("test", "long_potion_2"), LONG_POTION_2);

        register(Registry.ENTITY_TYPE, new Identifier("test", "entity"), ENTITY);
        FabricDefaultAttributeRegistry.register(ENTITY, TestEntity.createCreeperAttributes().add(EntityAttributes.GENERIC_LUCK));

        register(Registry.ENTITY_TYPE, new Identifier("test", "entity2"), ENTITY_2);
        FabricDefaultAttributeRegistry.register(ENTITY_2, TestEntity2.createCreeperAttributes());

        register(Registry.ENTITY_TYPE, new Identifier("test", "entity3"), ENTITY_3);
        FabricDefaultAttributeRegistry.register(ENTITY_3, TestEntity3.createMobAttributes().add(EntityAttributes.HORSE_JUMP_STRENGTH));

        PolymerEntityUtils.registerType(ENTITY, ENTITY_2, ENTITY_3);

        PolymerItemUtils.ITEM_CHECK.register((itemStack) -> itemStack.hasNbt() && itemStack.getNbt().contains("Test", NbtElement.STRING_TYPE));

        PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((original, virtual, player) -> {
            if (original.hasNbt() && original.getNbt().contains("Test", NbtElement.STRING_TYPE)) {
                ItemStack out = new ItemStack(Items.DIAMOND_SWORD, virtual.getCount());
                out.setNbt(virtual.getNbt());
                out.setCustomName(Text.literal("TEST VALUE: " + original.getNbt().getString("Test")).formatted(Formatting.WHITE));
                return out;
            }
            return virtual;
        });



        CommandRegistrationCallback.EVENT.register((d, b, c) -> {
            d.register(literal("test")
                    .executes((ctx) -> {
                        try {
                            ctx.getSource().sendFeedback(Text.literal("" + PolymerRPUtils.hasPack(ctx.getSource().getPlayer())), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return 0;
                    })
            );
            d.register(literal("incrementStat")
                    .executes((ctx) -> {
                        ctx.getSource().getPlayer().incrementStat(CUSTOM_STAT);
                        ctx.getSource().sendFeedback(Text.literal("Stat now: " + ctx.getSource().getPlayer().getStatHandler().getStat(Stats.CUSTOM, CUSTOM_STAT)), false);

                        return 1;
                    })
            );
        });


        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        CommandRegistrationCallback.EVENT.register((d, b, c) -> d.register(literal("test2").executes((ctx) -> {
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
        AtomicBoolean mapper = new AtomicBoolean(false);

        CommandRegistrationCallback.EVENT.register((d, b, c) -> d.register(literal("mapperswitch").executes((ctx) -> {
            try {
                var player = ctx.getSource().getPlayer();
                if (mapper.get()) {
                    BlockMapper.set(player.networkHandler, BlockMapper.createDefault());
                } else {
                    var list = new ArrayList<BlockState>();
                    Block.STATE_IDS.forEach(list::add);
                    var copy = new ArrayList<>(list);

                    var map = new IdentityHashMap<BlockState, BlockState>();
                    var random = new Random();
                    for (var entry : list) {
                        BlockState state;
                        if (entry.isAir()) {
                            state = entry;
                            copy.remove(entry);
                        } else {
                            state = copy.remove(random.nextInt(copy.size()));
                        }

                        map.put(entry, state);
                    }

                    BlockMapper.set(player.networkHandler, BlockMapper.createMap(map));
                }
                PolymerUtils.reloadWorld(player);
                mapper.set(!atomicBoolean.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        })));

        PolymerItemGroup.LIST_EVENT.register((p, s) -> {
            if (atomicBoolean.get()) {
                s.add(ITEM_GROUP_2);
            }
        });

        //var id = Block.STATE_IDS.getRawId(BLOCK.getDefaultState());
        //System.out.println(id);
        //System.out.println(Block.STATE_IDS.get(id));

        /*var iter = new AtomicInteger();
        ServerTickEvents.END_SERVER_TICK.register((s) -> {
            for (var player : s.getPlayerManager().getPlayerList()) {
                player.sendMessage(new LiteralText(iter.toString()), true);
                for (int i = 0; i < 30; i++) {
                    PolymerSyncUtils.synchronizePolymerRegistries(player.networkHandler);
                }
                iter.incrementAndGet();
            }
        });*/


        for (var entry : REG_CACHE.entrySet()) {
            Collections.shuffle(entry.getValue());

            for (var e : entry.getValue()) {
                Registry.register((Registry<Object>) entry.getKey(), e.getLeft(), e.getRight());
            }
        }


        if (PolymerImpl.IS_CLIENT) {
            InternalClientRegistry.decodeState(-1);
            InternalClientRegistry.decodeStatusEffect(-1);
            InternalClientRegistry.decodeItem(-1);
            InternalClientRegistry.decodeVillagerProfession(-1);
            InternalClientRegistry.decodeEntity(-1);
            InternalClientRegistry.decodeBlockEntityType(-1);
        }
    }
    
    public static <B, T extends B> T register(Registry<B> registry, Identifier id, T obj) {
        REG_CACHE.computeIfAbsent(registry, (r) -> new ArrayList<>()).add(new Pair<>(id, obj));
        return obj;
    }

    @Override
    public void onInitializeClient() {

    }
}
