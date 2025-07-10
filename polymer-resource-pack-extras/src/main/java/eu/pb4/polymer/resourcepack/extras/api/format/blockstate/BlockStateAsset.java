package eu.pb4.polymer.resourcepack.extras.api.format.blockstate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record BlockStateAsset(Optional<Map<String, List<StateModelVariant>>> variants, Optional<List<StateMultiPartDefinition>> multipart) {
    public static final Codec<BlockStateAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        StateModelVariant.MAP.optionalFieldOf("variants").forGetter(BlockStateAsset::variants),
        StateMultiPartDefinition.CODEC.listOf().optionalFieldOf("multipart").forGetter(BlockStateAsset::multipart)
    ).apply(instance, BlockStateAsset::new));
}
