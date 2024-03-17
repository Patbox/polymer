package eu.pb4.polymer.networking.api;

import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ExtClientConnection;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import eu.pb4.polymer.networking.impl.ServerPackets;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
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

    public static <T extends CustomPayload> void registerS2CSimple(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CSimple(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerS2CSimple(Identifier identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerS2CSimple(Identifier identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerS2C(identifier, versions, ContextByteBuf.simple(codec));
    }

    public static <T extends CustomPayload> void registerS2CVersioned(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerS2CVersioned(Identifier identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerS2CVersioned(Identifier identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerS2C(identifier, versions, ContextByteBuf.versioned(identifier, codec));
    }

    public static <T extends CustomPayload> void registerC2SSimple(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SSimple(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerC2SSimple(Identifier identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerC2SSimple(Identifier identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerC2S(identifier, versions, ContextByteBuf.simple(codec));
    }

    public static <T extends CustomPayload> void registerC2SVersioned(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerC2SVersioned(Identifier identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerC2SVersioned(Identifier identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerC2S(identifier, versions, ContextByteBuf.versioned(identifier, codec));
    }

    public static <T extends CustomPayload> void registerCommonSimple(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerCommonSimple(Identifier identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerCommonSimple(Identifier identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerCommon(identifier, versions, ContextByteBuf.simple(codec));
    }

    public static <T extends CustomPayload> void registerCommonVersioned(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerCommonVersioned(Identifier identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerCommonVersioned(Identifier identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerCommon(identifier, versions, ContextByteBuf.versioned(identifier, codec));
    }

    public static <T extends CustomPayload> void registerS2CSimple(CustomPayload.Id<T> identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CSimple(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerS2CSimple(CustomPayload.Id<T> identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerS2CSimple(CustomPayload.Id<T> identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CSimple(identifier.id(), versions, codec);
    }

    public static <T extends CustomPayload> void registerS2CVersioned(CustomPayload.Id<T> identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerS2CVersioned(CustomPayload.Id<T> identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerS2CVersioned(CustomPayload.Id<T> identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerS2CVersioned(identifier.id(), versions, codec);
    }

    public static <T extends CustomPayload> void registerC2SSimple(CustomPayload.Id<T> identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SSimple(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerC2SSimple(CustomPayload.Id<T> identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerC2SSimple(CustomPayload.Id<T> identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SSimple(identifier.id(), versions, codec);
    }

    public static <T extends CustomPayload> void registerC2SVersioned(CustomPayload.Id<T> identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerC2SVersioned(CustomPayload.Id<T> identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerC2SVersioned(CustomPayload.Id<T> identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerC2SVersioned(identifier.id(), versions, codec);
    }

    public static <T extends CustomPayload> void registerCommonSimple(CustomPayload.Id<T> identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerCommonSimple(CustomPayload.Id<T> identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerCommonSimple(CustomPayload.Id<T> identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier.id(), versions, codec);
    }

    public static <T extends CustomPayload> void registerCommonVersioned(CustomPayload.Id<T> identifier, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPayload> void registerCommonVersioned(CustomPayload.Id<T> identifier, int version, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPayload> void registerCommonVersioned(CustomPayload.Id<T> identifier, IntList versions, PacketCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier.id(), versions, codec);
    }



    private static <T extends CustomPayload> void registerCommon(Identifier identifier, IntList versions, PacketCodec<ByteBuf, T> codec) {
        registerS2C(identifier, versions, codec);
        registerC2S(identifier, versions, codec);
    }

    public static int getSupportedVersion(ClientConnection connection, Identifier identifier) {
        return connection != null ? ExtClientConnection.of(connection).polymerNet$getSupportedVersion(identifier) : -1;
    }

    private static <T extends CustomPayload> void registerS2C(Identifier identifier, IntList versions, PacketCodec<ByteBuf, T> codec) {
        ServerPackets.register(identifier, codec, versions.toIntArray());
    }
    private static <T extends CustomPayload> void registerC2S(Identifier identifier, IntList versions, PacketCodec<ByteBuf, T> codec) {
        ClientPackets.register(identifier, codec, versions.toIntArray());
    }
    public static <T extends CustomPayload> CustomPayload.Id<T> id(String id) {
        return new CustomPayload.Id<>(new Identifier(id));
    }

    public static <T extends CustomPayload> CustomPayload.Id<T> id(String namespace, String path) {
        return new CustomPayload.Id<>(new Identifier(namespace, path));
    }
}
