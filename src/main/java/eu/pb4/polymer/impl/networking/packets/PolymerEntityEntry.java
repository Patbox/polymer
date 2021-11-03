package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerEntityEntry(Identifier identifier, Text name) implements BufferWritable {
    public void write(PacketByteBuf buf, ServerPlayNetworkHandler handler) {
        buf.writeIdentifier(identifier);
        buf.writeText(ServerTranslationUtils.parseFor(handler, name));
    }

    public static PolymerEntityEntry of(EntityType<?> entityType) {
        return new PolymerEntityEntry(
                Registry.ENTITY_TYPE.getId(entityType),
                entityType.getName()
        );
    }

    public static PolymerEntityEntry read(PacketByteBuf buf) {
        return new PolymerEntityEntry(buf.readIdentifier(), buf.readText());
    }
}
