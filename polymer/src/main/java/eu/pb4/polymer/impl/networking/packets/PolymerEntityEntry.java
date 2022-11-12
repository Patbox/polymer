package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerEntityEntry(Identifier identifier, int rawId, Text name) implements BufferWritable {
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeIdentifier(identifier);
        if (version > 0) {
            buf.writeVarInt(this.rawId);
        }
        buf.writeText(ServerTranslationUtils.parseFor(handler, name));
    }

    public static PolymerEntityEntry of(EntityType<?> entityType) {
        return new PolymerEntityEntry(
                Registries.ENTITY_TYPE.getId(entityType),
                Registries.ENTITY_TYPE.getRawId(entityType),
                entityType.getName()
        );
    }

    public static PolymerEntityEntry read(PacketByteBuf buf, int version) {
        if (version == 1) {
            return new PolymerEntityEntry(buf.readIdentifier(), buf.readVarInt(), buf.readText());
        }
        return null;
    }
}
