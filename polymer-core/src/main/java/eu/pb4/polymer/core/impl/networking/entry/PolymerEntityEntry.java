package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.impl.networking.payloads.s2c.PolymerEntityS2CPayload;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerEntityEntry(Identifier identifier, int rawId, Component name) {

    public static final StreamCodec<ContextByteBuf, PolymerEntityEntry> CODEC = StreamCodec.ofMember(PolymerEntityEntry::write, PolymerEntityEntry::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(this.rawId);
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, name);
    }

    public static PolymerEntityEntry of(EntityType<?> entityType) {
        return new PolymerEntityEntry(
                BuiltInRegistries.ENTITY_TYPE.getKey(entityType),
                BuiltInRegistries.ENTITY_TYPE.getId(entityType),
                entityType.getDescription()
        );
    }

    public static PolymerEntityEntry read(FriendlyByteBuf buf) {
        return new PolymerEntityEntry(buf.readIdentifier(), buf.readVarInt(), ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf));
    }
}
