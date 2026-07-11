package eu.pb4.polymertest;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.*;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.other.PolymerStat;
import eu.pb4.polymer.core.api.other.SimplePolymerPotion;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.BlockStateAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.font.FontAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.ItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.commands.Commands.literal;


public class TestMod implements ModInitializer {
    private static final Map<Registry<?>, List<Pair<Identifier, ?>>> REG_CACHE = new HashMap<>();

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab.Builder(null, -1)
            .title(Component.translatable("testmod.itemgroup").withStyle(ChatFormatting.AQUA))
            .icon(()-> new ItemStack(TestMod.TATER_BLOCK_ITEM))
            .displayItems(new CreativeModeTab.DisplayItemsGenerator() {
                @Override
                public void accept(CreativeModeTab.ItemDisplayParameters arg, CreativeModeTab.Output entries) {
                    entries.accept(Items.DAMAGED_ANVIL.getDefaultInstance());
                    entries.accept(Items.MUSIC_DISC_5.getDefaultInstance());
                    var items = REG_CACHE.get(BuiltInRegistries.ITEM);

                    for (var pair : items) {
                        entries.accept((ItemLike) pair.getSecond());
                    }
                }
            })
            .build();
    public static Block FLUID_BLOCK;
    public static TestFluid.Flowing FLOWING_FLUID;
    public static TestFluid.Still STILL_FLUID;
    public static BucketItem FLUID_BUCKET;

