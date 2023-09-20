package eu.pb4.polymer.networking.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CustomPayloadS2CPacket.class)
public interface CustomPayloadS2CPacketAccessor {
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
