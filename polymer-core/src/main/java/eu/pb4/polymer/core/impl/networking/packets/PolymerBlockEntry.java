package eu.pb4.polymer.core.impl.networking.packets;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public record PolymerBlockEntry(Identifier identifier, int numId, Text text, BlockState visual) implements BufferWritable {
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(numId);
        buf.writeText(ServerTranslationUtils.parseFor(handler, text));
        var player = PolymerUtils.getPlayerContext();
        buf.writeVarInt(Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(PolyMcUtils.toVanilla(this.visual, player), player)));
    }

    public static PolymerBlockEntry of(Block block) {
        return new PolymerBlockEntry(Registries.BLOCK.getId(block), Registries.BLOCK.getRawId(block), block.getName(), block.getDefaultState());
    }

    public static PolymerBlockEntry read(PacketByteBuf buf, int version) {
        if (version > -1) {
            return new PolymerBlockEntry(buf.readIdentifier(), buf.readVarInt(), buf.readText(), Block.getStateFromRawId(buf.readVarInt()));
        }

        return null;
    }
}