    public static Holder<Attribute> ATTRIBUTE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Identifier.parse("test:attribute"),
            new RangedAttribute("test.attribute", 0, -5, 5)
                    .setSentiment(Attribute.Sentiment.POSITIVE).setSyncable(true));
    public static DynamicItem DYNAMIC_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "dynamic"), DynamicItem::new);
    public static SimplePolymerItem ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "item"), (s) -> new TestItem(s.fireResistant().stacksTo(5), Items.IRON_HOE));
    public static SimplePolymerItem ITEM_2 = registerItem(Identifier.fromNamespaceAndPath("test", "item_2"), (s) -> new SimplePolymerItem(s.fireResistant().stacksTo(99)
            .attributes(ItemAttributeModifiers.builder().add(ATTRIBUTE,
                    new AttributeModifier(Identifier.parse("test:aaa"), 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build()), Items.DIAMOND_BLOCK));
    public static SimplePolymerItem ITEM_3 = registerItem(Identifier.fromNamespaceAndPath("test", "item_3"), (s) -> new SimplePolymerItem(s.fireResistant().stacksTo(99), Items.CHAINMAIL_CHESTPLATE));
    public static Block BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "block"), (s) ->
            new TestBlock(s.lightLevel((state) -> 15).sound(SoundType.IRON).strength(2f)));
    public static Block BLOCK_USE = registerBlock(Identifier.fromNamespaceAndPath("test", "block_use"), (s) -> new TestUseBlock(s
            .lightLevel((state) -> state.getValue(TestUseBlock.LIT) ? 15 : 0).strength(2f)));
    public static BlockItem BLOCK_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "block"), (s) -> new PolymerBlockItem(BLOCK, s, Items.STONE));
    public static BlockItem BLOCK_USE_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "block_use"), (s) -> new PolymerBlockItem(BLOCK_USE, s, Items.REDSTONE_LAMP));
    public static Block BLOCK_PLAYER = registerBlock(Identifier.fromNamespaceAndPath("test", "block_player"), (s) -> new TestPerPlayerBlock(s.strength(2f)));
    public static BlockItem BLOCK_PLAYER_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "block_player"), (s) -> new PolymerBlockItem(BLOCK_PLAYER, s, Items.CARPET.white()));
    public static Block BLOCK_CLIENT = registerBlock(Identifier.fromNamespaceAndPath("test", "block_client"), (s) -> new TestClientBlock(s.lightLevel((state) -> 3).strength(2f)));
    public static BlockItem BLOCK_CLIENT_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "block_client"), (s) -> new TestClientBlockItem(BLOCK_CLIENT, s));
    public static Block BLOCK_FENCE = registerBlock(Identifier.fromNamespaceAndPath("test", "fence"), (s) -> new SimplePolymerBlock(s.lightLevel((state) -> 15).strength(2f), Blocks.NETHER_BRICK_FENCE));
    public static BlockItem BLOCK_FENCE_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "fence"), (s) ->  new PolymerBlockItem(BLOCK_FENCE, s, Items.NETHER_BRICK_FENCE));
    public static Block BLOCK_2 = registerBlock(Identifier.fromNamespaceAndPath("test", "block_2"), (s) -> new SimplePolymerBlock(
            s.strength(2f).sound(SoundType.AMETHYST), Blocks.TNT));
    public static Block BLOCK_3 = registerBlock(Identifier.fromNamespaceAndPath("test", "block_3"), (s) -> new Test3Block(s.strength(2f)));
    public static BlockItem BLOCK_ITEM_2 = registerItem(Identifier.fromNamespaceAndPath("test", "block_2"), (s) -> new PolymerBlockItem(BLOCK_2, s, Items.TNT));
    public static BlockItem BLOCK_ITEM_3 = registerItem(Identifier.fromNamespaceAndPath("test", "block_3"), (s) -> new PolymerBlockItem(BLOCK_3, s, Items.COBWEB));
    public static TinyPotatoBlock TATER_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "tater"), (s) -> new TinyPotatoBlock(s.strength(10f)));
    public static BlockItem TATER_BLOCK_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "tater"), (s) -> new PolymerHeadBlockItem(TATER_BLOCK, s.stacksTo(99).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath("polymertest", "test")))));
    public static TestPickaxeItem PICKAXE = registerItem(Identifier.fromNamespaceAndPath("test", "pickaxe"), (s) -> new TestPickaxeItem(Items.WOODEN_PICKAXE, ToolMaterial.NETHERITE, 10, -3.9f, s));
    public static TestPickaxeItem PICKAXE2 = registerItem(Identifier.fromNamespaceAndPath("test", "pickaxe2"), (s) -> new TestPickaxeItem(Items.NETHERITE_PICKAXE, ToolMaterial.WOOD, 10, -5f, s));
    public static TestHelmetItem HELMET = registerItem(Identifier.fromNamespaceAndPath("test", "helmet"), TestHelmetItem::new);
    public static Block WRAPPED_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "wrapped"), BlockBehaviour.Properties.ofFullCopy(BLOCK), (s) -> new SimplePolymerBlock(s, BLOCK));
    public static Block SELF_REFERENCE_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "self"),BlockBehaviour.Properties.ofFullCopy(Blocks.STONE), (s) -> new SelfReferenceBlock(s));
    public static Item WRAPPED_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "wrapped"), (s) -> new SimplePolymerItem(s, ITEM));

    public static Block WEAK_GLASS_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "glass"), BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS), WeakGlassBlock::new);
    public static Item WEAK_GLASS_BLOCK_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "glass"), (s) -> new PolymerBlockItem(WEAK_GLASS_BLOCK, s, Items.GLASS));

    public static Block MANA_CAULDRON = registerBlock(Identifier.fromNamespaceAndPath("test", "mana_cauldron"), BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON), ManaCauldron::new);
    public static Item MANA_CAULDRON_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "mana_cauldron"), (s) -> new PolymerBlockItem(MANA_CAULDRON, s, Items.CAULDRON));

    public static Item CAMERA_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "camera"), (s) ->  new SimplePolymerItem(s.fireResistant().stacksTo(5), Items.IRON_DOOR) {
        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetCameraPacket(interactionTarget));
            }

            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
    });

    public static Item FACE_PUNCHER = registerItem(Identifier.fromNamespaceAndPath("test", "tilt"), (s) -> new SimplePolymerItem(s.fireResistant().stacksTo(1), Items.STONE_SWORD) {

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            if (user instanceof ServerPlayer serverPlayer) {
                var stack = user.getItemInHand(hand);
                var x = /*stack.hasNbt() && stack.getNbt().contains("value", NbtElement.NUMBER_TYPE) ? stack.getNbt().getFloat("value") :*/ Math.random() * 360;

                serverPlayer.connection.send(new ClientboundHurtAnimationPacket(user.getId(), (float) x));
            }
            return super.use(world, user, hand);
        }
    });

    public static Item  FORCE_RIDER = registerItem(Identifier.fromNamespaceAndPath("test", "ride"), (s) ->  new SimplePolymerItem(s.fireResistant().stacksTo(1), Items.SADDLE) {
        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
            player.startRiding(interactionTarget, true, true);
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
    });

    public static MaceItem OVERLAY_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "overlay_item"), s -> new MaceItem(s.fireResistant()));
    public static TntBlock OVERLAY_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "overlay_block"), s -> new TntBlock(s.sound(new SoundType(
            0.8f, 1,
            SoundEvents.CREEPER_DEATH,
            SoundEvents.CREEPER_PRIMED,
            SoundEvents.SNIFFER_HAPPY,
            SoundEvents.SNIFFER_HURT,
            SoundEvents.GENERIC_EAT.value()
    ))));
    public static BlockItem OVERLAY_BLOCK_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "overlay_block"), x -> new BlockItem(OVERLAY_BLOCK, x));
    public static EntityType<IronGolem> OVERLAY_ENTITY = registerEntity("overlay_entity",
            EntityType.Builder.of(IronGolem::new, MobCategory.CREATURE).sized(1f, 1.8f));

    public static Identifier CUSTOM_STAT;

    public static final RecipeType<TestRecipe> TEST_RECIPE_TYPE = RecipeType.register("test");
    public static final RecipeSerializer<TestRecipe> TEST_RECIPE_SERIALIZER = new RecipeSerializer<>(ItemStack.CODEC.xmap(TestRecipe::new, TestRecipe::stack).fieldOf("item"), null);

    public static final MobEffect STATUS_EFFECT = new TestStatusEffect();
    public static final MobEffect STATUS_EFFECT_2 = new Test2StatusEffect();
    public static final Potion POTION = new SimplePolymerPotion(new MobEffectInstance(Holder.direct(STATUS_EFFECT), 300));
    public static final Potion POTION_2 = new SimplePolymerPotion(new MobEffectInstance(Holder.direct(STATUS_EFFECT_2), 300));
    public static final Potion LONG_POTION = new SimplePolymerPotion("potion", new MobEffectInstance(Holder.direct(STATUS_EFFECT), 600));
    public static final Potion LONG_POTION_2 = new SimplePolymerPotion("potion", new MobEffectInstance(Holder.direct(STATUS_EFFECT_2), 600));

    public static final EntityType<TestEntity> ENTITY = registerEntity("entity", EntityType.Builder.of(TestEntity::new, MobCategory.CREATURE).sized(0.75f, 1.8f));

    public static final EntityType<TestEntity2> ENTITY_2 = registerEntity("entity2", EntityType.Builder.of(TestEntity2::new, MobCategory.CREATURE).sized(0.75f, 1.8f));
    public static final EntityType<TestEntity3> ENTITY_3 = registerEntity("entity3", EntityType.Builder.of(TestEntity3::new, MobCategory.CREATURE).sized(0.75f, 1.8f));
    public static final EntityType<ClientTestEntity3> CLIENT_ENTITY_3 = registerEntity("cliententity3", EntityType.Builder.of(ClientTestEntity3::new, MobCategory.CREATURE).sized(0.75f, 1.8f));

    public static final EntityType<UnrealBlockEntity> PHYSIC_ENTITY_3 = registerEntity("psych", EntityType.Builder.of(UnrealBlockEntity::new, MobCategory.CREATURE).sized(1, 1)
            .updateInterval(1));

    public static final Item TEST_ENTITY_EGG = registerItem(Identifier.fromNamespaceAndPath("test", "spawn_egg"), (s) -> new PolymerSpawnEggItem(Items.COW_SPAWN_EGG, s.spawnEgg(ENTITY)));
    public static Item TEST_FOOD;
    public static final Item TEST_FOOD_2 = registerItem(Identifier.fromNamespaceAndPath("test", "food2"), (s) -> new SimplePolymerItem(s.food(new FoodProperties.Builder().nutrition(1).saturationModifier(2).build()), Items.CAKE));
    public static final DataComponentType<String> TEST = register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("test", "test"),
            DataComponentType.<String>builder().persistent(Codec.STRING).build());

    public static final DataComponentType<Item> CLIENT_ITEM = register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("test", "item"),
            DataComponentType.<Item>builder().persistent(BuiltInRegistries.ITEM.byNameCodec()).build());


    //public static final SoundEvent GHOST_HURT = new PolymerSoundEvent(PolymerResourcePackUtils.getMainUuid(), Identifier.of("polymertest", "ghosthurt"), 16, true, SoundEvents.ENTITY_GHAST_HURT);
    

    public static SimplePolymerItem ICE_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "ice"), (s) -> new ClickItem(s, Items.SNOWBALL, (player, hand) -> {
        //var tracker = new DataTracker(null);
        //tracker.startTracking(EntityAccessor.getFROZEN_TICKS(), Integer.MAX_VALUE);
        //player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), tracker.getChangedEntries()));

        var attributes = player.getAttributes().getSyncableAttributes();
        var tmp = new AttributeInstance(Attributes.MOVEMENT_SPEED, (x) -> {});
        tmp.setBaseValue(player.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
        tmp.addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath("test", "test"), 0.05d, AttributeModifier.Operation.ADD_VALUE));
        attributes.add(tmp);

        player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), attributes));
    }));

    public static SimplePolymerItem SPEC_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "spec"), (s) -> new ClickItem(s, Items.ENDER_EYE, (player, hand) -> {
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, GameType.SPECTATOR.getId()));
        player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
    }));



    public static SimplePolymerItem MARKER_TEST = registerItem(Identifier.fromNamespaceAndPath("test", "marker"), (s) -> new ClickItem(s, Items.BLAZE_ROD, (player, hand) -> {
        if (hand == InteractionHand.OFF_HAND) {
            //DebugInfoSender.clearGameTestMarkers((ServerWorld) player.getWorld());
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
    public static Block ANIMATED_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "animated"), s -> new AnimatedBlock(s.lightLevel((state) -> 15).strength(2f)));
    public static BlockItem ANIMATED_BLOCK_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "animated"), (s) -> new PolymerBlockItem(ANIMATED_BLOCK, s, Items.BEACON));

    public static Block END_GATEWAY = registerBlock(Identifier.fromNamespaceAndPath("test", "end_gateway"), s -> new FakeEndGatewayBlock(s.lightLevel((state) -> 15).strength(2f)));
    public static BlockEntityType END_GATEWAY_BE = register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", "end_gateway"),
            FabricBlockEntityTypeBuilder.create(FakeEndGatewayBlockEntity::new, END_GATEWAY).build());
    public static BlockItem END_GATEWAY_ITEM = registerItem(Identifier.fromNamespaceAndPath("test", "end_gateway"), (s) -> new PolymerBlockItem(END_GATEWAY, s, Items.CONCRETE_POWDER.black()));



    private static void regArmor(EquipmentSlot slot, String main, String id) {
        registerItem(Identifier.fromNamespaceAndPath("test", main + "_" + id), (s) -> new TestArmor(slot, Identifier.fromNamespaceAndPath("polymertest", "item/" + main + "_" + id), s));
    }

    public void onInitialize() {
        MixinEnvironment.getCurrentEnvironment().audit();

        //ITEM_GROUP.setIcon();
        PolymerResourcePackUtils.addModAssets("apolymertest");
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.fromNamespaceAndPath("polymertest", "testificate"));
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.fromNamespaceAndPath("blocktest", "block"));
        PolymerResourcePackUtils.getInstance().setPackDescription(Component.literal("TEST REPLACED DESCRIPTION").withStyle(ChatFormatting.GREEN));
        //PolymerResourcePackUtils.markAsRequired();
        //PolymerResourcePackUtils.addModAsAssetsSource("promenade");
        //register(Registries.ITEM_GROUP, Identifier.of("polymer", "test"), ITEM_GROUP);
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.parse("test:group"), ITEM_GROUP);
        registerItem(Identifier.fromNamespaceAndPath("bugged", "wooden_sword"), BuggedItem::new);

        PolymerItemUtils.enableStonecutterFix();

        register(BuiltInRegistries.DIALOG_TYPE, Identifier.fromNamespaceAndPath("test", "dialog"), TestDialog.CODEC);
        register(BuiltInRegistries.DIALOG_BODY_TYPE, Identifier.fromNamespaceAndPath("test", "image"), TestDialogImageBody.CODEC);
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(TestDialogImageBody::generateResources);
        RegistryEntryAddedCallback.allEntries(BuiltInRegistries.RECIPE_SERIALIZER, new Consumer<Holder.Reference<RecipeSerializer<?>>>() {
            @Override
            public void accept(Holder.Reference<RecipeSerializer<?>> ref) {
                if (ref.key().identifier().getNamespace().equals("minecraft")) {
                    RecipeSynchronization.synchronizeRecipeSerializer(ref.value());
                }
            }
        });


        SoundPatcher.convertIntoServerSound(Blocks.TNT.defaultBlockState().getSoundType());
        SoundPatcher.convertIntoServerSound(Blocks.NOTE_BLOCK.defaultBlockState().getSoundType());
        SoundPatcher.convertIntoServerSound(Blocks.DISPENSER.defaultBlockState().getSoundType());
        SoundPatcher.convertIntoServerSound(Blocks.EMERALD_BLOCK.defaultBlockState().getSoundType());

        PolymerBlockUtils.registerBlockEntity(END_GATEWAY_BE);

        registerItem(Identifier.fromNamespaceAndPath("test", "hoe"), MelonHoe::new);

        PolymerItemUtils.registerOverlay(OVERLAY_ITEM, new PolymerItem() {
            @Override
            public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
                return Items.MACE;
            }

            @Override
            public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
                return null;
            }
        });

        PolymerItemUtils.registerOverlay(OVERLAY_BLOCK_ITEM, new PolymerItem() {
            @Override
            public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
                return Items.EMERALD_BLOCK;
            }

            @Override
            public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
                return null;
            }

            @Override
            public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
                return true;
            }
        });
        PolymerBlockUtils.registerOverlay(OVERLAY_BLOCK, new PolymerBlock() {
            @Override
            public BlockState getPolymerBlockState(BlockState state, @org.jspecify.annotations.Nullable PacketContext context) {
                return Blocks.EMERALD_BLOCK.defaultBlockState();
            }

            @Override
            public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
                return true;
            }
        });

        PolymerEntityUtils.registerOverlay(OVERLAY_ENTITY, (entity) -> new PolymerEntity() {
            @Override
            public EntityType<?> getPolymerEntityType(PacketContext context) {
                return EntityTypes.IRON_GOLEM;
            }


            @Override
            public void modifyRawEntityAttributeData(List<ClientboundUpdateAttributesPacket.AttributeSnapshot> data, ServerPlayer player, boolean initial) {
                data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.SCALE, ((IronGolem) entity).getBbHeight() / EntityTypes.IRON_GOLEM.getHeight(), List.of()));
                PolymerEntity.super.modifyRawEntityAttributeData(data, player, initial);
            }
        });

        PolymerEntityUtils.POLYMER_ENTITY_INTERACTION_CHECK.register((player, hand, stack, world, entity, actionResult) -> entity instanceof ServerPlayer && stack.is(Items.LEAD));

        FabricDefaultAttributeRegistry.register(OVERLAY_ENTITY, IronGolem.createAttributes());

        Registry.register(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath("test", "effect"), STATUS_EFFECT);
        register(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath("test", "effect2"), STATUS_EFFECT_2);

        TEST_FOOD = registerItem(Identifier.fromNamespaceAndPath("test", "food"), (s) -> new SimplePolymerItem(s.food(new FoodProperties.Builder().nutrition(10).saturationModifier(20)
                .alwaysEdible().build(),  Consumable.builder().onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.get(Identifier.fromNamespaceAndPath("test", "effect")).get(), 20), 1)).build()), Items.POISONOUS_POTATO));
        registerItem(Identifier.fromNamespaceAndPath("test", "emerald"), (s) -> new SimplePolymerItem(s, Items.EMERALD));

        for (var i = 0; i < 1600; i++) {
            registerBlock(Identifier.fromNamespaceAndPath("test", "filler_" + i), TestBlock::new);
        }

        var instaMine = registerBlock(Identifier.fromNamespaceAndPath("test", "insta_mine"), BlockBehaviour.Properties.of().strength(0).instabreak(), (s) -> new SimplePolymerBlock(s, Blocks.TINTED_GLASS));
        registerItem(Identifier.fromNamespaceAndPath("test", "insta_mine"), s -> new PolymerBlockItem(instaMine, s));
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (state.is(instaMine)) {
                player.sendSystemMessage(Component.literal("Broke instabrek"));
                return false;
            }
            return true;
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            var t = registerBlock(Identifier.fromNamespaceAndPath("test", "server_block"), BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN), (s) -> new SimplePolymerBlock(s, Blocks.TINTED_GLASS));
            registerItem(Identifier.fromNamespaceAndPath("test", "server_block"), (s) -> new PolymerBlockItem(t, s, Items.TINTED_GLASS));
        }

        STILL_FLUID = register(BuiltInRegistries.FLUID, Identifier.fromNamespaceAndPath("test", "fluid"), new TestFluid.Still());
        FLOWING_FLUID = register(BuiltInRegistries.FLUID, Identifier.fromNamespaceAndPath("test", "flowing_fluid"), new TestFluid.Flowing());
        FLUID_BUCKET = registerItem(Identifier.fromNamespaceAndPath("test", "fluid_bucket"),
                (s) -> new TestBucketItem(STILL_FLUID, s.craftRemainder(Items.BUCKET).stacksTo(1), Items.LAVA_BUCKET));
        FLUID_BLOCK = registerBlock(Identifier.fromNamespaceAndPath("test", "fluid_block"), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER), (s) -> new TestFluidBlock(STILL_FLUID, s));

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

        register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("test", "test"), TEST_RECIPE_SERIALIZER);

        register(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath("test", "potion"), POTION);
        register(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath("test", "potion2"), POTION_2);
        register(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath("test", "long_potion"), LONG_POTION);
        register(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath("test", "long_potion_2"), LONG_POTION_2);

        FabricDefaultAttributeRegistry.register(ENTITY, TestEntity.createAttributes().add(Attributes.LUCK));

        FabricDefaultAttributeRegistry.register(ENTITY_2, TestEntity2.createAttributes());

        FabricDefaultAttributeRegistry.register(ENTITY_3, TestEntity3.createAttributes());
        FabricDefaultAttributeRegistry.register(CLIENT_ENTITY_3, TestEntity3.createAttributes());

        register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.fromNamespaceAndPath("test", "test"), TestEnchantmentEntityEffect.CODEC);

        PolymerEntityUtils.registerType(ENTITY, ENTITY_2, ENTITY_3, PHYSIC_ENTITY_3, CLIENT_ENTITY_3);

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
                            ctx.getSource().sendSuccess(() -> Component.literal("" + PolymerResourcePackUtils.hasPack(ctx.getSource().getPlayer().connection, PolymerResourcePackUtils.getMainUuid())), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return 0;
                    })
            );
            d.register(literal("incrementStat")
                    .executes((ctx) -> {
                        ctx.getSource().getPlayer().awardStat(CUSTOM_STAT);
                        ctx.getSource().sendSuccess(() -> Component.literal("Stat now: " + ctx.getSource().getPlayer().getStats().getValue(Stats.CUSTOM, CUSTOM_STAT)), false);

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
                PolymerSyncUtils.rebuildCreativeModeTabs(player.connection);
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
                    BlockMapper.set(player.connection, BlockMapper.createDefault());
                } else {
                    var list = new ArrayList<BlockState>();
                    Block.BLOCK_STATE_REGISTRY.forEach(list::add);
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

                    BlockMapper.set(player.connection, BlockMapper.createMap(map));
                }
                PolymerUtils.reloadWorld(player);
                mapper.set(!atomicBoolean.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        })));

        PolymerCreativeModeTabUtils.LIST_EVENT.register((p, s) -> {
            if (atomicBoolean.get()) {
                //s.add(ITEM_GROUP_2);
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
            //Collections.shuffle(entry.getValue());

            for (var e : entry.getValue()) {
                Registry.register((Registry<Object>) entry.getKey(), e.getFirst(), e.getSecond());


            }
        }

        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.parse("op_blocks"))).register(entries -> {
            entries.accept(MARKER_TEST);
            entries.prepend(CAMERA_ITEM);
            entries.insertAfter(Items.DEBUG_STICK, TEST_ENTITY_EGG);
        });


        CreativeModeTabEvents.modifyOutputEvent(PolymerCreativeModeTabUtils.getKey(ITEM_GROUP)).register(entries -> {
            entries.insertAfter(TEST_FOOD, Items.LAVA_BUCKET);
        });

        ServerLifecycleEvents.SERVER_STARTING.register((s) -> {
            int failure = 0;
            for (var entity : BuiltInRegistries.ENTITY_TYPE.asHolderIdMap()) {
                if (entity.unwrapKey().orElseThrow().identifier().getNamespace().equals("minecraft")) {
                    var ent = InternalEntityHelpers.getEntity(entity.value());
                    if (ent == InternalEntityHelpers.getFakeEntity() || ent == null) {
                        failure++;
                    }
                }
            }
            if (failure != 0) {
                throw new IllegalStateException("Entity Helper broke");
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            var creep = new Creeper(EntityTypes.CREEPER, s.overworld());
            new Thread(() -> {
                try {
                    while (!s.isStopped()) {
                        s.getPlayerList().getPlayers().forEach(x -> {
                            var i = x.getMainHandItem();
                            if (i.is(Items.EGG)) {
                                x.setPose(Pose.SLEEPING);
                                x.connection.send(new ClientboundSetEntityDataPacket(x.getId(), List.of(SynchedEntityData.DataValue.create(EntityData.POSE, Pose.SLEEPING))));
                            } else if (i.is(Items.CREEPER_HEAD)) {
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

        DefaultItemComponentEvents.MODIFY.register(x -> x.modify(Items.DIAMOND, b -> b.set(DataComponents.MAX_STACK_SIZE, 99)));
        DefaultItemComponentEvents.MODIFY.register(x -> x.modify(Items.CHAINMAIL_HELMET, b -> b.set(DataComponents.EQUIPPABLE, null)));
        DefaultItemComponentEvents.MODIFY.register(x -> x.modify(Items.CHAINMAIL_CHESTPLATE, b -> b.set(DataComponents.EQUIPPABLE, null)));
        PolymerItemUtils.syncDefaultComponent(Items.DIAMOND, DataComponents.MAX_STACK_SIZE);
        PolymerItemUtils.syncDefaultComponent(Items.CHAINMAIL_HELMET, DataComponents.EQUIPPABLE);

        PolymerComponent.registerDataComponent(TEST, CLIENT_ITEM);

        BlockWithElementHolder.registerOverlay(Blocks.JUKEBOX, new JukeboxHolderCreator());
        BlockWithElementHolder.registerOverlay(Blocks.NOTE_BLOCK, new NoteblockHolderCreator());

        BlockWithElementHolder.registerOverlay(Blocks.JUKEBOX, new JukeboxHolderCreator());
        BlockWithElementHolder.registerOverlay(Blocks.NOTE_BLOCK, new NoteblockHolderCreator());

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var vanillaJar = PolymerCommonUtils.getClientJarRoot();
            var itemsBase = vanillaJar.resolve("/assets/minecraft/items/");
            var modelsBase = vanillaJar.resolve("/assets/minecraft/models/");
            var atlasBase = vanillaJar.resolve("/assets/minecraft/atlases/");
            var fontBase = vanillaJar.resolve("/assets/minecraft/font/");
            var blockStateBase = vanillaJar.resolve("/assets/minecraft/blockstates/");

            try {
                var value = new MutableInt();
                var count = new MutableInt();
                Files.walk(fontBase).forEach(path -> {
                    if (!path.toString().endsWith(".json")) {
                        return;
                    }
                    count.increment();
                    try {
                        var asset = FontAsset.fromJson(Files.readString(path));
                        value.increment();
                    } catch (Throwable e) {
                        System.err.println("Error while parsing file: " + path);
                        e.printStackTrace();
                    }
                });
                System.out.println("Parsed " + value + " out of " + count + " fonts!");
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                var value = new MutableInt();
                var count = new MutableInt();
                Files.walk(itemsBase).forEach(path -> {
                    if (!path.toString().endsWith(".json")) {
                        return;
                    }
                    count.increment();
                    try {
                        var asset = ItemAsset.fromJson(Files.readString(path));

                        assert asset.model().equals(asset.model().replaceChildren(ItemModel.Replacer.NO_OP));
                        //System.out.println(path + ">" + asset);
                        value.increment();
                    } catch (Throwable e) {
                        System.err.println("Error while parsing file: " + path);
                        e.printStackTrace();
                    }
                });
                System.out.println("Parsed " + value + " out of " + count + " item assets!");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                var value = new MutableInt();
                var count = new MutableInt();
                Files.walk(modelsBase).forEach(path -> {
                    if (!path.toString().endsWith(".json")) {
                        return;
                    }
                    count.increment();
                    try {
                        var asset = ModelAsset.fromJson(Files.readString(path));
                        //System.out.println(path + ">" + asset);
                        value.increment();
                    } catch (Throwable e) {
                        System.err.println("Error while parsing file: " + path);
                        e.printStackTrace();
                    }
                });
                System.out.println("Parsed " + value + " out of " + count + " models!");
            } catch (Throwable e) {
                e.printStackTrace();
            }

            try {
                var value = new MutableInt();
                var count = new MutableInt();
                Files.walk(atlasBase).forEach(path -> {
                    if (!path.toString().endsWith(".json")) {
                        return;
                    }
                    count.increment();
                    try {
                        var asset = AtlasAsset.fromJson(Files.readString(path));
                        //System.out.println(path + ">" + asset);
                        value.increment();
                    } catch (Throwable e) {
                        System.err.println("Error while parsing file: " + path);
                        e.printStackTrace();
                    }
                });
                System.out.println("Parsed " + value + " out of " + count + " atlases!");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                var value = new MutableInt();
                var count = new MutableInt();
                for (var block : BuiltInRegistries.BLOCK) {
                    var id = BuiltInRegistries.BLOCK.getKey(block);

                    if (!id.getNamespace().equals("minecraft")) {
                        continue;
                    }

                    var path = blockStateBase.resolve(id.getPath() + ".json");

                    count.increment();
                    try {
                        var asset = BlockStateAsset.fromJson(Files.readString(path));

                        BlockStateModelManager.addBlock(id, block, asset);

                        value.increment();
                    } catch (Throwable e) {
                        System.err.println("Error while parsing file: " + path);
                        e.printStackTrace();
                    }
                };
                System.out.println("Parsed " + value + " out of " + count + " blockstates!");
            } catch (Throwable e) {
                e.printStackTrace();
            }

            try {
                var asset = SoundsAsset.fromJson(new String(TestMod.class.getResourceAsStream("/test/sounds.json").readAllBytes(), StandardCharsets.UTF_8));
                System.out.println("Parsed sounds.json!");
            } catch (Throwable e) {
                System.err.println("Error while parsing file: sounds.json");
                e.printStackTrace();
            }

            var map = new HashMap<Object, Set<BlockState>>();

            BlockStateModelManager.MAP.forEach((state, model) -> {
                state = state.trySetValue(BlockStateProperties.WATERLOGGED, false);

                map.computeIfAbsent(model, _ -> new HashSet<>()).add(state);
            });

            var builder = new StringBuilder();

            for (var value : map.values()) {
                if (value.size() > 1) {
                    builder.append("====").append('\n');
                    for (var v : value) {
                        builder.append(v.toString()).append("\n");
                    }

                    builder.append("\n");
                }
            }

            try {
                Files.writeString(FabricLoader.getInstance().getGameDir().resolve("equal_models.txt"), builder.toString());
            } catch (IOException e) {

            }


        }).run();
    }
    
    public static <B, T extends B> T register(Registry<B> registry, Identifier id, T obj) {
        REG_CACHE.computeIfAbsent(registry, (r) -> new ArrayList<>()).add(Pair.of(id, obj));
        return obj;
    }

    public static <T extends Item> T registerItem(Identifier id, Function<Item.Properties, T> obj) {
        return register(BuiltInRegistries.ITEM, id, obj.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    }

    public static <T extends Block> T registerBlock(Identifier id, Function<BlockBehaviour.Properties, T> obj) {
        return registerBlock(id, BlockBehaviour.Properties.of(), obj);
    }
    public static <T extends Block> T registerBlock(Identifier id, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> obj) {
        return register(BuiltInRegistries.BLOCK, id, obj.apply(settings.setId(ResourceKey.create(Registries.BLOCK, id))));
    }

    public static <T extends Entity> EntityType<T> registerEntity(String entity, EntityType.Builder<T> v) {
        return register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", entity), v.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", entity))));
    }
}
