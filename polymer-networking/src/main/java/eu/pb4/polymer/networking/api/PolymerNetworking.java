package eu.pb4.polymer.networking.api;

import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ExtConnection;
import eu.pb4.polymer.networking.impl.ServerPackets;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.Connection;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class PolymerNetworking {
    private PolymerNetworking() {}

    @Nullable
    public static <T extends Tag> T getMetadata(Connection handler, Identifier identifier, TagType<T> type) {
        var x = ExtConnection.of(handler).polymerNet$getMetadataMap().get(identifier);
        if (x != null && x.getType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    public static <T extends CustomPacketPayload> void registerClientboundSimple(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundSimple(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundSimple(Identifier identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundSimple(Identifier identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerClientbound(identifier, versions, ContextByteBuf.simple(codec));
    }

    public static <T extends CustomPacketPayload> void registerClientboundVersioned(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundVersioned(Identifier identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundVersioned(Identifier identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerClientbound(identifier, versions, ContextByteBuf.versioned(identifier, codec));
    }

    public static <T extends CustomPacketPayload> void registerServerboundSimple(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundSimple(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundSimple(Identifier identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundSimple(Identifier identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerServerbound(identifier, versions, ContextByteBuf.simple(codec));
    }

    public static <T extends CustomPacketPayload> void registerServerboundVersioned(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundVersioned(Identifier identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundVersioned(Identifier identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerServerbound(identifier, versions, ContextByteBuf.versioned(identifier, codec));
    }

    public static <T extends CustomPacketPayload> void registerCommonSimple(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonSimple(Identifier identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonSimple(Identifier identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerCommon(identifier, versions, ContextByteBuf.simple(codec));
    }

    public static <T extends CustomPacketPayload> void registerCommonVersioned(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonVersioned(Identifier identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonVersioned(Identifier identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerCommon(identifier, versions, ContextByteBuf.versioned(identifier, codec));
    }

    public static <T extends CustomPacketPayload> void registerClientboundSimple(CustomPacketPayload.Type<T> identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundSimple(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundSimple(CustomPacketPayload.Type<T> identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundSimple(CustomPacketPayload.Type<T> identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundSimple(identifier.id(), versions, codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundVersioned(CustomPacketPayload.Type<T> identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundVersioned(CustomPacketPayload.Type<T> identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerClientboundVersioned(CustomPacketPayload.Type<T> identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerClientboundVersioned(identifier.id(), versions, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundSimple(CustomPacketPayload.Type<T> identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundSimple(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundSimple(CustomPacketPayload.Type<T> identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundSimple(CustomPacketPayload.Type<T> identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundSimple(identifier.id(), versions, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundVersioned(CustomPacketPayload.Type<T> identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundVersioned(CustomPacketPayload.Type<T> identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerServerboundVersioned(CustomPacketPayload.Type<T> identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerServerboundVersioned(identifier.id(), versions, codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonSimple(CustomPacketPayload.Type<T> identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonSimple(CustomPacketPayload.Type<T> identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonSimple(CustomPacketPayload.Type<T> identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonSimple(identifier.id(), versions, codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonVersioned(CustomPacketPayload.Type<T> identifier, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, 0, codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonVersioned(CustomPacketPayload.Type<T> identifier, int version, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier, IntList.of(version), codec);
    }

    public static <T extends CustomPacketPayload> void registerCommonVersioned(CustomPacketPayload.Type<T> identifier, IntList versions, StreamCodec<ContextByteBuf, T> codec) {
        registerCommonVersioned(identifier.id(), versions, codec);
    }



    private static <T extends CustomPacketPayload> void registerCommon(Identifier identifier, IntList versions, StreamCodec<ByteBuf, T> codec) {
        registerClientbound(identifier, versions, codec);
        registerServerbound(identifier, versions, codec);
    }

    public static int getSupportedVersion(Connection connection, Identifier identifier) {
        return connection != null ? ExtConnection.of(connection).polymerNet$getSupportedVersion(identifier) : -1;
    }

    private static <T extends CustomPacketPayload> void registerClientbound(Identifier identifier, IntList versions, StreamCodec<ByteBuf, T> codec) {
        ServerPackets.register(identifier, codec, versions.toIntArray());
    }
    private static <T extends CustomPacketPayload> void registerServerbound(Identifier identifier, IntList versions, StreamCodec<ByteBuf, T> codec) {
        ClientPackets.register(identifier, codec, versions.toIntArray());
    }
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> id(String id) {
        return new CustomPacketPayload.Type<>(Identifier.parse(id));
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> id(String namespace, String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(namespace, path));
    }
}
