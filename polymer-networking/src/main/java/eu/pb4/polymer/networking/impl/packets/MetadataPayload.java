package eu.pb4.polymer.networking.impl.packets;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MetadataPayload(Map<Identifier, Tag> map) implements CustomPacketPayload {
    public static final Type<MetadataPayload> ID = PolymerNetworking.id("polymer", "metadata");
    public static final StreamCodec<ContextByteBuf, MetadataPayload> CODEC = ByteBufCodecs.map((IntFunction<Map<Identifier, Tag>>) HashMap::new,
                    Identifier.STREAM_CODEC, ByteBufCodecs.TAG).map(MetadataPayload::new, MetadataPayload::map).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
