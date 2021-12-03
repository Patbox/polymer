package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.entity.PolymerTrackedDataHandler;
import eu.pb4.polymer.impl.other.InternalEntityHelpers;
import eu.pb4.polymer.mixin.ItemFrameEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public class EntityTrackerUpdateS2CPacketMixin {
    @Shadow
    @Mutable
    private List<DataTracker.Entry<?>> trackedValues;

    @Inject(method = "<init>(ILnet/minecraft/entity/data/DataTracker;Z)V", at = @At("TAIL"))
    private void polymer_removeInvalidEntries(int id, DataTracker tracker, boolean forceUpdateAll, CallbackInfo ci) {
        Entity entity = ((DataTrackerAccessor) tracker).getTrackedEntity();
        List<DataTracker.Entry<?>> entries = new ArrayList<>();

        if (entity instanceof PolymerEntity polymerEntity) {
            List<DataTracker.Entry<?>> legalTrackedData = InternalEntityHelpers.getExampleTrackedDataOfEntityType((polymerEntity.getPolymerEntityType()));

            if (legalTrackedData.size() > 0) {
                entries = new ArrayList<>();

                if (this.trackedValues != null) {
                    for (DataTracker.Entry<?> entry : this.trackedValues) {
                        for (DataTracker.Entry<?> trackedData : legalTrackedData) {
                            if (trackedData.getData().getId() == entry.getData().getId() && entry.get().getClass().isInstance(trackedData.get())) {
                                entries.add(entry);
                                break;
                            }
                        }
                    }
                }

                polymerEntity.modifyTrackedData(entries);
                this.trackedValues = entries;
            } else {
                for (DataTracker.Entry<?> entry : this.trackedValues) {
                    if (entry.getData().getId() <= 13) {
                        entries.add(entry);
                    }
                }

                polymerEntity.modifyTrackedData(entries);
            }
        } else if (this.trackedValues == null) {
            return;
        } else {
            entries.addAll(this.trackedValues);
        }

        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);

            if (entry.getData() == ItemFrameEntityAccessor.getITEM_STACK() && entry.get() instanceof ItemStack stack) {
                entries.set(i, new DataTracker.Entry<>(PolymerTrackedDataHandler.CUSTOM_ITEM_FRAME_STACK, stack));
            }
        }

        this.trackedValues = entries;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getTrackedValues", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceItemsWithPolymerOnes(CallbackInfoReturnable<List<DataTracker.Entry<?>>> cir) {
        if (MinecraftClient.getInstance().getServer() != null && this.trackedValues != null) {
            List<DataTracker.Entry<?>> list = new ArrayList<>();
            ServerPlayerEntity player = PolymerUtils.getPlayer();

            for (DataTracker.Entry<?> entry : cir.getReturnValue()) {
                if (entry.get() instanceof ItemStack stack) {
                    if (entry.getData() == PolymerTrackedDataHandler.CUSTOM_ITEM_FRAME_STACK) {
                        var polymerStack = PolymerItemUtils.getPolymerItemStack(stack, PolymerUtils.getPlayer());

                        if (!stack.hasCustomName() && !(stack.getItem() instanceof PolymerItem polymerItem && polymerItem.showDefaultNameInItemFrames())) {
                            polymerStack.removeCustomName();
                        }
                        list.add(new DataTracker.Entry(ItemFrameEntityAccessor.getITEM_STACK(), polymerStack));
                    } else {
                        list.add(new DataTracker.Entry(entry.getData(), PolymerItemUtils.getPolymerItemStack(stack, player)));
                    }
                } else if (entry.get() instanceof Optional<?> optionalO && optionalO.isPresent()
                        && optionalO.get() instanceof BlockState state && state.getBlock() instanceof PolymerBlock polymerBlock) {
                    list.add(new DataTracker.Entry(entry.getData(), Optional.of(PolymerBlockUtils.getBlockStateSafely(polymerBlock, state))));
                } else {
                    list.add(entry);
                }
            }

            cir.setReturnValue(list);
        }
    }
}
