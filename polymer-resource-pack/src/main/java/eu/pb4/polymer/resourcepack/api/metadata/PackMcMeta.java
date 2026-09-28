package eu.pb4.polymer.resourcepack.api.metadata;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.mixin.accessors.ResourceFilterSectionAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.util.IdentifierPattern;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.Nullable;

import java.util.*;

public final class PackMcMeta {
    public static final Codec<PackMcMeta> CODEC = SortedMapCodec.of(Codec.STRING, Codec.PASSTHROUGH).xmap(PackMcMeta::new, p -> p.values);

    public static final Codec<PackMcMeta> VANILLA_CODEC = RecordCodecBuilder.create(instaince -> instaince.group(
            PackMetadataSection.CLIENT_TYPE.codec().fieldOf("pack").forGetter(PackMcMeta::pack),
            ResourceFilterSectionAccessor.getCODEC().optionalFieldOf("filter").forGetter(PackMcMeta::filter),
            OverlayMetadataSection.CLIENT_TYPE.codec().optionalFieldOf("overlays").forGetter(PackMcMeta::overlays),
            LanguageResourceMetadata.CODEC.optionalFieldOf("language").forGetter(PackMcMeta::language)
    ).apply(instaince, PackMcMeta::new));

    private final Map<String, Dynamic<?>> values;

    public PackMcMeta(PackMetadataSection pack, MetadataSectionType.WithValue<?>... metadata) {
        this.values = createValuesMap(pack, metadata);
    }

    public PackMcMeta(PackMetadataSection pack, Collection<MetadataSectionType.WithValue<?>> metadata) {
        this.values = createValuesMap(pack, metadata.toArray(MetadataSectionType.WithValue[]::new));
    }

    public PackMcMeta(PackMetadataSection pack, Optional<ResourceFilterSection> filter,
                      Optional<OverlayMetadataSection> overlays, Optional<LanguageResourceMetadata> language) {
        this.values = createValuesMap(pack,
                filter.map(ResourceFilterSection.TYPE::withValue).orElse(null),
                overlays.map(OverlayMetadataSection.CLIENT_TYPE::withValue).orElse(null),
                language.map(LanguageResourceMetadata.TYPE::withValue).orElse(null)
        );
    }

    private PackMcMeta(Map<String, Dynamic<?>> map) {
        this.values = map;

        if (!this.values.containsKey("pack")) {
            throw new IllegalArgumentException("pack is missing!");
        }

        PackMetadataSection.CLIENT_TYPE.codec().parse(this.values.get("pack")).getOrThrow();
    }

    public <T> Optional<T> get(MetadataSectionType<T> sectionType) {
        var value = this.values.get(sectionType.name());

        if (value == null) {
            return Optional.empty();
        }

        return sectionType.codec().parse(value).result();
    }

    public PackMetadataSection pack() {
        return get(PackMetadataSection.CLIENT_TYPE).orElseThrow();
    }

    public Optional<ResourceFilterSection> filter() {
        return get(ResourceFilterSection.TYPE);
    }

    public Optional<OverlayMetadataSection> overlays() {
        return get(OverlayMetadataSection.CLIENT_TYPE);
    }

    public Optional<LanguageResourceMetadata> language() {
        return get(LanguageResourceMetadata.TYPE);
    }

    public static PackMcMeta fromString(String string) {
        return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(string)).getOrThrow();
    }

    public String asString() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        PackMcMeta that = (PackMcMeta) object;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Dynamic<?>> createValuesMap(PackMetadataSection pack, MetadataSectionType.WithValue<?>... values) {
        var map = new HashMap<String, Dynamic<?>>();
        map.put(PackMetadataSection.CLIENT_TYPE.name(), new Dynamic<Object>((DynamicOps) JsonOps.INSTANCE, PackMetadataSection.CLIENT_TYPE.codec().encodeStart(JsonOps.INSTANCE, pack).getOrThrow()));

        for (var v : values) {
            if (v == null) {
                continue;
            }

            map.put(v.type().name(), new Dynamic<Object>((DynamicOps) JsonOps.INSTANCE, ((Codec) v.type().codec()).encodeStart(JsonOps.INSTANCE, v.value()).getOrThrow()));
        }

        return map;
    }

    public static class Builder {
        private PackMetadataSection metadata = new PackMetadataSection(
                Component.literal("Server Resource Pack"),
                new InclusiveRange<>(
                        SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES),
                        PolymerResourcePackImpl.IGNORE_PACK_VERSION ? new PackFormat(Integer.MAX_VALUE, Integer.MAX_VALUE) : new PackFormat(SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major(), Integer.MAX_VALUE)
                )
        );
        private final List<IdentifierPattern> filter = new ArrayList<>();
        private final List<OverlayMetadataSection.OverlayEntry> overlay = new ArrayList<>();
        private final Map<String, LanguageDefinition> language = new HashMap<>();
        private final Map<MetadataSectionType<Object>, Object> custom = new IdentityHashMap<>();

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

        public <T> Builder custom(MetadataSectionType<T> type, T value) {
            this.custom.put((MetadataSectionType<Object>) type, value);
            return this;
        }

        public PackMcMeta build() {
            var list = new ArrayList<MetadataSectionType.WithValue<?>>();

            if (!this.filter.isEmpty()) {
                list.add(ResourceFilterSection.TYPE.withValue(new ResourceFilterSection(this.filter)));
            }
            if (!this.overlay.isEmpty()) {
                list.add(OverlayMetadataSection.CLIENT_TYPE.withValue(new OverlayMetadataSection(this.overlay)));
            }
            if (!this.language.isEmpty()) {
                list.add(LanguageResourceMetadata.TYPE.withValue(new LanguageResourceMetadata(this.language)));
            }

            this.custom.forEach((key, value) -> {
                list.add(key.withValue(value));
            });

            return new PackMcMeta(this.metadata, list);
        }

        public PackMetadataSection metadata() {
            return this.metadata;
        }

        public List<OverlayMetadataSection.OverlayEntry> overlays() {
            return this.overlay;
        }

        @Nullable
        public <T> T getCustom(MetadataSectionType<T> type) {
            //noinspection unchecked
            return (T) this.custom.get(type);
        }
    }
}
