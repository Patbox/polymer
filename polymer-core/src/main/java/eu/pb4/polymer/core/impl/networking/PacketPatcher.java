package eu.pb4.polymer.core.impl.networking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PacketPatcher {

    private static final Codec<ItemStack> ITEM_VARIANT_FORMATTED_ITEM_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getEntryCodec().fieldOf("item").forGetter(ItemStack::getRegistryEntry),
            ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ItemStack::getComponentChanges)
    ).apply(instance, (item, components) -> new ItemStack(item, 1, components)));

    public static Packet<?> replace(ServerCommonNetworkHandler handler, Packet<?> packet) {
        if (handler instanceof ServerPlayNetworkHandler handler1) {
            if (packet instanceof EntityEquipmentUpdateS2CPacket original && EntityAttachedPacket.get(original, original.getEntityId()) instanceof PolymerEntity polymerEntity) {
                return EntityAttachedPacket.setIfEmpty(
                        new EntityEquipmentUpdateS2CPacket(((Entity) polymerEntity).getId(), polymerEntity.getPolymerVisibleEquipment(original.getEquipmentList(), handler1.getPlayer())),
                        (Entity) polymerEntity
                );
            }

            if (packet instanceof BundleS2CPacket bundleS2CPacket) {
                var list = new ArrayList<Packet<? super ClientPlayPacketListener>>();
                for (var value : bundleS2CPacket.getPackets()) {
                    var x = replace(handler, value);
                    if (!prevent(handler, x)) {
                        //noinspection unchecked
                        list.add((Packet<ClientPlayPacketListener>) x);
                    }
                }

                return new BundleS2CPacket(list);
            }
        } else if (handler instanceof ServerConfigurationPacketListener) {
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
            var player = PacketContext.create(handler);
            if ((
                    packet instanceof StatusEffectPacketExtension packet2
                            && ((packet2.polymer$getStatusEffect() instanceof PolymerStatusEffect pol && pol.getPolymerReplacement(player) == null))
            ) || !EntityAttachedPacket.shouldSend(packet, player.getPlayer())
            ) {
                return true;
            } else if ((packet instanceof EntityEquipmentUpdateS2CPacket original && original.getEquipmentList().isEmpty()) || !EntityAttachedPacket.shouldSend(packet, player.getPlayer())) {
                return true;
            } else if ((packet instanceof EntityAttributesS2CPacket original
                    && EntityAttachedPacket.get(packet, original.getEntityId()) instanceof PolymerEntity entity
                    && !InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType(player)))) {
                return true;
            } else if (packet instanceof BlockEntityUpdateS2CPacket be) {
                return PolymerBlockUtils.isPolymerBlockEntityType(be.getBlockEntityType());
            } else if (packet instanceof RecipeBookAddS2CPacket recipeBook && PolymerImpl.SPLIT_RECIPE_PACKETS > 0 && recipeBook.entries().size() > PolymerImpl.SPLIT_RECIPE_PACKETS) {
                var list = new ArrayList<RecipeBookAddS2CPacket.Entry>();
                if (recipeBook.replace()) {
                    handler.sendPacket(new RecipeBookAddS2CPacket(List.of(), true));
                }
                for (var entry : recipeBook.entries()) {
                    list.add(entry);
                    if (list.size() >= PolymerImpl.SPLIT_RECIPE_PACKETS) {
                        handler.sendPacket(new RecipeBookAddS2CPacket(list, false));
                        list = new ArrayList<>();
                    }
                }
                if (!list.isEmpty()) {
                    handler.sendPacket(new RecipeBookAddS2CPacket(list, false));
                }

                return true;
            }
        }

        return false;
    }

    @Nullable
    private static ItemStack silentItemStackFromNbt(RegistryWrapper.WrapperLookup lookup, NbtCompound nbt) {
        if (nbt.isEmpty()) {
            return null;
        }
        var ops = lookup.getOps(NbtOps.INSTANCE);
        var result = ItemStack.CODEC.parse(ops, nbt);
        if (result.isSuccess()) {
            return result.getOrThrow();
        }
        return null;
    }

    @Nullable
    private static ItemStack silentItemVariantFromNbt(RegistryWrapper.WrapperLookup lookup, NbtCompound nbt) {
        if (nbt.isEmpty()) {
            return null;
        }

        var ops = lookup.getOps(NbtOps.INSTANCE);
        var result = ITEM_VARIANT_FORMATTED_ITEM_STACK_CODEC.parse(ops, nbt);
        if (result.isSuccess()) {
            return result.getOrThrow();
        }
        return null;
    }

    public static NbtCompound transformBlockEntityNbt(PacketContext context, BlockEntityType<?> type, NbtCompound original) {
        if (original.isEmpty()) {
            return original;
        }
        NbtCompound override = null;

        var lookup = context.getRegistryWrapperLookup() != null ? context.getRegistryWrapperLookup() : PolymerImplUtils.FALLBACK_LOOKUP;

        if (original.contains("shared_data", NbtElement.COMPOUND_TYPE)) {
            var shared = original.getCompound("shared_data");
            if (shared.contains("display_item")) {
                var itemNbt = shared.getCompound("display_item");
                var stack = silentItemStackFromNbt(lookup, itemNbt);
                if (stack != null && PolymerItemUtils.isPolymerServerItem(stack, context)) {
                    //noinspection ConstantValue
                    if (override == null) {
                        override = original.copy();
                    }

                    try {
                        override.getCompound("shared_data").put("display_item",
                                PolymerItemUtils.getPolymerItemStack(stack, context).toNbtAllowEmpty(lookup));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        if (original.contains("Items", NbtElement.LIST_TYPE)) {
            var list = original.getList("Items", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                var nbt = list.getCompound(i);
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
                    override.getList("Items", NbtElement.COMPOUND_TYPE).set(i, stack.isEmpty() ? new NbtCompound() : stack.toNbt(lookup, nbt));
                }
            }
        }

        if (original.contains("item", NbtElement.COMPOUND_TYPE)) {
            var nbt = original.getCompound("item");
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
                        ? ITEM_VARIANT_FORMATTED_ITEM_STACK_CODEC.encodeStart(lookup.getOps(NbtOps.INSTANCE), stack).getOrThrow()
                        : stack.toNbtAllowEmpty(lookup)
                );
            }
        }

        if (original.contains("components", NbtElement.COMPOUND_TYPE)) {
            var ops = lookup.getOps(NbtOps.INSTANCE);

            var comp = ComponentMap.CODEC.decode(ops, original.getCompound("components"));
            if (comp.isSuccess()) {
                var map = comp.getOrThrow().getFirst();
                ComponentMap.Builder builder = null;

                for (var component : map) {
                    if (component.value() instanceof TransformingComponent transformingComponent && transformingComponent.polymer$requireModification(context)) {
                        if (builder == null) {
                            builder = ComponentMap.builder();
                            builder.addAll(map);
                        }
                        //noinspection unchecked
                        builder.add((ComponentType<? super Object>) component.type(), transformingComponent.polymer$getTransformed(context));
                    } else if (!PolymerComponent.canSync(component.type(), component.value(), context)) {
                        if (builder == null) {
                            builder = ComponentMap.builder();
                            builder.addAll(map);
                        }
                        builder.add(component.type(), null);
                    }
                }

                if (builder != null) {
                    if (override == null) {
                        override = original.copy();
                    }
                    override.put("components", ComponentMap.CODEC.encodeStart(ops, builder.build()).result().orElse(new NbtCompound()));
                }
            }
        }

        return override != null ? override : original;
    }
}
