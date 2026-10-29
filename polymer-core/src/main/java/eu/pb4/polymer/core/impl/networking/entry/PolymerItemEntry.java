package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public record PolymerItemEntry(int numId, Identifier identifier, ItemStack representation) {
    public static final StreamCodec<ContextByteBuf, PolymerItemEntry> CODEC = StreamCodec.ofMember(PolymerItemEntry::write, PolymerItemEntry::read);

    public static PolymerItemEntry of(Item item, ServerGamePacketListenerImpl handler, int version) {
        return new PolymerItemEntry(Item.getId(item), BuiltInRegistries.ITEM.getKey(item), item.getDefaultInstance());
    }

    public static PolymerItemEntry read(ContextByteBuf buf) {
        var bufId = buf.readVarInt();
        var id = buf.readIdentifier();
        try {
            return new PolymerItemEntry(bufId, id, readSafeItemStack(buf, () -> BuiltInRegistries.ITEM.getValue(id)));
        } catch (Throwable e) {
            PolymerImpl.LOGGER.error("Failed to parse '{}' item! Invalid stack data!", id);
            throw e;
        }
    }

    public void write(ContextByteBuf buf) {
        buf.writeVarInt(this.numId);
        buf.writeIdentifier(this.identifier);

        writeSafeItemStack(buf, this.representation);
    }

    public static void writeSafeItemStack(ContextByteBuf buf, ItemStack originalStack) {
        var writtenStack = PolymerItemUtils.getPolymerItemStack(originalStack, buf.packetContext(), buf.registryAccess());
        var isSafeWrite = !PolymerItemUtils.isPolymerServerItem(writtenStack, buf.packetContext());

        if (buf.version() >= 16) {
            buf.writeBoolean(isSafeWrite);
            if (isSafeWrite) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, originalStack);
            }
        } else {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, isSafeWrite ? originalStack : Items.STONE.getDefaultInstance());
        }
    }

    public static ItemStack readSafeItemStack(ContextByteBuf buf, Supplier<ItemLike> supplier) {
        if (buf.version() < 16 || buf.readBoolean()) {
            return ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        } else {
            return Objects.requireNonNullElse(supplier.get().asItem(), Items.AIR).getDefaultInstance();
        }
    }
}
