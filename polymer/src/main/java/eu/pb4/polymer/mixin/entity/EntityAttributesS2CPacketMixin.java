package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
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
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity entity && !InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType(PolymerUtils.getPlayer()))) {
            return -1;
        }
        return input;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getEntityId", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceWithPolymer2(CallbackInfoReturnable<Integer> cir) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity entity && !InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType(PolymerUtils.getPlayer()))) {
            cir.setReturnValue(-1);
        }
    }

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeCollection(Ljava/util/Collection;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private Collection<EntityAttributesS2CPacket.Entry> polymer_replaceWithPolymer(Collection<EntityAttributesS2CPacket.Entry> value) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity entity) {
            var type = entity.getPolymerEntityType(PolymerUtils.getPlayer());
            if (!InternalEntityHelpers.isLivingEntity(type)) {
                return List.of();
            }

            DefaultAttributeContainer vanillaContainer = DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) type);
            List<EntityAttributesS2CPacket.Entry> list = new ArrayList<>();
            for (EntityAttributesS2CPacket.Entry entry : value) {
                if (vanillaContainer.has(entry.getId())) {
                    list.add(entry);
                }
            }
            return list;
        }

        return value;
    }
}
