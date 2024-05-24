package eu.pb4.polymer.core.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TagPacketSerializer.Serialized.class)
public interface SerializedAccessor {
    @Accessor
    Map<Identifier, IntList> getContents();

    @Invoker("<init>")
    static TagPacketSerializer.Serialized createSerialized(Map<Identifier, IntList> contents) {
        throw new UnsupportedOperationException();
    }
}
