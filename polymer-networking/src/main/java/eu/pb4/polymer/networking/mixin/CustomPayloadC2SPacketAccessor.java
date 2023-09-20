package eu.pb4.polymer.networking.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CustomPayloadC2SPacket.class)
public interface CustomPayloadC2SPacketAccessor {
    @Accessor
    static Map<Identifier, PacketByteBuf.PacketReader<? extends CustomPayload>> getID_TO_READER() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    static void setID_TO_READER(Map<Identifier, PacketByteBuf.PacketReader<? extends CustomPayload>> ID_TO_READER) {
        throw new UnsupportedOperationException();
    }
}
