package eu.pb4.polymer.resourcepack.extras.api.format.item;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.WritableAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.ItemModel;

public record ItemAsset(ItemModel model, Properties properties) implements WritableAsset.Json {
    public static final Codec<ItemAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemModel.CODEC.fieldOf("model").forGetter(ItemAsset::model),
            Properties.CODEC.forGetter(ItemAsset::properties)
        ).apply(instance, ItemAsset::new)
    );

    public ItemAsset(ItemModel model) {
        this(model, Properties.DEFAULT);
    }

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static ItemAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }

    public record Properties(boolean handAnimationOnSwap, boolean oversizedInGui) {
        public static final Properties DEFAULT = new Properties(true, false);
        public static final MapCodec<Properties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("hand_animation_on_swap", true).forGetter(Properties::handAnimationOnSwap),
                Codec.BOOL.optionalFieldOf("oversized_in_gui", false).forGetter(Properties::oversizedInGui)
        ).apply(instance, Properties::new));

        public Properties(boolean handAnimationOnSwap) {
            this(handAnimationOnSwap, false);
        }
    }
}