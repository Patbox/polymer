package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.List;

@Mixin(EntityAttributesS2CPacket.class)
public abstract class EntityAttributesS2CPacketMixin {

    /**
     * If the entity is not living, use an invalid entity ID so the client ignores it.
     * No error is printed, packet is just silently ignored.
     */
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;"))
    private int polymer_replaceWithPolymer(int input) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity entity && !InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType())) {
            return -1;
        }
        return input;
    }

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeCollection(Ljava/util/Collection;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private Collection<EntityAttributesS2CPacket.Entry> polymer_replaceWithPolymer(Collection<EntityAttributesS2CPacket.Entry> value) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity entity) {
            if (!InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType())) {
                return List.of();
            }

            DefaultAttributeContainer vanillaContainer = DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getPolymerEntityType());
            return value.stream().filter(entry -> vanillaContainer.has(entry.getId())).toList();
        }

        return value;
    }
}
