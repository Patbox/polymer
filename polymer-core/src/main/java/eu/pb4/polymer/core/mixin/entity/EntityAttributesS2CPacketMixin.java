package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(EntityAttributesS2CPacket.class)
public abstract class EntityAttributesS2CPacketMixin {
    @SuppressWarnings("UnreachableCode")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, EntityAttributesS2CPacket> patchCodec(PacketCodec<RegistryByteBuf, EntityAttributesS2CPacket> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, packet) -> {
            if (EntityAttachedPacket.get(packet, packet.getEntityId()) instanceof PolymerEntity entity) {
                var type = entity.getPolymerEntityType(PolymerUtils.getPlayerContext());
                var p = new EntityAttributesS2CPacket(packet.getEntityId(), List.of());
                var list = ((EntityAttributesS2CPacketAccessor) p).getEntries();
                var vanillaContainer = DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) type);
                for (var entry : packet.getEntries()) {
                    if (vanillaContainer.has(entry.attribute()) && !PolymerEntityUtils.isPolymerEntityAttribute(entry.attribute())) {
                        list.add(entry);
                    }
                }
                return p;
            } else {
                var p = new EntityAttributesS2CPacket(packet.getEntityId(), List.of());
                var list = ((EntityAttributesS2CPacketAccessor) p).getEntries();
                for (var entry : packet.getEntries()) {
                    if (!PolymerEntityUtils.isPolymerEntityAttribute(entry.attribute())) {
                        list.add(entry);
                    }
                }
                return p;
            }
        });
    }
}
