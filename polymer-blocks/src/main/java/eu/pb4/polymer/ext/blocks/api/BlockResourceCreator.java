package eu.pb4.polymer.ext.blocks.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.api.resourcepack.ResourcePackCreator;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.ext.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.ext.blocks.impl.DefaultModelData;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockResourceCreator {
    private final Map<BlockModelType, List<BlockState>> states;
    private final Map<BlockState, PolymerBlockModel[]> models;
    private final ResourcePackCreator creator;
    private final Runnable onRegister;
    private final BlockExtBlockMapper blockMapper;

    private boolean registered = false;

    public static BlockResourceCreator of(ResourcePackCreator creator) {
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
        var map = new HashMap<String, HashMap<String, JsonArray>>();

        for (var blockStateEntry : this.models.entrySet()) {
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
}
