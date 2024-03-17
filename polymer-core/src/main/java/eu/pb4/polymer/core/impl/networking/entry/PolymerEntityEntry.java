package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.impl.networking.payloads.s2c.PolymerEntityS2CPayload;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerEntityEntry(Identifier identifier, int rawId, Text name) {

    public static final PacketCodec<ContextByteBuf, PolymerEntityEntry> CODEC = PacketCodec.of(PolymerEntityEntry::write, PolymerEntityEntry::read);

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(this.rawId);
        TextCodecs.PACKET_CODEC.encode(buf, name);
    }

    public static PolymerEntityEntry of(EntityType<?> entityType) {
        return new PolymerEntityEntry(
                Registries.ENTITY_TYPE.getId(entityType),
                Registries.ENTITY_TYPE.getRawId(entityType),
                entityType.getName()
        );
    }

    public static PolymerEntityEntry read(PacketByteBuf buf) {
        return new PolymerEntityEntry(buf.readIdentifier(), buf.readVarInt(), TextCodecs.PACKET_CODEC.decode(buf));
    }
}
