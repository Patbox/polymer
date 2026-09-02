package eu.pb4.polymer.core.mixin.tag;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import eu.pb4.polymer.core.mixin.NetworkPayloadAccessor;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientboundUpdateTagsPacket.class)
public class ClientboundUpdateTagsPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/ByteBufCodecs;map(Ljava/util/function/IntFunction;Lnet/minecraft/network/codec/StreamCodec;Lnet/minecraft/network/codec/StreamCodec;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<ByteBuf, Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>> polymer$skipEntries(StreamCodec<ByteBuf, Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>> original) {
        return TransformingPacketCodec.encodeOnly(original, (_, groups) -> {
            var regMap = new HashMap<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>();
            var player = PacketContext.get();
            for (var regEntry : groups.entrySet()) {
                if (PolymerUtils.isServerOnlyRegistry(regEntry.getKey())) {
                    continue;
                }
                //noinspection rawtypes,unchecked
                var reg = BuiltInRegistries.REGISTRY.getValue((ResourceKey) regEntry.getKey());

                if (reg != null) {
                    //var replacers = PolymerTagHacks.REPLACERS.getOrDefault(regEntry.getKey().identifier(), Map.of());
                    //var fakes = PolymerTagHacks.FAKE_ENTRIES.getOrDefault(regEntry.getKey().identifier(), Map.of());
                    var map = new HashMap<Identifier, IntList>();

                    for (var entry : ((NetworkPayloadAccessor) (Object) regEntry.getValue()).getTags().entrySet()) {
                        var list = new IntArrayList(entry.getValue().size());

                        for (int i : entry.getValue()) {
                            //noinspection unchecked
                            if (PolymerSyncedObject.canSyncRawToClient(reg, reg.byId(i), player)) {
                                list.add(i);
                            }
                        }

                    /*var replacer = replacers.get(entry.getKey());
                    if (replacer != null) {
                        map.put(replacer.target(), list);
                        list = replacer.keepEntries() ? new IntArrayList(list) : new IntArrayList();
                    }

                    for (var f : fakes.getOrDefault(entry.getKey(), List.of())) {
                        //noinspection unchecked
                        list.add(reg.getId(f));
                    }*/

                        map.put(entry.getKey(), list);
                    }

                    regMap.put(regEntry.getKey(), NetworkPayloadAccessor.createSerialized(map));
                } else {
                    // Dynamic registry, client *should* understand it
                    regMap.put(regEntry.getKey(), regEntry.getValue());
                }
            }
            return regMap;
        });
    }
}