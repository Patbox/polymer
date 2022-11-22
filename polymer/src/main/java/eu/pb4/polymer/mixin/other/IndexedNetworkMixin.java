package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.impl.interfaces.IndexedNetwork;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.IntFunction;

@Mixin({ SimpleRegistry.class, IdList.class })
public abstract class IndexedNetworkMixin<T> implements IndexedNetwork<T> {
    private IntFunction<T> polymer$decoder = this::get;

    @Override
    public IntFunction<T> polymer$getDecoder() {
        return this.polymer$decoder;
    }

    @Override
    public void polymer$setDecoder(IntFunction<T> decoder) {
        this.polymer$decoder = decoder;
    }
}
