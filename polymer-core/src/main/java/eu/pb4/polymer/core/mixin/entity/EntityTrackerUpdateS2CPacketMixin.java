package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.EntityTrackerUpdateS2CPacketExt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
@Mixin(EntityTrackerUpdateS2CPacket.class)
public class EntityTrackerUpdateS2CPacketMixin implements EntityTrackerUpdateS2CPacketExt {
    @Shadow @Final private int id;

    @Shadow @Final private List<DataTracker.SerializedEntry<?>> trackedValues;
    @Unique
    private boolean polymer$isInitial = false;

    @Nullable
    private List<DataTracker.SerializedEntry<?>> polymer$createEntries() {
        var entity = EntityAttachedPacket.get(this, this.id);
        if (entity == null) {
            return this.trackedValues != null ? new ArrayList<>(this.trackedValues) : null;
        }

        var entries = new ArrayList<DataTracker.SerializedEntry<?>>();
        var player = PolymerUtils.getPlayerContext();

        if (entity instanceof PolymerEntity polymerEntity && InternalEntityHelpers.canPatchTrackedData(player, entity)) {
            var mod = this.trackedValues != null ? new ArrayList<>(this.trackedValues) : new ArrayList<DataTracker.SerializedEntry<?>>();
            polymerEntity.modifyRawTrackedData(mod, player, this.polymer$isInitial);

            var legalTrackedData = InternalEntityHelpers.getExampleTrackedDataOfEntityType((polymerEntity.getPolymerEntityType(player)));

            if (mod.size() > 0 && legalTrackedData != null && legalTrackedData.size() > 0) {
                for (var entry : mod) {
                    var x = legalTrackedData.get(entry.id());
                    if (x != null && x.getData().getType() == entry.handler()) {
                        entries.add(entry);
                    }
                }
            } else {
                entries.addAll(mod);
            }
        } else if (this.trackedValues == null) {
            return null;
        } else {
            entries.addAll(this.trackedValues);
        }

        final var isItemFrame = entity instanceof ItemFrameEntity;
        final var isMinecart = entity instanceof AbstractMinecartEntity;
        final var size = entries.size();
        for (int i = 0; i < size; i++) {
            var entry = entries.get(i);

            if (isItemFrame && entry.id() == ItemFrameEntityAccessor.getITEM_STACK().getId() && entry.value() instanceof ItemStack stack) {
                var polymerStack = PolymerItemUtils.getPolymerItemStack(stack, player);

                if (!stack.hasCustomName() && !(stack.getItem() instanceof PolymerItem polymerItem && polymerItem.showDefaultNameInItemFrames())) {
                    var nbtCompound = polymerStack.getSubNbt("display");
                    if (nbtCompound != null) {
                        var name = nbtCompound.get("Name");
                        if (name != null) {
                            polymerStack.getNbt().put(PolymerItemUtils.ITEM_FRAME_NAME_TAG, name);
                        }
                        nbtCompound.remove("Name");
                    }
                }

                entries.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), polymerStack));
            } else if (isMinecart && entry.id() == AbstractMinecartEntityAccessor.getCUSTOM_BLOCK_ID().getId()) {
                entries.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId((int) entry.value()), player))));
            }
        }

        return entries;
    }

    @ModifyArg(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "net/minecraft/network/packet/s2c/play/EntityTrackerUpdateS2CPacket.write(Ljava/util/List;Lnet/minecraft/network/PacketByteBuf;)V", ordinal = 0))
    private List<DataTracker.SerializedEntry<?>> polymer$changeForPacket(List<DataTracker.Entry<?>> value) {
        return this.polymer$createEntries();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "trackedValues", at = @At("RETURN"), cancellable = true)
    private void polymer$patchDataClient(CallbackInfoReturnable<List<DataTracker.SerializedEntry<?>>> cir) {
        if (ClientUtils.isSingleplayer() && this.trackedValues != null) {
            var list = this.polymer$createEntries();

            ServerPlayerEntity player = ClientUtils.getPlayer();

            for (int i = 0; i < list.size(); i++) {
                var entry = list.get(i);
                if (entry.value() instanceof Optional<?> optionalO && optionalO.isPresent()
                        && optionalO.get() instanceof BlockState state) {
                    list.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), Optional.of(PolymerBlockUtils.getPolymerBlockState(state, player))));
                } else if (entry.value() instanceof BlockState state) {
                    list.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), PolymerBlockUtils.getPolymerBlockState(state, player)));
                } else if (entry.value() instanceof ItemStack stack) {
                    list.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), PolymerItemUtils.getPolymerItemStack(stack, player)));
                }
            }

            cir.setReturnValue(list);
        }
    }

    @Override
    public boolean polymer$getInitial() {
        return this.polymer$isInitial;
    }

    @Override
    public void polymer$setInitial() {
        this.polymer$isInitial = true;
    }
}
