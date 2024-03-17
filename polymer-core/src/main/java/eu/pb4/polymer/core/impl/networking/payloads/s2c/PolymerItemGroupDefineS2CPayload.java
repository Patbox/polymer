package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupDefineS2CPayload(Identifier groupId, Text name, ItemStack icon) implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupDefineS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_DEFINE);
    public static final PacketCodec<ContextByteBuf, PolymerItemGroupDefineS2CPayload> CODEC = PacketCodec.of(PolymerItemGroupDefineS2CPayload::write, PolymerItemGroupDefineS2CPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.groupId);

        TextCodecs.PACKET_CODEC.encode(buf, name);
        ItemStack.PACKET_CODEC.encode((RegistryByteBuf) buf, icon);
    }

    public static PolymerItemGroupDefineS2CPayload read(PacketByteBuf buf) {
        return new PolymerItemGroupDefineS2CPayload(buf.readIdentifier(), TextCodecs.PACKET_CODEC.decode(buf), ItemStack.PACKET_CODEC.decode((RegistryByteBuf) buf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
