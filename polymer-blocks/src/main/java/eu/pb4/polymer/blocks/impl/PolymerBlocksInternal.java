package eu.pb4.polymer.blocks.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        var stringBuilder = new StringBuilder();

        var entries = new ArrayList<>(state.getEntries().entrySet());
        entries.sort(Map.Entry.comparingByKey(Comparator.comparing(Property::getName)));
        var iterator = entries.iterator();

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
