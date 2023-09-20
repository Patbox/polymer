package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.SingleplayerSerialization;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupDefineS2CPayload(Identifier groupId, Text name, ItemStack icon) implements VersionedPayload, SingleplayerSerialization {
    public static final Identifier ID = S2CPackets.SYNC_ITEM_GROUP_DEFINE;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeIdentifier(this.groupId);
        buf.writeText(name);
        buf.writeItemStack(icon);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerItemGroupDefineS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerItemGroupDefineS2CPayload(buf.readIdentifier(), buf.readText(), buf.readItemStack());
    }
}
