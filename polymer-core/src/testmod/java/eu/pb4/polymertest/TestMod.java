package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.*;
import eu.pb4.polymer.core.api.other.PolymerStat;
import eu.pb4.polymer.core.api.other.SimplePolymerPotion;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import xyz.nucleoid.server.translations.api.LocalizationTarget;
import xyz.nucleoid.server.translations.impl.ServerTranslations;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer {
    private static final Map<Registry<?>, List<Pair<Identifier, ?>>> REG_CACHE = new HashMap<>();

    public static final ItemGroup ITEM_GROUP = new ItemGroup.Builder(null, -1)
            .displayName(Text.translatable("testmod.itemgroup").formatted(Formatting.AQUA))
            .icon(()-> new ItemStack(TestMod.TATER_BLOCK_ITEM))
            .entries(new ItemGroup.EntryCollector() {
                @Override
                public void accept(ItemGroup.DisplayContext arg, ItemGroup.Entries entries) {
                    entries.add(Items.DAMAGED_ANVIL.getDefaultStack());
                    var items = REG_CACHE.get(Registries.ITEM);

                    for (var pair : items) {
                        entries.add((ItemConvertible) pair.getRight());
                    }
                }
            })
            .build();
    public static Block FLUID_BLOCK;
    public static TestFluid.Flowing FLOWING_FLUID;
    public static TestFluid.Still STILL_FLUID;
    public static BucketItem FLUID_BUCKET;

    public static RegistryEntry<EntityAttribute> ATTRIBUTE = Registry.registerReference(Registries.ATTRIBUTE, Identifier.of("test:attribute"),
            new ClampedEntityAttribute("test.attribute", 0, -5, 5)
                    .setCategory(EntityAttribute.Category.POSITIVE).setTracked(true));
    public static SimplePolymerItem ITEM = registerItem(Identifier.of("test", "item"), (s) -> new TestItem(s.fireproof().maxCount(5), Items.IRON_HOE));
    public static SimplePolymerItem ITEM_2 = registerItem(Identifier.of("test", "item_2"), (s) -> new SimplePolymerItem(s.fireproof().maxCount(99)
            .attributeModifiers(AttributeModifiersComponent.builder().add(ATTRIBUTE,
                    new EntityAttributeModifier(Identifier.of("test:aaa"), 5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build()), Items.DIAMOND_BLOCK));
    public static SimplePolymerItem ITEM_3 = registerItem(Identifier.of("test", "item_3"), (s) -> new SimplePolymerItem(s.fireproof().maxCount(99), Items.CHAINMAIL_CHESTPLATE));
    public static Block BLOCK = registerBlock(Identifier.of("test", "block"), (s) -> new TestBlock(s.luminance((state) -> 15).strength(2f)));
    public static Block BLOCK_USE = registerBlock(Identifier.of("test", "block_use"), (s) -> new TestUseBlock(s
            .luminance((state) -> state.get(TestUseBlock.LIT) ? 15 : 0).strength(2f)));
    public static BlockItem BLOCK_ITEM = registerItem(Identifier.of("test", "block"), (s) -> new PolymerBlockItem(BLOCK, s, Items.STONE));
    public static BlockItem BLOCK_USE_ITEM = registerItem(Identifier.of("test", "block_use"), (s) -> new PolymerBlockItem(BLOCK_USE, s, Items.REDSTONE_LAMP));
    public static Block BLOCK_PLAYER = registerBlock(Identifier.of("test", "block_player"), (s) -> new TestPerPlayerBlock(s.strength(2f)));
    public static BlockItem BLOCK_PLAYER_ITEM = registerItem(Identifier.of("test", "block_player"), (s) -> new PolymerBlockItem(BLOCK_PLAYER, s, Items.WHITE_CARPET));
    public static Block BLOCK_CLIENT = registerBlock(Identifier.of("test", "block_client"), (s) -> new TestClientBlock(s.luminance((state) -> 3).strength(2f)));
    public static BlockItem BLOCK_CLIENT_ITEM = registerItem(Identifier.of("test", "block_client"), (s) -> new TestClientBlockItem(BLOCK_CLIENT, s));
    public static Block BLOCK_FENCE = registerBlock(Identifier.of("test", "fence"), (s) -> new SimplePolymerBlock(s.luminance((state) -> 15).strength(2f), Blocks.NETHER_BRICK_FENCE));
    public static BlockItem BLOCK_FENCE_ITEM = registerItem(Identifier.of("test", "fence"), (s) ->  new PolymerBlockItem(BLOCK_FENCE, s, Items.NETHER_BRICK_FENCE));
    public static Block BLOCK_2 = registerBlock(Identifier.of("test", "block_2"), (s) -> new SimplePolymerBlock(s.strength(2f), Blocks.TNT));
    public static Block BLOCK_3 = registerBlock(Identifier.of("test", "block_3"), (s) -> new Test3Block(s.strength(2f)));
    public static BlockItem BLOCK_ITEM_2 = registerItem(Identifier.of("test", "block_2"), (s) -> new PolymerBlockItem(BLOCK_2, s, Items.TNT));
    public static BlockItem BLOCK_ITEM_3 = registerItem(Identifier.of("test", "block_3"), (s) -> new PolymerBlockItem(BLOCK_3, s, Items.COBWEB));
    public static TinyPotatoBlock TATER_BLOCK = registerBlock(Identifier.of("test", "tater"), (s) -> new TinyPotatoBlock(s.strength(10f)));
    public static BlockItem TATER_BLOCK_ITEM = registerItem(Identifier.of("test", "tater"), (s) -> new PolymerHeadBlockItem(TATER_BLOCK, s.maxCount(99)));
    public static TestPickaxeItem PICKAXE = registerItem(Identifier.of("test", "pickaxe"), (s) -> new TestPickaxeItem(Items.WOODEN_PICKAXE, ToolMaterial.NETHERITE, 10, -3.9f, s));
    public static TestPickaxeItem PICKAXE2 = registerItem(Identifier.of("test", "pickaxe2"), (s) -> new TestPickaxeItem(Items.NETHERITE_PICKAXE, ToolMaterial.WOOD, 10, -5f, s));
    public static TestHelmetItem HELMET = registerItem(Identifier.of("test", "helmet"), TestHelmetItem::new);
    public static Block WRAPPED_BLOCK = registerBlock(Identifier.of("test", "wrapped"), AbstractBlock.Settings.copy(BLOCK), (s) -> new SimplePolymerBlock(s, BLOCK));
    public static Block SELF_REFERENCE_BLOCK = registerBlock(Identifier.of("test", "self"),AbstractBlock.Settings.copy(Blocks.STONE), (s) -> new SelfReferenceBlock(s));
    public static Item WRAPPED_ITEM = registerItem(Identifier.of("test", "wrapped"), (s) -> new SimplePolymerItem(s, ITEM));

    public static Block WEAK_GLASS_BLOCK = registerBlock(Identifier.of("test", "glass"), AbstractBlock.Settings.copy(Blocks.GLASS), WeakGlassBlock::new);
    public static Item WEAK_GLASS_BLOCK_ITEM = registerItem(Identifier.of("test", "glass"), (s) -> new PolymerBlockItem(WEAK_GLASS_BLOCK, s, Items.GLASS));

    public static Block MANA_CAULDRON = registerBlock(Identifier.of("test", "mana_cauldron"), AbstractBlock.Settings.copy(Blocks.CAULDRON), ManaCauldron::new);
    public static Item MANA_CAULDRON_ITEM = registerItem(Identifier.of("test", "mana_cauldron"), (s) -> new PolymerBlockItem(MANA_CAULDRON, s, Items.CAULDRON));

    public static Item CAMERA_ITEM = registerItem(Identifier.of("test", "camera"), (s) ->  new SimplePolymerItem(s.fireproof().maxCount(5), Items.IRON_DOOR) {
        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new SetCameraEntityS2CPacket(entity));
            }

            return super.useOnEntity(stack, user, entity, hand);
        }
    });

    public static Item FACE_PUNCHER = registerItem(Identifier.of("test", "tilt"), (s) -> new SimplePolymerItem(s.fireproof().maxCount(1), Items.STONE_SWORD) {

        @Override
        public ActionResult use(World world, PlayerEntity user, Hand hand) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                var stack = user.getStackInHand(hand);
                var x = /*stack.hasNbt() && stack.getNbt().contains("value", NbtElement.NUMBER_TYPE) ? stack.getNbt().getFloat("value") :*/ Math.random() * 360;

                serverPlayer.networkHandler.sendPacket(new DamageTiltS2CPacket(user.getId(), (float) x));
            }
            return super.use(world, user, hand);
        }
    });

    public static Item  FORCE_RIDER = registerItem(Identifier.of("test", "ride"), (s) ->  new SimplePolymerItem(s.fireproof().maxCount(1), Items.SADDLE) {

        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
            user.startRiding(entity, true);
            return super.useOnEntity(stack, user, entity, hand);
        }
    });

    public static Identifier CUSTOM_STAT;

    public static final RecipeType<TestRecipe> TEST_RECIPE_TYPE = RecipeType.register("test");
    public static final RecipeSerializer<TestRecipe> TEST_RECIPE_SERIALIZER = new TestRecipe.Serializer();

    public static final StatusEffect STATUS_EFFECT = new TestStatusEffect();
    public static final StatusEffect STATUS_EFFECT_2 = new Test2StatusEffect();
    public static final Potion POTION = new SimplePolymerPotion(new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT), 300));
    public static final Potion POTION_2 = new SimplePolymerPotion(new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT_2), 300));
    public static final Potion LONG_POTION = new SimplePolymerPotion("potion", new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT), 600));
    public static final Potion LONG_POTION_2 = new SimplePolymerPotion("potion", new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT_2), 600));

    public static final EntityType<TestEntity> ENTITY = registerEntity("entity", EntityType.Builder.create(TestEntity::new, SpawnGroup.CREATURE).dimensions(0.75f, 1.8f));

    public static final EntityType<TestEntity2> ENTITY_2 = registerEntity("entity2", EntityType.Builder.create(TestEntity2::new, SpawnGroup.CREATURE).dimensions(0.75f, 1.8f));
    public static final EntityType<TestEntity3> ENTITY_3 = registerEntity("entity3", EntityType.Builder.create(TestEntity3::new, SpawnGroup.CREATURE).dimensions(0.75f, 1.8f));

    public static final EntityType<UnrealBlockEntity> PHYSIC_ENTITY_3 = registerEntity("psych", EntityType.Builder.create(UnrealBlockEntity::new, SpawnGroup.CREATURE).dimensions(1, 1)
            .trackingTickInterval(1));

    public static final Item TEST_ENTITY_EGG = registerItem(Identifier.of("test", "spawn_egg"), (s) -> new PolymerSpawnEggItem(ENTITY, Items.COW_SPAWN_EGG, s));
    public static Item TEST_FOOD;
    public static final Item TEST_FOOD_2 = registerItem(Identifier.of("test", "food2"), (s) -> new SimplePolymerItem(s.food(new FoodComponent.Builder().nutrition(1).saturationModifier(2).build()), Items.CAKE));

    //public static final SoundEvent GHOST_HURT = new PolymerSoundEvent(PolymerResourcePackUtils.getMainUuid(), Identifier.of("polymertest", "ghosthurt"), 16, true, SoundEvents.ENTITY_GHAST_HURT);
    

    public static SimplePolymerItem ICE_ITEM = registerItem(Identifier.of("test", "ice"), (s) -> new ClickItem(s, Items.SNOWBALL, (player, hand) -> {
        //var tracker = new DataTracker(null);
        //tracker.startTracking(EntityAccessor.getFROZEN_TICKS(), Integer.MAX_VALUE);
        //player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), tracker.getChangedEntries()));

        var attributes = player.getAttributes().getAttributesToSend();
        var tmp = new EntityAttributeInstance(EntityAttributes.MOVEMENT_SPEED, (x) -> {});
        tmp.setBaseValue(player.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED));
        tmp.addPersistentModifier(new EntityAttributeModifier(Identifier.of("test", "test"), 0.05d, EntityAttributeModifier.Operation.ADD_VALUE));
        attributes.add(tmp);

        player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), attributes));

        TranslatableTextContent.
        CODEC.xmap(Function.identity(), (content) -> {
            if (content.getFallback() != null) {
                var target = LocalizationTarget.forPacket();
                if (target != null) {
                    return new TranslatableTextContent(content.getKey(), target.getLanguage().serverTranslations().get(content.getKey()), content.getArgs());
                }
            }
            return content;
        });
    }));

    public static SimplePolymerItem SPEC_ITEM = registerItem(Identifier.of("test", "spec"), (s) -> new ClickItem(s, Items.ENDER_EYE, (player, hand) -> {
        player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getId()));
        player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
    }));



    public static SimplePolymerItem MARKER_TEST = registerItem(Identifier.of("test", "marker"), (s) -> new ClickItem(s, Items.BLAZE_ROD, (player, hand) -> {
        if (hand == Hand.OFF_HAND) {
            DebugInfoSender.clearGameTestMarkers((ServerWorld) player.getWorld());
        } else {
            // Red Blue Green Alpha
            // Blue Alpha Green Red

            /*DebugInfoSender.addGameTestMarker((ServerWorld) player.getWorld(), player.getBlockPos(), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb(0xFF, 0, 0, 0),
                    Integer.MAX_VALUE);

            DebugInfoSender.addGameTestMarker((ServerWorld) player.getWorld(), player.getBlockPos().up(), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb(0, 0x22, 0, 0xEE),
                    Integer.MAX_VALUE);

            DebugInfoSender.addGameTestMarker((ServerWorld) player.getWorld(), player.getBlockPos().up(2), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb( 0xFF, 0xFF, 0xFF, 0x22),
                    Integer.MAX_VALUE);*/
        }
    }));
    public static Block ANIMATED_BLOCK = registerBlock(Identifier.of("test", "animated"), s -> new AnimatedBlock(s.luminance((state) -> 15).strength(2f)));
    public static BlockItem ANIMATED_BLOCK_ITEM = registerItem(Identifier.of("test", "animated"), (s) -> new PolymerBlockItem(ANIMATED_BLOCK, s, Items.BEACON));

    private static void regArmor(EquipmentSlot slot, String main, String id) {
        registerItem(Identifier.of("test", main + "_" + id), (s) -> new TestArmor(slot, Identifier.of("polymertest", "item/" + main + "_" + id), s));
    }

    public void onInitialize() {
        //ITEM_GROUP.setIcon();
        PolymerResourcePackUtils.addModAssets("apolymertest");
        PolymerResourcePackUtils.addBridgedModelsFolder(Identifier.of("polymertest", "testificate"));
        PolymerResourcePackUtils.getInstance().setPackDescription(Text.literal("TEST REPLACED DESCRIPTION").formatted(Formatting.GREEN));
        //PolymerResourcePackUtils.markAsRequired();
        //PolymerResourcePackUtils.addModAsAssetsSource("promenade");
        //register(Registries.ITEM_GROUP, Identifier.of("polymer", "test"), ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of("test:group"), ITEM_GROUP);
        registerItem(Identifier.of("bugged", "wooden_sword"), (s) -> new BuggedItem(s));




        Registry.register(Registries.STATUS_EFFECT, Identifier.of("test", "effect"), STATUS_EFFECT);
        register(Registries.STATUS_EFFECT, Identifier.of("test", "effect2"), STATUS_EFFECT_2);

        TEST_FOOD = registerItem(Identifier.of("test", "food"), (s) -> new SimplePolymerItem(s.food(new FoodComponent.Builder().nutrition(10).saturationModifier(20)
                .alwaysEdible().build(),  ConsumableComponent.builder().consumeEffect(new ApplyEffectsConsumeEffect(
                new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(Identifier.of("test", "effect")).get(), 20), 1)).build()), Items.POISONOUS_POTATO));
        registerItem(Identifier.of("test", "emerald"), (s) -> new SimplePolymerItem(s, Items.EMERALD));

        for (var i = 0; i < 1600; i++) {
            registerBlock(Identifier.of("test", "filler_" + i), TestBlock::new);
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            var t = registerBlock(Identifier.of("test", "server_block"), AbstractBlock.Settings.copy(Blocks.OBSIDIAN), (s) -> new SimplePolymerBlock(s, Blocks.TINTED_GLASS));
            registerItem(Identifier.of("test", "server_block"), (s) -> new PolymerBlockItem(t, s, Items.TINTED_GLASS));
        }

        STILL_FLUID = register(Registries.FLUID, Identifier.of("test", "fluid"), new TestFluid.Still());
        FLOWING_FLUID = register(Registries.FLUID, Identifier.of("test", "flowing_fluid"), new TestFluid.Flowing());
        FLUID_BUCKET = registerItem(Identifier.of("test", "fluid_bucket"),
                (s) -> new TestBucketItem(STILL_FLUID, s.recipeRemainder(Items.BUCKET).maxCount(1), Items.LAVA_BUCKET));
        FLUID_BLOCK = registerBlock(Identifier.of("test", "fluid_block"), AbstractBlock.Settings.copy(Blocks.WATER), (s) -> new TestFluidBlock(STILL_FLUID, s));

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

        CUSTOM_STAT = PolymerStat.registerStat("test:custom_stat", StatFormatter.DEFAULT);

        register(Registries.RECIPE_SERIALIZER, Identifier.of("test", "test"), TEST_RECIPE_SERIALIZER);

        register(Registries.POTION, Identifier.of("test", "potion"), POTION);
        register(Registries.POTION, Identifier.of("test", "potion2"), POTION_2);
        register(Registries.POTION, Identifier.of("test", "long_potion"), LONG_POTION);
        register(Registries.POTION, Identifier.of("test", "long_potion_2"), LONG_POTION_2);

        FabricDefaultAttributeRegistry.register(ENTITY, TestEntity.createCreeperAttributes().add(EntityAttributes.LUCK));

        FabricDefaultAttributeRegistry.register(ENTITY_2, TestEntity2.createCreeperAttributes());

        FabricDefaultAttributeRegistry.register(ENTITY_3, TestEntity3.createCreeperAttributes());

        register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of("test", "test"), TestEnchantmentEntityEffect.CODEC);

        PolymerEntityUtils.registerType(ENTITY, ENTITY_2, ENTITY_3, PHYSIC_ENTITY_3);

        //PolymerItemUtils.ITEM_CHECK.register((itemStack) -> itemStack.hasNbt() && itemStack.getNbt().contains("Test", NbtElement.STRING_TYPE));

        PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((original, virtual, player) -> {
            //if (original.hasNbt() && original.getNbt().contains("Test", NbtElement.STRING_TYPE)) {
            //    ItemStack out = new ItemStack(Items.DIAMOND_SWORD, virtual.getCount());
            //    out.setNbt(virtual.getNbt());
            //    out.setCustomName(Text.literal("TEST VALUE: " + original.getNbt().getString("Test")).formatted(Formatting.WHITE));
            //    return out;
           // }
            return virtual;
        });



        CommandRegistrationCallback.EVENT.register((d, b, c) -> {
            d.register(literal("test")
                    .executes((ctx) -> {
                        try {
                            ctx.getSource().sendFeedback(() -> Text.literal("" + PolymerResourcePackUtils.hasPack(ctx.getSource().getPlayer(), PolymerResourcePackUtils.getMainUuid())), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return 0;
                    })
            );
            d.register(literal("incrementStat")
                    .executes((ctx) -> {
                        ctx.getSource().getPlayer().incrementStat(CUSTOM_STAT);
                        ctx.getSource().sendFeedback(() -> Text.literal("Stat now: " + ctx.getSource().getPlayer().getStatHandler().getStat(Stats.CUSTOM, CUSTOM_STAT)), false);

                        return 1;
                    })
            );
        });

        PolymerEntityUtils.registerAttribute(ATTRIBUTE);

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        CommandRegistrationCallback.EVENT.register((d, b, c) -> d.register(literal("test2").executes((ctx) -> {
            try {
                var player = ctx.getSource().getPlayer();
                if (atomicBoolean.get()) {
                    //PolymerSyncUtils.sendCreativeTab(ITEM_GROUP_2, player.networkHandler);
                } else {
                    //PolymerSyncUtils.removeCreativeTab(ITEM_GROUP_2, player.networkHandler);
                }
                PolymerSyncUtils.rebuildItemGroups(player.networkHandler);
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

        PolymerItemGroupUtils.LIST_EVENT.register((p, s) -> {
            if (atomicBoolean.get()) {
                //s.add(ITEM_GROUP_2);
            }
        });

        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 1, (factories, rebalanced) -> {
            factories.add((e, r) -> (new TradeOffer(new TradedItem(TEST_FOOD, 2), Optional.of(new TradedItem(TEST_FOOD, 1)),
                    TEST_FOOD.getDefaultStack(), 67, 0, 1)));
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
            //Collections.shuffle(entry.getValue());

            for (var e : entry.getValue()) {
                Registry.register((Registry<Object>) entry.getKey(), e.getLeft(), e.getRight());


            }
        }

        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("op_blocks"))).register(entries -> {
            entries.addAfter(Items.DEBUG_STICK, TEST_ENTITY_EGG);
        });


        ItemGroupEvents.modifyEntriesEvent(PolymerItemGroupUtils.getKey(ITEM_GROUP)).register(entries -> {
            entries.addAfter(TEST_FOOD, Items.LAVA_BUCKET);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            var creep = new CreeperEntity(EntityType.CREEPER, s.getOverworld());
            new Thread(() -> {
                try {
                    while (!s.isStopped()) {
                        s.getPlayerManager().getPlayerList().forEach(x -> {
                            var i = x.getMainHandStack();
                            if (i.isOf(Items.EGG)) {
                                x.setPose(EntityPose.SLEEPING);
                                x.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(x.getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.POSE, EntityPose.SLEEPING))));
                            } else if (i.isOf(Items.CREEPER_HEAD)) {
                                /*var l = new ArrayList<Packet<? super ClientPlayPacketListener>>();
                                creep.setPos(x.getX(), x.getY() - 255, x.getZ());
                                //l.add(new EntitySpawnS2CPacket(creep));
                                l.add(new SetCameraEntityS2CPacket(creep));
                                l.add(new EntitiesDestroyS2CPacket(creep.getId()));
                                l.add(new PlayerRespawnS2CPacket(x.createCommonPlayerSpawnInfo(x.getServerWorld()), PlayerRespawnS2CPacket.KEEP_ALL));

                                x.networkHandler.sendPacket(new BundleS2CPacket(l));*/
                            }
                        });
                        Thread.sleep(5);
                    }
                } catch (Throwable e) {

                }
            }).start();
        });


        var local = new ThreadLocal<Boolean>();
        local.set(Boolean.TRUE);
        long localTime = System.currentTimeMillis();

        if (PolymerImpl.IS_CLIENT) {
            InternalClientRegistry.decodeState(-1);
        }
    }
    
    public static <B, T extends B> T register(Registry<B> registry, Identifier id, T obj) {
        REG_CACHE.computeIfAbsent(registry, (r) -> new ArrayList<>()).add(new Pair<>(id, obj));
        return obj;
    }

    public static <T extends Item> T registerItem(Identifier id, Function<Item.Settings, T> obj) {
        return register(Registries.ITEM, id, obj.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id))));
    }

    public static <T extends Block> T registerBlock(Identifier id, Function<Block.Settings, T> obj) {
        return registerBlock(id, AbstractBlock.Settings.create(), obj);
    }
    public static <T extends Block> T registerBlock(Identifier id, AbstractBlock.Settings settings, Function<Block.Settings, T> obj) {
        return register(Registries.BLOCK, id, obj.apply(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id))));
    }

    public static <T extends Entity> EntityType<T> registerEntity(String entity, EntityType.Builder<T> v) {
        return register(Registries.ENTITY_TYPE, Identifier.of("test", entity), v.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("test", entity))));
    }
}
