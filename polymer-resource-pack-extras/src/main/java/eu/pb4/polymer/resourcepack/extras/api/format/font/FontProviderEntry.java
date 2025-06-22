package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FontProviderEntry(FontProvider provider, FontProviderFilter filter) {
    public static final Codec<FontProviderEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FontProvider.CODEC.forGetter(FontProviderEntry::provider),
            FontProviderFilter.CODEC.optionalFieldOf("filter", FontProviderFilter.DEFAULT).forGetter(FontProviderEntry::filter)
    ).apply(instance, FontProviderEntry::new));

    public FontProviderEntry(FontProvider provider) {
        this(provider, FontProviderFilter.DEFAULT);
    }
}
