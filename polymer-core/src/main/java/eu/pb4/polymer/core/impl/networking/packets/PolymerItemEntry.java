package eu.pb4.polymer.core.impl.networking.packets;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.mixin.item.MiningToolItemAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public record PolymerItemEntry(
        int numId,
        Identifier identifier,
        ItemStack representation,
        int foodLevels,
        float saturation,
        Identifier miningTool,
        int miningLevel,
        int stackSize
) implements BufferWritable {
    public static final Identifier NOT_TOOL = PolymerImplUtils.id("not_tool");

    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        if (version >= 4) {
            buf.writeVarInt(this.numId);

            buf.writeIdentifier(this.identifier);
            PolymerImplUtils.writeStack(buf, PolymerImplUtils.convertStack(this.representation, handler.player));

            buf.writeVarInt(this.foodLevels);
            buf.writeFloat(this.saturation);
            buf.writeIdentifier(this.miningTool);
            buf.writeVarInt(this.miningLevel);

            buf.writeVarInt(this.stackSize);

        }
    }

    public static PolymerItemEntry of(Item item, ServerPlayNetworkHandler handler, int version) {
        var toolItem = item instanceof MiningToolItem x ? x : null;
        var food = item.getFoodComponent();

        return new PolymerItemEntry(
                Item.getRawId(item),
                Registries.ITEM.getId(item),
                item.getDefaultStack(),
                food != null ? food.getHunger() : 0,
                food != null ? food.getSaturationModifier() : 0,
                toolItem != null ? ((MiningToolItemAccessor) toolItem).getEffectiveBlocks().id() : NOT_TOOL,
                toolItem != null  ? toolItem.getMaterial().getMiningLevel() : 0,
                item.getMaxCount()
        );
    }

    public static PolymerItemEntry read(PacketByteBuf buf, int version) {
        return switch (version) {
            case 4 -> new PolymerItemEntry(buf.readVarInt(), buf.readIdentifier(), PolymerImplUtils.readStack(buf), buf.readVarInt(), buf.readFloat(), buf.readIdentifier(), buf.readVarInt(), buf.readVarInt());
            default -> null;
        };
    }
}
