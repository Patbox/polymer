package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.*;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
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
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
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
    public static SimplePolymerItem ITEM = new TestItem(new Item.Settings().fireproof().maxCount(5), Items.IRON_HOE);
    public static SimplePolymerItem ITEM_2 = new SimplePolymerItem(new Item.Settings().fireproof().maxCount(99)
            .attributeModifiers(AttributeModifiersComponent.builder().add(ATTRIBUTE,
                    new EntityAttributeModifier(Identifier.of("test:aaa"), 5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build()), Items.DIAMOND_BLOCK);
    public static SimplePolymerItem ITEM_3 = new SimplePolymerItem(new Item.Settings().fireproof().maxCount(99), Items.CHAINMAIL_CHESTPLATE);
    public static Block BLOCK = new TestBlock(AbstractBlock.Settings.create().luminance((state) -> 15).strength(2f));
    public static BlockItem BLOCK_ITEM = new PolymerBlockItem(BLOCK, new Item.Settings(), Items.STONE);
    public static Block BLOCK_PLAYER = new TestPerPlayerBlock(AbstractBlock.Settings.create().strength(2f));
    public static BlockItem BLOCK_PLAYER_ITEM = new PolymerBlockItem(BLOCK_PLAYER, new Item.Settings(), Items.WHITE_CARPET);
    public static Block BLOCK_CLIENT = new TestClientBlock(AbstractBlock.Settings.create().luminance((state) -> 3).strength(2f));
    public static BlockItem BLOCK_CLIENT_ITEM = new TestClientBlockItem(BLOCK_CLIENT, new Item.Settings());
    public static Block BLOCK_FENCE = new SimplePolymerBlock(AbstractBlock.Settings.create().luminance((state) -> 15).strength(2f), Blocks.NETHER_BRICK_FENCE);
    public static BlockItem BLOCK_FENCE_ITEM = new PolymerBlockItem(BLOCK_FENCE, new Item.Settings(), Items.NETHER_BRICK_FENCE);
    public static Block BLOCK_2 = new SimplePolymerBlock(AbstractBlock.Settings.create().strength(2f), Blocks.TNT);
    public static Block BLOCK_3 = new Test3Block(AbstractBlock.Settings.create().strength(2f));
    public static BlockItem BLOCK_ITEM_2 = new PolymerBlockItem(BLOCK_2, new Item.Settings(), Items.TNT);
    public static BlockItem BLOCK_ITEM_3 = new PolymerBlockItem(BLOCK_3, new Item.Settings(), Items.COBWEB);
    public static TinyPotatoBlock TATER_BLOCK = new TinyPotatoBlock(AbstractBlock.Settings.create().strength(10f));
    public static BlockItem TATER_BLOCK_ITEM = new PolymerHeadBlockItem(TATER_BLOCK, new Item.Settings().maxCount(9999));
    public static BlockItem TATER_BLOCK_ITEM2 = new PolymerBlockItem(TATER_BLOCK, new Item.Settings(), Items.RAW_IRON_BLOCK);
    public static TestPickaxeItem PICKAXE = new TestPickaxeItem(Items.WOODEN_PICKAXE, ToolMaterials.NETHERITE, 10, -3.9f, new Item.Settings());
    public static TestPickaxeItem PICKAXE2 = new TestPickaxeItem(Items.NETHERITE_PICKAXE, ToolMaterials.WOOD, 10, -5f, new Item.Settings());
    public static TestHelmetItem HELMET = new TestHelmetItem(new Item.Settings());
    public static Block WRAPPED_BLOCK = new SimplePolymerBlock(AbstractBlock.Settings.copy(BLOCK), BLOCK);
    public static Block SELF_REFERENCE_BLOCK = new SelfReferenceBlock(AbstractBlock.Settings.copy(Blocks.STONE));
    public static Item WRAPPED_ITEM = new SimplePolymerItem(new Item.Settings(), ITEM);

    public static Block WEAK_GLASS_BLOCK = new WeakGlassBlock(AbstractBlock.Settings.copy(Blocks.GLASS));
    public static Item WEAK_GLASS_BLOCK_ITEM = new PolymerBlockItem(WEAK_GLASS_BLOCK, new Item.Settings(), Items.GLASS);

    public static Block MANA_CAULDRON = new ManaCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON));
    public static Item MANA_CAULDRON_ITEM = new PolymerBlockItem(MANA_CAULDRON, new Item.Settings(), Items.CAULDRON);


    public static TestBowItem BOW_1 = new TestBowItem(new Item.Settings(), "bow");
    public static TestBowItem BOW_2 = new TestBowItem(new Item.Settings(), "bow2");

    public static Item CAMERA_ITEM = new SimplePolymerItem(new Item.Settings().fireproof().maxCount(5), Items.IRON_DOOR) {
        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new SetCameraEntityS2CPacket(entity));
            }

            return super.useOnEntity(stack, user, entity, hand);
        }
    };

    public static Item FACE_PUNCHER = new SimplePolymerItem(new Item.Settings().fireproof().maxCount(1), Items.STONE_SWORD) {

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                var stack = user.getStackInHand(hand);
                var x = /*stack.hasNbt() && stack.getNbt().contains("value", NbtElement.NUMBER_TYPE) ? stack.getNbt().getFloat("value") :*/ Math.random() * 360;

                serverPlayer.networkHandler.sendPacket(new DamageTiltS2CPacket(user.getId(), (float) x));
            }
            return super.use(world, user, hand);
        }
    };

    public static Item  FORCE_RIDER = new SimplePolymerItem(new Item.Settings().fireproof().maxCount(1), Items.SADDLE) {

        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
            user.startRiding(entity, true);
            return super.useOnEntity(stack, user, entity, hand);
        }
    };

    public static Identifier CUSTOM_STAT;

    public static final RecipeType<TestRecipe> TEST_RECIPE_TYPE = RecipeType.register("test");
    public static final RecipeSerializer<TestRecipe> TEST_RECIPE_SERIALIZER = new TestRecipe.Serializer();

    public static final StatusEffect STATUS_EFFECT = new TestStatusEffect();
    public static final StatusEffect STATUS_EFFECT_2 = new Test2StatusEffect();
    public static final Potion POTION = new SimplePolymerPotion(new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT), 300));
    public static final Potion POTION_2 = new SimplePolymerPotion(new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT_2), 300));
    public static final Potion LONG_POTION = new SimplePolymerPotion("potion", new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT), 600));
    public static final Potion LONG_POTION_2 = new SimplePolymerPotion("potion", new StatusEffectInstance(RegistryEntry.of(STATUS_EFFECT_2), 600));

    public static final EntityType<TestEntity> ENTITY = EntityType.Builder.create(TestEntity::new, SpawnGroup.CREATURE).dimensions(0.75f, 1.8f).build("ig");
    public static final EntityType<TestEntity2> ENTITY_2 = EntityType.Builder.create(TestEntity2::new, SpawnGroup.CREATURE).dimensions(0.75f, 1.8f).build("no");
    public static final EntityType<TestEntity3> ENTITY_3 = EntityType.Builder.create(TestEntity3::new, SpawnGroup.CREATURE).dimensions(0.75f, 1.8f).build("re");

    public static final EntityType<UnrealBlockEntity> PHYSIC_ENTITY_3 = EntityType.Builder.create(UnrealBlockEntity::new, SpawnGroup.CREATURE).dimensions(1, 1)
            .trackingTickInterval(1).build("re");

    public static final Item TEST_ENTITY_EGG = new PolymerSpawnEggItem(ENTITY, Items.COW_SPAWN_EGG, new Item.Settings());
    public static Item TEST_FOOD;
    public static final Item TEST_FOOD_2 = new SimplePolymerItem(new Item.Settings().food(new FoodComponent.Builder().nutrition(1).saturationModifier(2).build()), Items.CAKE);

    public static final SoundEvent GHOST_HURT = new PolymerSoundEvent(PolymerResourcePackUtils.getMainUuid(), Identifier.of("polymertest", "ghosthurt"), 16, true, SoundEvents.ENTITY_GHAST_HURT);
    
    private static final Map<Registry<?>, List<Pair<Identifier, ?>>> REG_CACHE = new HashMap<>();

    public static SimplePolymerItem ICE_ITEM = new ClickItem(new Item.Settings(), Items.SNOWBALL, (player, hand) -> {
        //var tracker = new DataTracker(null);
        //tracker.startTracking(EntityAccessor.getFROZEN_TICKS(), Integer.MAX_VALUE);
        //player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), tracker.getChangedEntries()));

        var attributes = player.getAttributes().getAttributesToSend();
        var tmp = new EntityAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED, (x) -> {});
        tmp.setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
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
    });

    public static SimplePolymerItem SPEC_ITEM = new ClickItem(new Item.Settings(), Items.ENDER_EYE, (player, hand) -> {
        player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getId()));
        player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
    });



    public static SimplePolymerItem MARKER_TEST = new ClickItem(new Item.Settings(), Items.BLAZE_ROD, (player, hand) -> {
        if (hand == Hand.OFF_HAND) {
            DebugInfoSender.clearGameTestMarkers((ServerWorld) player.getWorld());
        } else {
            // Red Blue Green Alpha
            // Blue Alpha Green Red

            DebugInfoSender.addGameTestMarker((ServerWorld) player.getWorld(), player.getBlockPos(), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb(0xFF, 0, 0, 0),
                    Integer.MAX_VALUE);

            DebugInfoSender.addGameTestMarker((ServerWorld) player.getWorld(), player.getBlockPos().up(), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb(0, 0x22, 0, 0xEE),
                    Integer.MAX_VALUE);

            DebugInfoSender.addGameTestMarker((ServerWorld) player.getWorld(), player.getBlockPos().up(2), player.getStackInHand(hand).getCount() > 1 ? "Test: " + Math.random() : "",
                    ColorHelper.Argb.getArgb( 0xFF, 0xFF, 0xFF, 0x22),
                    Integer.MAX_VALUE);
        }
    });
    public static Block ANIMATED_BLOCK = new AnimatedBlock(AbstractBlock.Settings.create().luminance((state) -> 15).strength(2f));
    public static BlockItem ANIMATED_BLOCK_ITEM = new PolymerBlockItem(ANIMATED_BLOCK, new Item.Settings(), Items.BEACON);

    private static void regArmor(EquipmentSlot slot, String main, String id) {
        register(Registries.ITEM, Identifier.of("test", main + "_" + id), new TestArmor(slot, Identifier.of("polymertest", "item/" + main + "_" + id), Identifier.of("polymertest", main)));
    }

    public void onInitialize() {
        //ITEM_GROUP.setIcon();
        PolymerResourcePackUtils.addModAssets("apolymertest");
        PolymerResourcePackUtils.requestArmor(Identifier.of("polymertest", "shulker"));
        PolymerResourcePackUtils.getInstance().setPackDescription(Text.literal("TEST REPLACED DESCRIPTION").formatted(Formatting.GREEN));
        //PolymerResourcePackUtils.markAsRequired();
        //PolymerResourcePackUtils.addModAsAssetsSource("promenade");
        //register(Registries.ITEM_GROUP, Identifier.of("polymer", "test"), ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of("test:group"), ITEM_GROUP);
        register(Registries.ITEM, Identifier.of("bugged", "wooden_sword"), new BuggedItem(new Item.Settings()));



        Registry.register(Registries.STATUS_EFFECT, Identifier.of("test", "effect"), STATUS_EFFECT);
        register(Registries.STATUS_EFFECT, Identifier.of("test", "effect2"), STATUS_EFFECT_2);

        TEST_FOOD = new SimplePolymerItem(new Item.Settings().food(new FoodComponent.Builder().nutrition(10).saturationModifier(20)
                .alwaysEdible().statusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(Identifier.of("test", "effect")).get(), 20), 1).build()), Items.POISONOUS_POTATO);
        register(Registries.ITEM, Identifier.of("test", "emerald"), new SimplePolymerItem(new Item.Settings(), Items.EMERALD));
        register(Registries.ITEM, Identifier.of("test", "item"), ITEM);
        register(Registries.ITEM, Identifier.of("test", "item2"), ITEM_2);
        register(Registries.ITEM, Identifier.of("test", "item3"), ITEM_3);
        register(Registries.BLOCK, Identifier.of("test", "block"), BLOCK);
        register(Registries.ITEM, Identifier.of("test", "block"), BLOCK_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "block_client"), BLOCK_CLIENT);
        register(Registries.ITEM, Identifier.of("test", "block_client"), BLOCK_CLIENT_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "block_player"), BLOCK_PLAYER);
        register(Registries.ITEM, Identifier.of("test", "block_player"), BLOCK_PLAYER_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "block_fence"), BLOCK_FENCE);
        register(Registries.ITEM, Identifier.of("test", "block_fence"), BLOCK_FENCE_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "block2"), BLOCK_2);
        register(Registries.ITEM, Identifier.of("test", "block2"), BLOCK_ITEM_2);
        register(Registries.BLOCK, Identifier.of("test", "block3"), BLOCK_3);
        register(Registries.ITEM, Identifier.of("test", "block3"), BLOCK_ITEM_3);
        register(Registries.BLOCK, Identifier.of("test", "potato_block"), TATER_BLOCK);
        register(Registries.ITEM, Identifier.of("test", "potato_block"), TATER_BLOCK_ITEM);
        register(Registries.ITEM, Identifier.of("test", "potato_block2"), TATER_BLOCK_ITEM2);
        register(Registries.ITEM, Identifier.of("test", "pickaxe"), PICKAXE);
        register(Registries.ITEM, Identifier.of("test", "pickaxe2"), PICKAXE2);
        register(Registries.ITEM, Identifier.of("test", "helmet"), HELMET);
        register(Registries.ITEM, Identifier.of("test", "bow1"), BOW_1);
        register(Registries.ITEM, Identifier.of("test", "bow2"), BOW_2);
        register(Registries.BLOCK, Identifier.of("test", "wrapped_block"), WRAPPED_BLOCK);
        register(Registries.BLOCK, Identifier.of("test", "self_block"), SELF_REFERENCE_BLOCK);
        register(Registries.ITEM, Identifier.of("test", "wrapped_item"), WRAPPED_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "weak_glass"), WEAK_GLASS_BLOCK);
        register(Registries.ITEM, Identifier.of("test", "weak_glass"), WEAK_GLASS_BLOCK_ITEM);
        register(Registries.ITEM, Identifier.of("test", "ice_item"), ICE_ITEM);
        register(Registries.ITEM, Identifier.of("test", "spawn_egg"), TEST_ENTITY_EGG);
        register(Registries.ITEM, Identifier.of("test", "food"), TEST_FOOD);
        register(Registries.ITEM, Identifier.of("test", "food2"), TEST_FOOD_2);
        register(Registries.ITEM, Identifier.of("test", "camera"), CAMERA_ITEM);
        register(Registries.ITEM, Identifier.of("test", "cmarker_test"), MARKER_TEST);
        register(Registries.ITEM, Identifier.of("test", "spec"), SPEC_ITEM);
        register(Registries.ITEM, Identifier.of("test", "tilt"), FACE_PUNCHER);
        register(Registries.ITEM, Identifier.of("test", "rider"), FORCE_RIDER);

        register(Registries.ITEM, Identifier.of("test", "animated"), ANIMATED_BLOCK_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "animated"), ANIMATED_BLOCK);

        register(Registries.ITEM, Identifier.of("test", "mana_cauldron"), MANA_CAULDRON_ITEM);
        register(Registries.BLOCK, Identifier.of("test", "mana_cauldron"), MANA_CAULDRON);

        for (var i = 0; i < 16; i++) {
            register(Registries.BLOCK, Identifier.of("test", "filler_" + i), new TestBlock(AbstractBlock.Settings.create()));
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            var t = new SimplePolymerBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN), Blocks.TINTED_GLASS);
            register(Registries.BLOCK, Identifier.of("test", "server_block"), t);
            register(Registries.ITEM, Identifier.of("test", "server_block"), new PolymerBlockItem(t, new Item.Settings(), Items.TINTED_GLASS));
        }

        STILL_FLUID = register(Registries.FLUID, Identifier.of("test", "fluid"), new TestFluid.Still());
        FLOWING_FLUID = register(Registries.FLUID, Identifier.of("test", "flowing_fluid"), new TestFluid.Flowing());
        FLUID_BUCKET = register(Registries.ITEM, Identifier.of("test", "fluid_bucket"),
                new TestBucketItem(STILL_FLUID, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1), Items.LAVA_BUCKET));
        FLUID_BLOCK = register(Registries.BLOCK, Identifier.of("test", "fluid_block"), new TestFluidBlock(STILL_FLUID, AbstractBlock.Settings.copy(Blocks.WATER)));

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

        register(Registries.ENTITY_TYPE, Identifier.of("test", "entity"), ENTITY);
        FabricDefaultAttributeRegistry.register(ENTITY, TestEntity.createCreeperAttributes().add(EntityAttributes.GENERIC_LUCK));

        register(Registries.ENTITY_TYPE, Identifier.of("test", "entity2"), ENTITY_2);
        FabricDefaultAttributeRegistry.register(ENTITY_2, TestEntity2.createCreeperAttributes());

        register(Registries.ENTITY_TYPE, Identifier.of("test", "entity3"), ENTITY_3);
        FabricDefaultAttributeRegistry.register(ENTITY_3, TestEntity3.createCreeperAttributes());

        register(Registries.ENTITY_TYPE, Identifier.of("test", "physics"), PHYSIC_ENTITY_3);

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
}
