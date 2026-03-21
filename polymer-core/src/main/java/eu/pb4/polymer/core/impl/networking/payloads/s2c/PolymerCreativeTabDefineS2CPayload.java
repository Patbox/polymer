package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PolymerCreativeTabDefineS2CPayload(Identifier groupId, Component name, ItemStack icon) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerCreativeTabDefineS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_CREATIVE_TAB_DEFINE);
    public static final StreamCodec<ContextByteBuf, PolymerCreativeTabDefineS2CPayload> CODEC = StreamCodec.ofMember(PolymerCreativeTabDefineS2CPayload::write, PolymerCreativeTabDefineS2CPayload::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(this.groupId);

        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, name);
        ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, icon);
    }

    public static PolymerCreativeTabDefineS2CPayload read(FriendlyByteBuf buf) {
        return new PolymerCreativeTabDefineS2CPayload(buf.readIdentifier(), ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf), ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
