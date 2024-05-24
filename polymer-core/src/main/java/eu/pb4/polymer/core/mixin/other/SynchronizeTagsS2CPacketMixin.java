package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.mixin.SerializedAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashMap;
import java.util.Map;

@Mixin(SynchronizeTagsS2CPacket.class)
public class SynchronizeTagsS2CPacketMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeMap(Ljava/util/Map;Lnet/minecraft/network/codec/PacketEncoder;Lnet/minecraft/network/codec/PacketEncoder;)V"))
    private Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> polymer$skipEntries(Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> groups) {
        var regMap = new HashMap<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized>();
        var player = PolymerUtils.getPlayerContext();
        for (var regEntry : groups.entrySet()) {
            //noinspection rawtypes,unchecked
            var reg = Registries.REGISTRIES.get((RegistryKey) regEntry.getKey());

            if (reg != null) {
                var map = new HashMap<Identifier, IntList>();

                for (var entry : ((SerializedAccessor) (Object) regEntry.getValue()).getContents().entrySet()) {
                    var list = new IntArrayList(entry.getValue().size());

                    for (int i : entry.getValue()) {
                        if (PolymerSyncedObject.canSyncRawToClient(reg.get(i), player)) {
                            list.add(i);
                        }
                    }
                    map.put(entry.getKey(), list);
                }

                regMap.put(regEntry.getKey(), SerializedAccessor.createSerialized(map));
            } else {
                // Dynamic registry, client should understand it
                regMap.put(regEntry.getKey(), regEntry.getValue());
            }
        }
        return regMap;
    }
}