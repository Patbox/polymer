package eu.pb4.polymer.ext.blocks.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.Map;

public class PolymerBlocksInternal {
    public static Map<BlockState, PolymerBlockModel[]> modelMap = Collections.emptyMap();



    public static JsonArray createJsonElement(PolymerBlockModel[] models) {
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

        return array;
    }

    public static String generateStateName(BlockState state) {

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

        return stringBuilder.toString();
    }
}
