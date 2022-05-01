package eu.pb4.polymer.ext.blocks.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.api.resourcepack.ResourcePackCreator;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.ext.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.ext.blocks.impl.DefaultModelData;
import eu.pb4.polymer.ext.blocks.impl.PolymerBlocksInternal;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class BlockResourceCreator {
    private final Map<BlockModelType, List<BlockState>> states;
    final Map<BlockState, PolymerBlockModel[]> models;
    private final ResourcePackCreator creator;
    private final Runnable onRegister;
    private final BlockExtBlockMapper blockMapper;

    private boolean registered = false;

    public static BlockResourceCreator of(ResourcePackCreator creator) {
        if (CompatStatus.POLYMC) {
            PolymerImpl.LOGGER.warn("Polymer Blocks non-global module might not work correctly with PolyMC! Be warned!");
        }

        return new BlockResourceCreator(creator, new BlockExtBlockMapper(BlockMapper.createDefault()), () -> {});
    }

    protected BlockResourceCreator(ResourcePackCreator creator, BlockExtBlockMapper blockMapper, Runnable onRegister) {
        this.states = new HashMap<>(DefaultModelData.USABLE_STATES);
        this.models = new HashMap<>(DefaultModelData.MODELS);
        this.creator = creator;
        this.blockMapper = blockMapper;
        this.onRegister = onRegister;
    }

    public BlockMapper getBlockMapper() {
        return this.blockMapper;
    }

    protected void registerEvent() {
        if (!this.registered) {
            creator.creationEvent.register((b) -> {
                if (b instanceof DefaultRPBuilder defaultRPBuilder) {
                    defaultRPBuilder.buildEvent.register((c) -> this.generateResources(defaultRPBuilder, c));
                }
            });
            this.onRegister.run();
            this.registered = true;
        }
    }

    @Nullable
    public BlockState requestBlock(BlockModelType type, PolymerBlockModel... model) {
        var states = this.states.get(type);
        if (states.size() != 0) {
            this.registerEvent();
            var state = states.remove(0);
            models.put(state, model);
            this.blockMapper.stateMap.put(state, DefaultModelData.SPECIAL_REMAPS.getOrDefault(state, state.getBlock().getDefaultState()));
            return state;
        }
        return null;
    }

    public int getBlocksLeft(BlockModelType type) {
        return this.states.get(type).size();
    }

    private void generateResources(DefaultRPBuilder builder, List<String> credits) {
        if (CompatStatus.POLYMC && this == PolymerBlockResourceUtils.CREATOR) {
            // PolyMC's generation is better, so just leave it for now...
            return;
        }

        var map = new HashMap<String, HashMap<String, JsonArray>>();

        for (var blockStateEntry : this.models.entrySet()) {
            var state = blockStateEntry.getKey();
            var models = blockStateEntry.getValue();

            var id = Registry.BLOCK.getId(state.getBlock());

            var stateName = PolymerBlocksInternal.generateStateName(state);
            var array = PolymerBlocksInternal.createJsonElement(models);

            map.computeIfAbsent("assets/" + id.getNamespace()  + "/blockstates/" + id.getPath() + ".json", (s) -> new HashMap<>()).put(stateName, array);
        }

        for (var baseEntry : map.entrySet()) {
            try {
                var modelObject = new JsonObject();

                var variants = new JsonObject();

                var values = new ArrayList<>(baseEntry.getValue().entrySet());
                values.sort(Comparator.comparing(e -> e.getKey()));
                for (var entries : values) {
                    variants.add(entries.getKey(), entries.getValue());
                }

                modelObject.add("variants", variants);

                builder.addData(baseEntry.getKey(), DefaultRPBuilder.GSON.toJson(modelObject).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                PolymerImpl.LOGGER.warn("Exception occurred while building block model!", e);
            }
        }

    }
}
