package eu.pb4.polymer.resourcepack.api.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;

public record LanguageDefinition(String region, String name, boolean rightToLeft) {
    public static final Codec<LanguageDefinition> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codecs.NON_EMPTY_STRING.fieldOf("region").forGetter(LanguageDefinition::region), Codecs.NON_EMPTY_STRING.fieldOf("name").forGetter(LanguageDefinition::name), Codec.BOOL.optionalFieldOf("bidirectional", false).forGetter(LanguageDefinition::rightToLeft)).apply(instance, LanguageDefinition::new);
    });
}
