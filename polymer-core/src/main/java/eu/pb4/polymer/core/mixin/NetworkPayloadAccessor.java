package eu.pb4.polymer.core.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagNetworkSerialization;

@Mixin(TagNetworkSerialization.NetworkPayload.class)
public interface NetworkPayloadAccessor {
    @Accessor
    Map<Identifier, IntList> getTags();

    @Invoker("<init>")
    static TagNetworkSerialization.NetworkPayload createSerialized(Map<Identifier, IntList> contents) {
        throw new UnsupportedOperationException();
    }
}
