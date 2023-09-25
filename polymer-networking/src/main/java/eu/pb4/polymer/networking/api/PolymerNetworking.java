package eu.pb4.polymer.networking.api;

import com.google.common.collect.ImmutableMap;
import eu.pb4.polymer.networking.api.payload.ContextPayload;
import eu.pb4.polymer.networking.api.payload.PayloadDecoder;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ExtClientConnection;
import eu.pb4.polymer.networking.impl.ServerPackets;
import eu.pb4.polymer.networking.mixin.CustomPayloadC2SPacketAccessor;
import eu.pb4.polymer.networking.mixin.CustomPayloadS2CPacketAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PolymerNetworking {
    private PolymerNetworking() {}

    @Nullable
    public static <T extends NbtElement> T getMetadata(ClientConnection handler, Identifier identifier, NbtType<T> type) {
        var x = ExtClientConnection.of(handler).polymerNet$getMetadataMap().get(identifier);
        if (x != null && x.getNbtType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    public static <T extends ContextPayload> void registerS2CPayload(Identifier identifier, ContextPayload.Decoder<T> decoder) {
        registerS2CPayload(identifier, 0, decoder);
    }

    public static <T extends ContextPayload> void registerS2CPayload(Identifier identifier, int version, ContextPayload.Decoder<T> decoder) {
        registerS2CPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends ContextPayload> void registerS2CPayload(Identifier identifier, IntList versions, ContextPayload.Decoder<T> decoder) {
        registerS2CPayload(identifier, versions, (PayloadDecoder<T>) decoder);
    }

    public static <T extends VersionedPayload> void registerS2CPayload(Identifier identifier, VersionedPayload.Decoder<T> decoder) {
        registerS2CPayload(identifier, 0, decoder);
    }

    public static <T extends VersionedPayload> void registerS2CPayload(Identifier identifier, int version, VersionedPayload.Decoder<T> decoder) {
        registerS2CPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends VersionedPayload> void registerS2CPayload(Identifier identifier, IntList versions, VersionedPayload.Decoder<T> decoder) {
        registerS2CPayload(identifier, versions, (PayloadDecoder<T>) decoder);
    }

    public static <T extends CustomPayload> void registerS2CPayload(Identifier identifier, PayloadDecoder<T> decoder) {
        registerS2CPayload(identifier, 0, decoder);
    }

    public static <T extends CustomPayload> void registerS2CPayload(Identifier identifier, int version, PayloadDecoder<T> decoder) {
        registerS2CPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends CustomPayload> void registerS2CPayload(Identifier identifier, IntList versions, PayloadDecoder<T> decoder) {
        var builder = ImmutableMap.<Identifier, PacketByteBuf.PacketReader<? extends CustomPayload>>builder().putAll(CustomPayloadS2CPacketAccessor.getID_TO_READER());
        builder.put(identifier, decoder.forPacket(identifier));
        CustomPayloadS2CPacketAccessor.setID_TO_READER(builder.build());
        ServerPackets.register(identifier, versions.toIntArray());
    }

    public static <T extends ContextPayload> void registerC2SPayload(Identifier identifier, ContextPayload.Decoder<T> decoder) {
        registerC2SPayload(identifier, 0, decoder);
    }

    public static <T extends ContextPayload> void registerC2SPayload(Identifier identifier, int version, ContextPayload.Decoder<T> decoder) {
        registerC2SPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends ContextPayload> void registerC2SPayload(Identifier identifier, IntList versions, ContextPayload.Decoder<T> decoder) {
        registerC2SPayload(identifier, versions, (PayloadDecoder<T>) decoder);
    }

    public static <T extends VersionedPayload> void registerC2SPayload(Identifier identifier, VersionedPayload.Decoder<T> decoder) {
        registerC2SPayload(identifier, 0, decoder);
    }

    public static <T extends VersionedPayload> void registerC2SPayload(Identifier identifier, int version, VersionedPayload.Decoder<T> decoder) {
        registerC2SPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends VersionedPayload> void registerC2SPayload(Identifier identifier, IntList versions, VersionedPayload.Decoder<T> decoder) {
        registerC2SPayload(identifier, versions, (PayloadDecoder<T>) decoder);
    }

    public static <T extends CustomPayload> void registerC2SPayload(Identifier identifier, PayloadDecoder<T> decoder) {
        registerC2SPayload(identifier, 0, decoder);
    }

    public static <T extends CustomPayload> void registerC2SPayload(Identifier identifier, int version, PayloadDecoder<T> decoder) {
        registerC2SPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends CustomPayload> void registerC2SPayload(Identifier identifier, IntList versions, PayloadDecoder<T> decoder) {
        var builder = ImmutableMap.<Identifier, PacketByteBuf.PacketReader<? extends CustomPayload>>builder().putAll(CustomPayloadC2SPacketAccessor.getID_TO_READER());
        builder.put(identifier, decoder.forPacket(identifier));
        CustomPayloadC2SPacketAccessor.setID_TO_READER(builder.build());
        ClientPackets.register(identifier, versions.toIntArray());
    }

    public static <T extends ContextPayload> void registerCommonPayload(Identifier identifier, ContextPayload.Decoder<T> decoder) {
        registerCommonPayload(identifier, 0, decoder);
    }

    public static <T extends ContextPayload> void registerCommonPayload(Identifier identifier, int version, ContextPayload.Decoder<T> decoder) {
        registerCommonPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends ContextPayload> void registerCommonPayload(Identifier identifier, IntList versions, ContextPayload.Decoder<T> decoder) {
       registerCommonPayload(identifier, versions, (PayloadDecoder<T>) decoder);
    }

    public static <T extends VersionedPayload> void registerCommonPayload(Identifier identifier, VersionedPayload.Decoder<T> decoder) {
        registerCommonPayload(identifier, 0, decoder);
    }

    public static <T extends VersionedPayload> void registerCommonPayload(Identifier identifier, int version, VersionedPayload.Decoder<T> decoder) {
        registerCommonPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends VersionedPayload> void registerCommonPayload(Identifier identifier, IntList versions, VersionedPayload.Decoder<T> decoder) {
        registerCommonPayload(identifier, versions, (PayloadDecoder<T>) decoder);
    }

    public static <T extends CustomPayload> void registerCommonPayload(Identifier identifier, PayloadDecoder<T> decoder) {
        registerCommonPayload(identifier, 0, decoder);
    }

    public static <T extends CustomPayload> void registerCommonPayload(Identifier identifier, int version, PayloadDecoder<T> decoder) {
        registerCommonPayload(identifier, IntList.of(version), decoder);
    }

    public static <T extends CustomPayload> void registerCommonPayload(Identifier identifier, IntList versions, PayloadDecoder<T> decoder) {
        registerS2CPayload(identifier, versions, decoder);
        registerC2SPayload(identifier, versions, decoder);
    }
}
