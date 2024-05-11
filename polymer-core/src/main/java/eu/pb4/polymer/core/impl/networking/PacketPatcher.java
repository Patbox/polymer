package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;

public class PacketPatcher {

    public static Packet<?> replace(ServerCommonNetworkHandler handler, Packet<?> packet) {
        if (handler.getClass() == ServerPlayNetworkHandler.class) {
            if (packet instanceof EntityEquipmentUpdateS2CPacket original && EntityAttachedPacket.get(original, original.getId()) instanceof PolymerEntity polymerEntity) {
                return EntityAttachedPacket.setIfEmpty(
                        new EntityEquipmentUpdateS2CPacket(((Entity) polymerEntity).getId(), polymerEntity.getPolymerVisibleEquipment(original.getEquipmentList(), ((ServerPlayNetworkHandler) handler).getPlayer())),
                        (Entity) polymerEntity
                );
            }

            if (packet instanceof BundleS2CPacket bundleS2CPacket) {
                var list = new ArrayList<Packet<? super ClientPlayPacketListener>>();
                var iterator = bundleS2CPacket.getPackets().iterator();
                while (iterator.hasNext()) {
                    var x = replace(handler, iterator.next());
                    if (!prevent(handler, x)) {
                        list.add((Packet<ClientPlayPacketListener>) x);
                    }
                }

                return new BundleS2CPacket(list);
            }
        } else if (handler.getClass() == ServerConfigurationNetworkHandler.class) {
            if (packet instanceof FeaturesS2CPacket featuresS2CPacket) {
                var x = PolymerUtils.getClientEnabledFeatureFlags();

                if (x.isEmpty()) {
                    return packet;
                }

                FeatureSet set = FeatureFlags.FEATURE_MANAGER.featureSetOf(x.toArray(new FeatureFlag[0]));

                if (featuresS2CPacket.features().getClass() == HashSet.class) {
                    featuresS2CPacket.features().addAll(FeatureFlags.FEATURE_MANAGER.toId(set));
                } else {
                    var y = new HashSet<Identifier>();
                    y.addAll(featuresS2CPacket.features());
                    y.addAll(FeatureFlags.FEATURE_MANAGER.toId(set));
                    return new FeaturesS2CPacket(y);
                }
            }

        }

        return packet;
    }

    public static void sendExtra(ServerCommonNetworkHandler handler, Packet<?> packet) {
        if (handler.getClass() == ServerPlayNetworkHandler.class) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                ImmersivePortalsUtils.sendBlockPackets((ServerPlayNetworkHandler) handler, packet);
            } else {
                BlockPacketUtil.sendFromPacket(packet, (ServerPlayNetworkHandler) handler);
            }
        }
    }

    public static boolean prevent(ServerCommonNetworkHandler handler, Packet<?> packet) {
        if (handler.getClass() == ServerPlayNetworkHandler.class) {
            var player = ((ServerPlayNetworkHandler) handler).player;
            if ((
                    packet instanceof StatusEffectPacketExtension packet2
                            && ((packet2.polymer$getStatusEffect() instanceof PolymerStatusEffect pol && pol.getPolymerReplacement(player) == null))
            ) || !EntityAttachedPacket.shouldSend(packet, player)
            ) {
                return true;
            } else if ((packet instanceof EntityEquipmentUpdateS2CPacket original && original.getEquipmentList().isEmpty()) || !EntityAttachedPacket.shouldSend(packet, player)) {
                return true;
            } else if ((packet instanceof EntityAttributesS2CPacket original
                    && EntityAttachedPacket.get(packet, original.getEntityId()) instanceof PolymerEntity entity
                    && InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType(player)))) {
                return true;
            } else if (packet instanceof BlockEntityUpdateS2CPacket be && PolymerBlockUtils.isPolymerBlockEntityType(be.getBlockEntityType())) {
                return true;
            }
        }

        return false;
    }
}
