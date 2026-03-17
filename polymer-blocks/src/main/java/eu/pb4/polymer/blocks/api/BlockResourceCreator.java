package eu.pb4.polymer.blocks.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import eu.pb4.polymer.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.blocks.impl.DefaultModelData;
import eu.pb4.polymer.blocks.impl.PolymerBlocksInternal;
import eu.pb4.polymer.blocks.impl.VanillaBlockPropertiesPredicate;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public final class BlockResourceCreator {
    private static final PolymerBlockModel EMPTY = PolymerBlockModel.of(Identifier.fromNamespaceAndPath("polymer", "block/empty"));
    private final Map<BlockModelType, List<BlockState>> states;
    private final Set<Block> hasRequested = Collections.newSetFromMap(new IdentityHashMap<>());
    final Map<BlockState, Either<PolymerBlockModel[], MultiPolymerBlockModel>> models;
    private final ResourcePackCreator creator;
    private final Runnable onRegister;
    private final BlockExtBlockMapper blockMapper;

    private final EnumMap<BlockModelType, BlockState> emptyBlocks = new EnumMap<>(BlockModelType.class);

    private boolean registered = false;
    private boolean registeredEmpty = false;

    public static BlockResourceCreator of(ResourcePackCreator creator) {
        if (CompatStatus.POLYMC) {
            PolymerImpl.LOGGER.warn("Polymer Blocks non-global module might not work correctly with PolyMC! Be warned!");
        }

        return new BlockResourceCreator(creator, new BlockExtBlockMapper(BlockMapper.createDefault()), () -> {});
    }

    BlockResourceCreator(ResourcePackCreator creator, BlockExtBlockMapper blockMapper, Runnable onRegister) {
        this.states = new EnumMap<>(BlockModelType.class);
        DefaultModelData.USABLE_STATES.forEach((key, value) -> this.states.put(key, new ReferenceArrayList<>(value)));
        this.models = new IdentityHashMap<>(DefaultModelData.MODELS);
        this.creator = creator;
        this.blockMapper = blockMapper;
        this.onRegister = onRegister;
    }

    public BlockMapper getBlockMapper() {
        return this.blockMapper;
    }

    private void registerEvent() {
        if (!this.registered) {
            creator.creationEvent.register((b) -> {
                if (b instanceof DefaultRPBuilder<?> defaultRPBuilder) {
                    defaultRPBuilder.buildEvent.register(c -> this.generateResources(defaultRPBuilder, c));
                }
            });
            this.onRegister.run();
            this.registered = true;
        }
    }

    @Nullable
    public BlockState requestEmpty(BlockModelType type) {
        var x = this.emptyBlocks.get(type);
        if (x != null) {
            return x;
        }
        Predicate<BlockState> predicate = null;
        if (type.name().contains("TRAPDOOR")) {
            predicate = b -> b.is(Blocks.IRON_TRAPDOOR);
        } else if (type.name().contains("DOOR")) {
            predicate = b -> b.is(Blocks.IRON_DOOR);
        }  else if (type == BlockModelType.VINES_BLOCK) {
            predicate = b -> b.is(Blocks.TWISTING_VINES);
        }

        if (predicate != null) {
            x = requestBlockImpl(type, predicate, true, Either.left(new PolymerBlockModel[]{EMPTY}));
        }
        if (x == null) {
            x = requestBlockImpl(type, y -> true, true, Either.left(new PolymerBlockModel[]{EMPTY}));
        }
        if (x == null) {
            return null;
        }
        this.emptyBlocks.put(type, x);
        if (!this.registeredEmpty) {
            this.registeredEmpty = true;
            this.creator.addAssetSource("polymer-blocks");
        }
        return x;
    }

    @Nullable
    public BlockState requestBlock(BlockModelType type, PolymerBlockModel... model) {
        return requestBlock(type, x -> true, model);
    }

    public BlockState requestBlock(BlockModelType type, Predicate<BlockState> predicate, PolymerBlockModel... model) {
        return requestBlockImpl(type, predicate, false, Either.left(model));
    }

    @Nullable
    public BlockState requestBlock(BlockModelType type, MultiPolymerBlockModel model) {
        return requestBlock(type, x -> true, model);
    }

    public BlockState requestBlock(BlockModelType type, Predicate<BlockState> predicate, MultiPolymerBlockModel model) {
        return requestBlockImpl(type, predicate, false, Either.right(model));
    }

    private BlockState requestBlockImpl(BlockModelType type, Predicate<BlockState> predicate, boolean reversed, Either<PolymerBlockModel[], MultiPolymerBlockModel> model) {
        var states = this.states.get(type);
        if (!states.isEmpty()) {
            if (reversed) {
                states = states.reversed();
            }

            BlockState state = null;
            for (var s : states) {
                if (predicate.test(s)) {
                    state = s;
                    break;
                }
            }
            if (state == null) {
                return null;
            }
            states.remove(state);
            models.put(state, model);
            this.hasRequested.add(state.getBlock());
            this.registerEvent();

            if (state.getBlock() instanceof SimpleWaterloggedBlock) {
                this.blockMapper.stateMap.put(state, DefaultModelData.SPECIAL_REMAPS
                        .getOrDefault(state, (state.getBlock() instanceof LeavesBlock
                                ? state.getBlock().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true) : state.getBlock().defaultBlockState()).setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED)))
                );
            } else {
                this.blockMapper.stateMap.put(state, DefaultModelData.SPECIAL_REMAPS
                        .getOrDefault(state, state.getBlock() instanceof LeavesBlock
                                ? state.getBlock().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true) : state.getBlock().defaultBlockState())
                );
            }

            return state;
        }
        return null;
    }

    public int getBlocksLeft(BlockModelType type) {
        return this.states.get(type).size();
    }

    public boolean hasRequestedEmpty(BlockModelType type) {
        return this.emptyBlocks.containsKey(type);
    }

    private void generateResources(DefaultRPBuilder builder, List<String> credits) {
        if (CompatStatus.POLYMC && this == PolymerBlockResourceUtils.CREATOR) {
            // PolyMC's generation is better, so just leave it for now...
            return;
        }

        var keys = new HashSet<Map.Entry<String, Block>>();

        var variants = new HashMap<String, HashMap<String, JsonArray>>();
        var multipart = new HashMap<String, List<JsonObject>>();
        var bannedStates = new HashMap<String, JsonArray>();

        for (var blockStateEntry : this.models.entrySet()) {
            if (!this.hasRequested.contains(blockStateEntry.getKey().getBlock())) {
                continue;
            }
            var state = blockStateEntry.getKey();
            var models = blockStateEntry.getValue();

            var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            var path = "assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json";
            keys.add(Map.entry(path, state.getBlock()));

            var banned = bannedStates.computeIfAbsent(path, (s) -> new JsonArray());
            var obj = new JsonObject();
            var el = new JsonArray();
            var selfMulti = new JsonObject();

            for (var prop : state.getProperties()) {
                var name = prop.getName();
                var obj2 = new JsonObject();
                //noinspection rawtypes,unchecked
                var value = ((Property) prop).getName(state.getValue(prop));
                obj2.addProperty(name,  "!" + value);
                selfMulti.addProperty(name, value);
                el.add(obj2);
            }
            obj.add("OR", el);
            banned.add(obj);

            if (models.left().isPresent()) {
                var stateName = PolymerBlocksInternal.generateStateName(state);
                var array = PolymerBlocksInternal.createJsonElement(models.left().orElseThrow());
                variants.computeIfAbsent(path, (s) -> new HashMap<>()).put(stateName, array);
            } else {
                for (var x : models.right().orElseThrow().models()) {
                    var mult = new JsonObject();
                    mult.add("when", selfMulti);
                    mult.add("apply", PolymerBlocksInternal.createJsonElement(x));
                    multipart.computeIfAbsent(path, (s) -> new ArrayList<>()).add(mult);
                }
            }

        }

        for (var keyVal : keys) {
            var key = keyVal.getKey();
            try {
                var modelObject = new JsonObject();

                var variantsObject = new JsonObject();
                var multipartObject = new JsonArray();

                if (variants.containsKey(key)) {
                    var values = new ArrayList<>(variants.get(key).entrySet());
                    values.sort(Map.Entry.comparingByKey());
                    for (var entries : values) {
                        variantsObject.add(entries.getKey(), entries.getValue());
                    }
                }


                if (multipart.containsKey(key)) {
                    multipart.get(key).forEach(multipartObject::add);

                    var vanillaData = builder.getDataOrSource(key);
                    if (vanillaData != null) {
                        var vanillaJson = JsonParser.parseString(new String(vanillaData, StandardCharsets.UTF_8)).getAsJsonObject();
                        if (vanillaJson.has("variants")) {
                            var values = new ArrayList<>(vanillaJson.get("variants").getAsJsonObject().entrySet());
                            values.sort(Map.Entry.comparingByKey());
                            for (var entries : values) {
                                var predicate = VanillaBlockPropertiesPredicate.parse(keyVal.getValue().getStateDefinition(), entries.getKey());

                                for (var state : keyVal.getValue().getStateDefinition().getPossibleStates()) {
                                    if (predicate.test(state) && !this.models.containsKey(state)) {
                                        variantsObject.add(PolymerBlocksInternal.generateStateName(state), entries.getValue());
                                    }
                                }
                            }
                        }
                        if (vanillaJson.has("multipart")) {
                            for (var entry : vanillaJson.get("multipart").getAsJsonArray()) {
                                var val = entry.getAsJsonObject().deepCopy();
                                var list = new JsonArray();
                                if (val.has("when")) {
                                    list.add(val.get("when"));
                                }

                                var ban2 = new JsonObject();
                                ban2.add("AND", bannedStates.get(key));
                                list.add(ban2);

                                var when = new JsonObject();
                                when.add("AND", list);

                                val.add("when", when);
                                multipartObject.add(val);
                            }
                        }
                    }
                }


                if (!variantsObject.isEmpty()) {
                    modelObject.add("variants", variantsObject);
                }

                if (!multipartObject.isEmpty()) {
                    modelObject.add("multipart", multipartObject);
                }

                builder.addData(key, DefaultRPBuilder.GSON.toJson(modelObject).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                PolymerImpl.LOGGER.warn("Exception occurred while building block model!", e);
            }
        }

    }
}
