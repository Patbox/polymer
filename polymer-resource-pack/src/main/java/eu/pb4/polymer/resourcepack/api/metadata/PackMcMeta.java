package eu.pb4.polymer.resourcepack.api.metadata;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.mixin.accessors.ResourceFilterSectionAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.util.IdentifierPattern;
import net.minecraft.util.InclusiveRange;
import java.util.*;

public record PackMcMeta(PackMetadataSection pack, Optional<ResourceFilterSection> filter, Optional<OverlayMetadataSection> overlays, Optional<LanguageResourceMetadata> language) {
    public static final Codec<PackMcMeta> CODEC = RecordCodecBuilder.create(instaince -> instaince.group(
            PackMetadataSection.CLIENT_TYPE.codec().fieldOf("pack").forGetter(PackMcMeta::pack),
            ResourceFilterSectionAccessor.getCODEC().optionalFieldOf("filter").forGetter(PackMcMeta::filter),
            OverlayMetadataSection.CLIENT_TYPE.codec().optionalFieldOf("overlays").forGetter(PackMcMeta::overlays),
            LanguageResourceMetadata.CODEC.optionalFieldOf("language").forGetter(PackMcMeta::language)
    ).apply(instaince, PackMcMeta::new));

    public static PackMcMeta fromString(String string) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(string)).getOrThrow().getFirst();
    }

    public String asString() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static class Builder {
        private PackMetadataSection metadata = new PackMetadataSection(
                Component.literal("Server Resource Pack"),
                new InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES))
        );
        private final List<IdentifierPattern> filter = new ArrayList<>();
        private final List<OverlayMetadataSection.OverlayEntry> overlay = new ArrayList<>();
        private final Map<String, LanguageDefinition> language = new HashMap<>();

        public Builder metadata(PackMetadataSection metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder description(Component description) {
            this.metadata = new PackMetadataSection(description, this.metadata.supportedFormats());
            return this;
        }

        public Builder addFilter(IdentifierPattern entry) {
            this.filter.add(entry);
            return this;
        }

        public Builder addOverlay(InclusiveRange<PackFormat> format, String overlay) {
            this.overlay.add(new OverlayMetadataSection.OverlayEntry(format, overlay));
            return this;
        }

        public Builder addOverlay(OverlayMetadataSection.OverlayEntry entry) {
            this.overlay.add(entry);
            return this;
        }

        public Builder addLanguage(String name, LanguageDefinition definition) {
            this.language.put(name, definition);
            return this;
        }

        public PackMcMeta build() {
            return new PackMcMeta(this.metadata,
                    this.filter.isEmpty() ? Optional.empty() : Optional.of(new ResourceFilterSection(this.filter)),
                    this.overlay.isEmpty() ? Optional.empty() : Optional.of(new OverlayMetadataSection(this.overlay)),
                    this.language.isEmpty() ? Optional.empty() : Optional.of(new LanguageResourceMetadata(this.language))
            );
        }

        public PackMetadataSection metadata() {
            return this.metadata;
        }

        public List<OverlayMetadataSection.OverlayEntry> overlays() {
            return this.overlay;
        }
    }
}
