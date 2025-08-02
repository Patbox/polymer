package eu.pb4.polymer.resourcepack.api.metadata;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

import java.util.Map;

public record LanguageResourceMetadata(Map<String, LanguageDefinition> definitions) {
    public static final Codec<String> LANGUAGE_CODE_CODEC = Codec.string(1, 16);
    public static final Codec<LanguageResourceMetadata> CODEC = SortedMapCodec.of(LANGUAGE_CODE_CODEC, LanguageDefinition.CODEC)
            .xmap(LanguageResourceMetadata::new, LanguageResourceMetadata::definitions);
    public static final ResourceMetadataSerializer<LanguageResourceMetadata> SERIALIZER = new ResourceMetadataSerializer<>("language", CODEC);
}