package eu.pb4.polymer.resourcepack.api.metadata;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record LanguageResourceMetadata(Map<String, LanguageDefinition> definitions) {
    public static final Codec<String> LANGUAGE_CODE_CODEC = Codec.string(1, 16);
    public static final Codec<LanguageResourceMetadata> CODEC = SortedMapCodec.of(LANGUAGE_CODE_CODEC, LanguageDefinition.CODEC)
            .xmap(LanguageResourceMetadata::new, LanguageResourceMetadata::definitions);
    public static final MetadataSectionType<LanguageResourceMetadata> SERIALIZER = new MetadataSectionType<>("language", CODEC);
}