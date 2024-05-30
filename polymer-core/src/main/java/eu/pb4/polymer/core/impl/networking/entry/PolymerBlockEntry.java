package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public record PolymerBlockEntry(Identifier identifier, int numId, Text text, BlockState visual, ItemStack visualStack) {
    private static final PacketCodec<ByteBuf, BlockState> STATE = PacketCodecs.entryOf(Block.STATE_IDS);
    public static final PacketCodec<ContextByteBuf, PolymerBlockEntry> CODEC = PacketCodec.of(PolymerBlockEntry::write, PolymerBlockEntry::read);
    public void write(ContextByteBuf buf) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(numId);
        TextCodecs.PACKET_CODEC.encode(buf, text);
        STATE.encode(buf, visual);
        if (buf.version() >= 7) {
            ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, this.visualStack);
        }
    }

    public static PolymerBlockEntry of(Block block) {
        return new PolymerBlockEntry(Registries.BLOCK.getId(block), Registries.BLOCK.getRawId(block),
                block.getName(), block.getDefaultState(), block.asItem() != null ? block.asItem().getDefaultStack() : ItemStack.EMPTY);
    }

    public static PolymerBlockEntry read(ContextByteBuf buf) {
        var id = buf.readIdentifier();
        var numId = buf.readVarInt();
        var name = TextCodecs.PACKET_CODEC.decode(buf);
        var visual = STATE.decode(buf);
        var visualStack = buf.version() >= 7 ? ItemStack.OPTIONAL_PACKET_CODEC.decode(buf)
                : (visual.getBlock().asItem() != null ? visual.getBlock().asItem().getDefaultStack() : ItemStack.EMPTY);
        return new PolymerBlockEntry(id, numId, name, visual, visualStack);
    }
}
