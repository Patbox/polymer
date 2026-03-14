package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.mixin.NetworkPayloadAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

@Mixin(ClientboundUpdateTagsPacket.class)
public class ClientboundUpdateTagsPacketMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeMap(Ljava/util/Map;Lnet/minecraft/network/codec/StreamEncoder;Lnet/minecraft/network/codec/StreamEncoder;)V"))
    private Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> polymer$skipEntries(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> groups) {
        var regMap = new HashMap<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>();
        var player = PacketContext.get();
        for (var regEntry : groups.entrySet()) {
            if (PolymerUtils.isServerOnlyRegistry(regEntry.getKey())) {
                continue;
            }
            //noinspection rawtypes,unchecked
            var reg = BuiltInRegistries.REGISTRY.getValue((ResourceKey) regEntry.getKey());

            if (reg != null) {
                var map = new HashMap<Identifier, IntList>();

                for (var entry : ((NetworkPayloadAccessor) (Object) regEntry.getValue()).getTags().entrySet()) {
                    var list = new IntArrayList(entry.getValue().size());

                    for (int i : entry.getValue()) {
                        //noinspection unchecked
                        if (PolymerSyncedObject.canSyncRawToClient(reg, reg.byId(i), player)) {
                            list.add(i);
                        }
                    }
                    map.put(entry.getKey(), list);
                }

                regMap.put(regEntry.getKey(), NetworkPayloadAccessor.createSerialized(map));
            } else {
                // Dynamic registry, client *should* understand it
                regMap.put(regEntry.getKey(), regEntry.getValue());
            }
        }
        return regMap;
    }
}