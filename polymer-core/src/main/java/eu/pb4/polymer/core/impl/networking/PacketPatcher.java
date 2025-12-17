package eu.pb4.polymer.core.impl.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PacketPatcher {

    private static final Codec<ItemStack> ITEM_VARIANT_FORMATTED_ITEM_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemStack::getItemHolder),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
    ).apply(instance, (item, components) -> new ItemStack(item, 1, components)));

    public static Packet<?> replace(ServerCommonPacketListenerImpl handler, Packet<?> packet) {
        if (handler instanceof ServerGamePacketListenerImpl handler1) {
            if (packet instanceof ClientboundSetEquipmentPacket original) {
                var entity = EntityAttachedPacket.get(original, original.getEntity());
                var polymerEntity = PolymerEntity.get(entity);
                if (polymerEntity != null) {
                    return EntityAttachedPacket.setIfEmpty(
                            new ClientboundSetEquipmentPacket(entity.getId(), polymerEntity.getPolymerVisibleEquipment(original.getSlots(), handler1.getPlayer())),
                            entity
                    );
                }
            }

            if (packet instanceof ClientboundBundlePacket bundleS2CPacket) {
                var list = new ArrayList<Packet<? super ClientGamePacketListener>>();
                for (var value : bundleS2CPacket.subPackets()) {
                    var x = replace(handler, value);
                    if (!prevent(handler, x)) {
                        //noinspection unchecked
                        list.add((Packet<ClientGamePacketListener>) x);
                    }
                }

                return new ClientboundBundlePacket(list);
            }
        } else if (handler instanceof ServerConfigurationPacketListener) {
            if (packet instanceof ClientboundUpdateEnabledFeaturesPacket featuresS2CPacket) {
                var x = PolymerUtils.getClientEnabledFeatureFlags();

                if (x.isEmpty()) {
                    return packet;
                }

                FeatureFlagSet set = FeatureFlags.REGISTRY.subset(x.toArray(new FeatureFlag[0]));

                if (featuresS2CPacket.features().getClass() == HashSet.class) {
                    featuresS2CPacket.features().addAll(FeatureFlags.REGISTRY.toNames(set));
                } else {
                    var y = new HashSet<Identifier>();
                    y.addAll(featuresS2CPacket.features());
                    y.addAll(FeatureFlags.REGISTRY.toNames(set));
                    return new ClientboundUpdateEnabledFeaturesPacket(y);
                }
            }

        }

        return packet;
    }

    public static void sendExtra(ServerCommonPacketListenerImpl handler, Packet<?> packet) {
        if (handler.getClass() == ServerGamePacketListenerImpl.class) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                ImmersivePortalsUtils.sendBlockPackets((ServerGamePacketListenerImpl) handler, packet);
            } else {
                BlockPacketUtil.sendFromPacket(packet, (ServerGamePacketListenerImpl) handler);
            }
        }
    }

    public static boolean prevent(ServerCommonPacketListenerImpl handler, Packet<?> packet) {
        if (handler.getClass() == ServerGamePacketListenerImpl.class) {
            var player = PacketContext.create(handler);
            //noinspection DataFlowIssue
            if ((
                    packet instanceof StatusEffectPacketExtension packet2
                            && ((PolymerSyncedObject.getSyncedObject(BuiltInRegistries.MOB_EFFECT, packet2.polymer$getStatusEffect()) != null
                            && PolymerSyncedObject.getSyncedObject(BuiltInRegistries.MOB_EFFECT, packet2.polymer$getStatusEffect()).getPolymerReplacement(packet2.polymer$getStatusEffect(), player) == null))
            ) || !EntityAttachedPacket.shouldSend(packet, player.getPlayer())
            ) {
                return true;
            } else if ((packet instanceof ClientboundSetEquipmentPacket original && original.getSlots().isEmpty()) || !EntityAttachedPacket.shouldSend(packet, player.getPlayer())) {
                return true;
            } else if ((packet instanceof ClientboundUpdateAttributesPacket original
                    && PolymerEntity.get(EntityAttachedPacket.get(packet, original.getEntityId())) instanceof PolymerEntity entity
                    && !InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType(player)))) {
                return true;
            } else if (packet instanceof ClientboundBlockEntityDataPacket be) {
                return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK_ENTITY_TYPE, be.getType()) instanceof PolymerSyncedObject<BlockEntityType<?>> obj
                        && obj.getPolymerReplacement(be.getType(), player) == null;
            } else if (packet instanceof ClientboundRecipeBookAddPacket recipeBook && PolymerImpl.SPLIT_RECIPE_PACKETS > 0 && recipeBook.entries().size() > PolymerImpl.SPLIT_RECIPE_PACKETS) {
                var list = new ArrayList<ClientboundRecipeBookAddPacket.Entry>();
                if (recipeBook.replace()) {
                    handler.send(new ClientboundRecipeBookAddPacket(List.of(), true));
                }
                for (var entry : recipeBook.entries()) {
                    list.add(entry);
                    if (list.size() >= PolymerImpl.SPLIT_RECIPE_PACKETS) {
                        handler.send(new ClientboundRecipeBookAddPacket(list, false));
                        list = new ArrayList<>();
                    }
                }
                if (!list.isEmpty()) {
                    handler.send(new ClientboundRecipeBookAddPacket(list, false));
                }

                return true;
            } else if (packet instanceof ClientboundAnimatePacket animationS2CPacket && PolymerEntity.get(EntityAttachedPacket.get(packet, animationS2CPacket.getId())) instanceof PolymerEntity polymerEntity
                    && !InternalEntityHelpers.isLivingEntity(polymerEntity.getPolymerEntityType(PacketContext.create(handler)))) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private static ItemStack silentItemStackFromNbt(HolderLookup.Provider lookup, CompoundTag nbt) {
        if (nbt.isEmpty()) {
            return null;
        }
        var ops = lookup.createSerializationContext(NbtOps.INSTANCE);
        var result = ItemStack.CODEC.parse(ops, nbt);
        if (result.isSuccess()) {
            return result.getOrThrow();
        }
        return null;
    }

    @Nullable
    private static ItemStack silentItemVariantFromNbt(HolderLookup.Provider lookup, CompoundTag nbt) {
        if (nbt.isEmpty()) {
            return null;
        }

        var ops = lookup.createSerializationContext(NbtOps.INSTANCE);
        var result = ITEM_VARIANT_FORMATTED_ITEM_STACK_CODEC.parse(ops, nbt);
        if (result.isSuccess()) {
            return result.getOrThrow();
        }
        return null;
    }

    public static CompoundTag transformBlockEntityNbt(PacketContext context, BlockEntityType<?> type, CompoundTag original) {
        if (original.isEmpty()) {
            return original;
        }
        CompoundTag override = null;

        var lookup = context.getRegistryWrapperLookup() != null ? context.getRegistryWrapperLookup() : PolymerImplUtils.FALLBACK_LOOKUP;
        var ops = lookup.createSerializationContext(NbtOps.INSTANCE);
        if (original.get("shared_data") instanceof CompoundTag shared) {
            if (shared.get("display_item") instanceof CompoundTag itemNbt) {
                var stack = silentItemStackFromNbt(lookup, itemNbt);
                if (stack != null && PolymerItemUtils.isPolymerServerItem(stack, context)) {
                    //noinspection ConstantValue
                    if (override == null) {
                        override = original.copy();
                    }

                    try {
                        override.getCompoundOrEmpty("shared_data").store("display_item",
                                ItemStack.OPTIONAL_CODEC, ops, PolymerItemUtils.getPolymerItemStack(stack, context));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        if (original.get("Items") instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                var nbt = list.getCompoundOrEmpty(i);
                var stack = silentItemStackFromNbt(lookup, nbt);
                if (stack != null && PolymerItemUtils.isPolymerServerItem(stack, context)) {
                    if (override == null) {
                        override = original.copy();
                    }
                    nbt = nbt.copy();
                    nbt.remove("id");
                    nbt.remove("components");
                    nbt.remove("count");
                    stack = PolymerItemUtils.getPolymerItemStack(stack, context);
                    override.getListOrEmpty("Items").set(i, ItemStack.OPTIONAL_CODEC.encode(stack, ops, nbt).getOrThrow());
                }
            }
        }

        if (original.get("item") instanceof CompoundTag nbt) {
            var stack = silentItemStackFromNbt(lookup, nbt);
            boolean variant = false;
            if (stack == null) {
                stack = silentItemVariantFromNbt(lookup, nbt);
                variant = stack != null;
            }

            if (stack != null && PolymerItemUtils.isPolymerServerItem(stack, context)) {
                if (override == null) {
                    override = original.copy();
                }
                stack = PolymerItemUtils.getPolymerItemStack(stack, context);
                override.put("item", variant
                        ? ITEM_VARIANT_FORMATTED_ITEM_STACK_CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow()
                        : ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack).getOrThrow());
            }
        }

        if (original.get("components") instanceof CompoundTag compound) {
            var comp = DataComponentMap.CODEC.decode(ops, compound);
            if (comp.isSuccess()) {
                var map = comp.getOrThrow().getFirst();
                DataComponentMap.Builder builder = null;

                for (var component : map) {
                    if (component.value() instanceof TransformingComponent transformingComponent && transformingComponent.polymer$requireModification(context)) {
                        if (builder == null) {
                            builder = DataComponentMap.builder();
                            builder.addAll(map);
                        }
                        //noinspection unchecked
                        builder.set((DataComponentType<? super Object>) component.type(), transformingComponent.polymer$getTransformed(context));
                    } else if (!PolymerComponent.canSync(component.type(), component.value(), context)) {
                        if (builder == null) {
                            builder = DataComponentMap.builder();
                            builder.addAll(map);
                        }
                        builder.set(component.type(), null);
                    }
                }

                if (builder != null) {
                    if (override == null) {
                        override = original.copy();
                    }
                    override.put("components", DataComponentMap.CODEC.encodeStart(ops, builder.build()).result().orElse(new CompoundTag()));
                }
            }
        }

        return override != null ? override : original;
    }
}
