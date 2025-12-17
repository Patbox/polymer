package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.PossiblyInitialPacket;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientboundUpdateAttributesPacket.class)
public abstract class ClientboundUpdateAttributesPacketMixin implements PossiblyInitialPacket {
    @Unique
    private boolean isInitial = false;

    @Override
    public boolean polymer$getInitial() {
        return this.isInitial;
    }

    @Override
    public void polymer$setInitial() {
        this.isInitial = true;
    }

    @SuppressWarnings("UnreachableCode")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket> patchCodec(StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, packet) -> {
            if (PolymerEntity.get(EntityAttachedPacket.get(packet, packet.getEntityId())) instanceof PolymerEntity entity) {
                var context = PacketContext.get();
                var type = entity.getPolymerEntityType(context);
                var p = new ClientboundUpdateAttributesPacket(packet.getEntityId(), List.of());
                var list = ((ClientboundUpdateAttributesPacketAccessor) p).getAttributes();
                //noinspection unchecked
                var vanillaContainer = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) type);
                var data = new ArrayList<>(packet.getValues());
                entity.modifyRawEntityAttributeData(data, context.getPlayer(), ((PossiblyInitialPacket) packet).polymer$getInitial());
                for (var entry : data) {
                    if (vanillaContainer.hasAttribute(entry.attribute()) && !PolymerEntityUtils.isPolymerEntityAttribute(entry.attribute())) {
                        list.add(entry);
                    }
                }
                return p;
            } else {
                var p = new ClientboundUpdateAttributesPacket(packet.getEntityId(), List.of());
                var list = ((ClientboundUpdateAttributesPacketAccessor) p).getAttributes();
                for (var entry : packet.getValues()) {
                    if (!PolymerEntityUtils.isPolymerEntityAttribute(entry.attribute())) {
                        list.add(entry);
                    }
                }
                return p;
            }
        });
    }
}
