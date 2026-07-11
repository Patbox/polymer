package eu.pb4.polymer.resourcepack.extras.api.format.blockstate;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.WritableAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record BlockStateAsset(Optional<Map<String, List<StateModelVariant>>> variants, Optional<List<StateMultiPartDefinition>> multipart) implements WritableAsset.Json {
    public static final Codec<BlockStateAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        StateModelVariant.MAP.optionalFieldOf("variants").forGetter(BlockStateAsset::variants),
        StateMultiPartDefinition.CODEC.listOf().optionalFieldOf("multipart").forGetter(BlockStateAsset::multipart)
    ).apply(instance, BlockStateAsset::new));

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static BlockStateAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }
}
