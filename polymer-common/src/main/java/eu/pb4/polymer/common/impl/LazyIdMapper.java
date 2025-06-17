package eu.pb4.polymer.common.impl;

import com.mojang.serialization.Codec;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Consumer;

public final class LazyIdMapper<A, B> extends Codecs.IdMapper<A, B> {
    private Consumer<Codecs.IdMapper<A,B>> initializer;

    public LazyIdMapper(Consumer<Codecs.IdMapper<A, B>> initializer) {
        this.initializer = initializer;
    }

    @Override
    public Codec<B> getCodec(Codec<A> idCodec) {
        if (this.initializer != null) {
            var init = this.initializer;
            this.initializer = null;
            init.accept(this);
        }
        return super.getCodec(idCodec);
    }

    @Override
    public Codecs.IdMapper<A, B> put(A id, B value) {
        if (this.initializer != null) {
            var init = this.initializer;
            this.initializer = null;
            init.accept(this);
        }
        return super.put(id, value);
    }
}
