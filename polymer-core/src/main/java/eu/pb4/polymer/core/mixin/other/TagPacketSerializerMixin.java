package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashMap;
import java.util.Map;

@Mixin(TagPacketSerializer.Serialized.class)
public class TagPacketSerializerMixin {
    @ModifyArg(method = "writeBuf", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeMap(Ljava/util/Map;Lnet/minecraft/network/PacketByteBuf$PacketWriter;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"))
    private Map<Identifier, IntList> polymer$skipEntries(Map<Identifier, IntList> value) {
        var map = new HashMap<Identifier, IntList>();
        var player = PolymerUtils.getPlayerContext();
        for (var entry : value.entrySet()) {
            var reg = Registries.REGISTRIES.get(entry.getKey());

            if (reg != null) {
                var list = new IntArrayList(entry.getValue().size());

                for (int i : entry.getValue()) {
                    if (PolymerSyncedObject.canSyncRawToClient(reg.get(i), player)) {
                        list.add(i);
                    }
                }

                map.put(entry.getKey(), list);
            } else {
                // Dynamic registry, client should understand it
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
}