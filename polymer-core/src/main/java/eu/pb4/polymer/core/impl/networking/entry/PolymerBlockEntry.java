package eu.pb4.polymer.core.impl.networking.entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public record PolymerBlockEntry(Identifier identifier, int numId, Text text, BlockState visual) implements WritableEntry {
    public void write(PacketByteBuf buf, int version) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(numId);
        //buf.writeText(text);
        //buf.writeRegistryValue(Block.STATE_IDS, visual);
    }

    public static PolymerBlockEntry of(Block block) {
        return new PolymerBlockEntry(Registries.BLOCK.getId(block), Registries.BLOCK.getRawId(block), block.getName(), block.getDefaultState());
    }

    public static PolymerBlockEntry read(PacketByteBuf buf, int version) {
        return null;
        //return new PolymerBlockEntry(buf.readIdentifier(), buf.readVarInt(), buf.readText(), Block.getStateFromRawId(buf.readVarInt()));
    }
}
