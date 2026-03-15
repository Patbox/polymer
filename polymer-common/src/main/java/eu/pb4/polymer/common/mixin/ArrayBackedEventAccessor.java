package eu.pb4.polymer.common.mixin;

import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@org.spongepowered.asm.mixin.Mixin(targets = "net.fabricmc.fabric.impl.base.event.ArrayBackedEvent")
public interface ArrayBackedEventAccessor {
    @Accessor
    <T> Function<T[], T> getInvokerFactory();
    @Accessor
    <T> T[] getHandlers();
}
