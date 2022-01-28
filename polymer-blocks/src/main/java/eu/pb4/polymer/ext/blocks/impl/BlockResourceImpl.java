package eu.pb4.polymer.ext.blocks.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockResourceImpl {
    private static final Map<PolymerBlockResourceUtils.Type, List<BlockState>> USABLE_STATES = new HashMap<>();
    private static final Map<BlockState, BlockState> SPECIAL_REMAPS = new HashMap<>();
    private static final Map<BlockState, PolymerBlockModel[]> MODELS = new HashMap<>();

    private static boolean registered = false;

    @Nullable
    public static BlockState requestBlock(PolymerBlockResourceUtils.Type type, PolymerBlockModel... model) {
        var states = USABLE_STATES.get(type);
        if (states.size() != 0) {
            registerEvent();
            var state = states.remove(0);
            MODELS.put(state, model);
            BlockExtBlockMapper.INSTANCE.stateMap.put(state, SPECIAL_REMAPS.getOrDefault(state, state.getBlock().getDefaultState()));
            return state;
        }
        return null;
    }

    public static int getBlocksLeft(PolymerBlockResourceUtils.Type type) {
        return USABLE_STATES.get(type).size();
    }


    private static void registerEvent() {
        if (!registered) {
            PolymerRPUtils.RESOURCE_PACK_CREATION_EVENT.register((b) -> {
                if (b instanceof DefaultRPBuilder defaultRPBuilder) {
                    defaultRPBuilder.buildEvent.register((c) -> generateResources(defaultRPBuilder, c));
                }
            });

            PolymerSyncUtils.PREPARE_HANDSHAKE.register((handler -> handler.setBlockMapper(BlockExtBlockMapper.INSTANCE)));
            registered = true;
        }
    }

    private static void generateResources(DefaultRPBuilder builder, List<String> credits) {
        var map = new HashMap<String, HashMap<String, JsonArray>>();

        for (var blockStateEntry : MODELS.entrySet()) {
            var state = blockStateEntry.getKey();
            var models = blockStateEntry.getValue();

            var id = Registry.BLOCK.getId(state.getBlock());

            var stringBuilder = new StringBuilder();

            var iterator = state.getEntries().entrySet().iterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                stringBuilder.append((entry.getKey()).getName()).append("=").append(((Property) entry.getKey()).name(entry.getValue()));

                if (iterator.hasNext()) {
                    stringBuilder.append(",");
                }
            }

            var array = new JsonArray();

            for (var model : models) {
                var modelObj = new JsonObject();

                modelObj.addProperty("model", model.model().toString());
                modelObj.addProperty("x", model.x());
                modelObj.addProperty("y", model.y());
                modelObj.addProperty("uvlock", model.uvLock());
                modelObj.addProperty("weight", model.weight());

                array.add(modelObj);
            }

            map.computeIfAbsent("assets/" + id.getNamespace()  + "/blockstates/" + id.getPath() + ".json", (s) -> new HashMap<>()).put(stringBuilder.toString(), array);
        }

        for (var baseEntry : map.entrySet()) {
            try {
                var modelObject = new JsonObject();

                var variants = new JsonObject();

                for (var entries : baseEntry.getValue().entrySet()) {
                    variants.add(entries.getKey(), entries.getValue());
                }

                modelObject.add("variants", variants);

                builder.addData(baseEntry.getKey(), DefaultRPBuilder.GSON.toJson(modelObject).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                PolymerImpl.LOGGER.warn(e);
            }
        }

    }

    static {
        var fullBlocks = new ArrayList<>(Blocks.NOTE_BLOCK.getStateManager().getStates());
        {
            var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier("minecraft:block/note_block"))};
            for (var state : fullBlocks) {
                MODELS.put(state, model);
            }
        }
        fullBlocks.remove(Blocks.NOTE_BLOCK.getDefaultState());
        USABLE_STATES.put(PolymerBlockResourceUtils.Type.FULL_BLOCK, fullBlocks);

        /*Registry.BLOCK.stream().filter((b) -> b instanceof SlabBlock).forEach((block) -> {
            var state = block.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);

            var id = Registry.BLOCK.getId(block);
            for (var string : new String[]{"", "s", "_block", "_planks", ""}) {
                var possibleReplacements = Registry.BLOCK.get(new Identifier(id.getNamespace(), id.getPath().replace("_slab", string)));

                if (possibleReplacements != Blocks.AIR) {
                    SPECIAL_REMAPS.put(state, possibleReplacements.getDefaultState());
                    fullBlocks.add(state);
                    break;
                }
            }
        });*/


        var leaves = new ArrayList<BlockState>();

        for (var block : new Block[]{ Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES }) {

            var id = Registry.BLOCK.getId(block);
            var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
            for (var state : block.getStateManager().getStates()) {
                MODELS.put(state, model);
            }

            leaves.addAll(block.getStateManager().getStates());
            leaves.remove(block.getDefaultState());
        }

        USABLE_STATES.put(PolymerBlockResourceUtils.Type.TRANSPARENT_BLOCK, leaves);
    }
}
