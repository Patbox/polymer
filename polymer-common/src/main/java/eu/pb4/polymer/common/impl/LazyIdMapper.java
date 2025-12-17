package eu.pb4.polymer.common.impl;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.util.ExtraCodecs;

public final class LazyIdMapper<A, B> extends ExtraCodecs.LateBoundIdMapper<A, B> {
    private Consumer<ExtraCodecs.LateBoundIdMapper<A,B>> initializer;

    public LazyIdMapper(Consumer<ExtraCodecs.LateBoundIdMapper<A, B>> initializer) {
        this.initializer = initializer;
    }

    @Override
    public Codec<B> codec(Codec<A> idCodec) {
        if (this.initializer != null) {
            var init = this.initializer;
            this.initializer = null;
            init.accept(this);
        }
        return super.codec(idCodec);
    }

    @Override
    public ExtraCodecs.LateBoundIdMapper<A, B> put(A id, B value) {
        if (this.initializer != null) {
            var init = this.initializer;
            this.initializer = null;
            init.accept(this);
        }
        return super.put(id, value);
    }
}
