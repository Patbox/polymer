package eu.pb4.polymer.core.impl.networking.entry;

import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerEntityEntry(Identifier identifier, int rawId, Text name) implements WritableEntry {
    public void write(PacketByteBuf buf, int version) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(this.rawId);
        //buf.writeText(name);
    }

    public static PolymerEntityEntry of(EntityType<?> entityType) {
        return new PolymerEntityEntry(
                Registries.ENTITY_TYPE.getId(entityType),
                Registries.ENTITY_TYPE.getRawId(entityType),
                entityType.getName()
        );
    }

    public static PolymerEntityEntry read(PacketByteBuf buf, int version) {
        if (version >= 1) {
            //return new PolymerEntityEntry(buf.readIdentifier(), buf.readVarInt(), buf.readText());
        }
        return null;
    }
}
