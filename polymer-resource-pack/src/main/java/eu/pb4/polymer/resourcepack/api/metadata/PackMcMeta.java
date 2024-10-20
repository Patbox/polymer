package eu.pb4.polymer.resourcepack.api.metadata;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.mixin.accessors.PackOverlaysMetadataAccessor;
import eu.pb4.polymer.resourcepack.mixin.accessors.ResourceFilterAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.BlockEntry;
import net.minecraft.resource.metadata.PackOverlaysMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceFilter;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Range;

import java.util.*;

public record PackMcMeta(PackResourceMetadata pack, Optional<ResourceFilter> filter, Optional<PackOverlaysMetadata> overlays, Optional<LanguageResourceMetadata> language) {
    public static final Codec<PackMcMeta> CODEC = RecordCodecBuilder.create(instaince -> instaince.group(
            PackResourceMetadata.CODEC.fieldOf("pack").forGetter(PackMcMeta::pack),
            ResourceFilterAccessor.getCODEC().optionalFieldOf("filter").forGetter(PackMcMeta::filter),
            PackOverlaysMetadataAccessor.getCODEC().optionalFieldOf("overlays").forGetter(PackMcMeta::overlays),
            LanguageResourceMetadata.CODEC.optionalFieldOf("language").forGetter(PackMcMeta::language)
    ).apply(instaince, PackMcMeta::new));

    public static PackMcMeta fromString(String string) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(string)).getOrThrow().getFirst();
    }

    public String asString() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static class Builder {
        private PackResourceMetadata metadata = new PackResourceMetadata(
                Text.literal("Server Resource Pack"),
                SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES),
                Optional.empty()
        );
        private final List<BlockEntry> filter = new ArrayList<>();
        private final List<PackOverlaysMetadata.Entry> overlay = new ArrayList<>();
        private final Map<String, LanguageDefinition> language = new HashMap<>();

        public Builder metadata(PackResourceMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder description(Text description) {
            this.metadata = new PackResourceMetadata(description, this.metadata.packFormat(), this.metadata.supportedFormats());
            return this;
        }

        public Builder addFilter(BlockEntry entry) {
            this.filter.add(entry);
            return this;
        }

        public Builder addOverlay(Range<Integer> format, String overlay) {
            this.overlay.add(new PackOverlaysMetadata.Entry(format, overlay));
            return this;
        }

        public Builder addOverlay(PackOverlaysMetadata.Entry entry) {
            this.overlay.add(entry);
            return this;
        }

        public Builder addLanguage(String name, LanguageDefinition definition) {
            this.language.put(name, definition);
            return this;
        }

        public PackMcMeta build() {
            return new PackMcMeta(this.metadata,
                    this.filter.isEmpty() ? Optional.empty() : Optional.of(new ResourceFilter(this.filter)),
                    this.overlay.isEmpty() ? Optional.empty() : Optional.of(new PackOverlaysMetadata(this.overlay)),
                    this.language.isEmpty() ? Optional.empty() : Optional.of(new LanguageResourceMetadata(this.language))
            );
        }
    }
}
