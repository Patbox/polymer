package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.TriState;

public record FontProviderFilter(TriState uniform, TriState jp) {
    public static final FontProviderFilter DEFAULT = new FontProviderFilter(TriState.DEFAULT, TriState.DEFAULT);
    public static final FontProviderFilter UNIFORM = new FontProviderFilter(TriState.TRUE, TriState.DEFAULT);
    public static final FontProviderFilter JP = new FontProviderFilter(TriState.DEFAULT, TriState.TRUE);
    public static final FontProviderFilter UNIFORM_JP = new FontProviderFilter(TriState.TRUE, TriState.TRUE);

    public static final Codec<FontProviderFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.xmap(x -> x ? TriState.TRUE : TriState.FALSE, x -> x.asBoolean(true)).optionalFieldOf("uniform", TriState.DEFAULT).forGetter(FontProviderFilter::uniform),
            Codec.BOOL.xmap(x -> x ? TriState.TRUE : TriState.FALSE, x -> x.asBoolean(true)).optionalFieldOf("jp", TriState.DEFAULT).forGetter(FontProviderFilter::jp)
    ).apply(instance, FontProviderFilter::new));
}
