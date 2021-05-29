package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.entity.VirtualEntity;
import eu.pb4.polymer.other.Helpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public class EntityTrackerUpdateS2CPacketMixin {
    @Shadow @Mutable
    private List<DataTracker.Entry<?>> trackedValues;

    @Inject(method = "<init>(ILnet/minecraft/entity/data/DataTracker;Z)V", at = @At("TAIL"))
    private void removeInvalidEntries(int id, DataTracker tracker, boolean forceUpdateAll, CallbackInfo ci) {
        Entity entity = ((DataTrackerAccessor) tracker).getTrackedEntity();

        if (entity instanceof VirtualEntity) {
            List<DataTracker.Entry<?>> legalTrackedData = Helpers.getExampleTrackedDataOfEntityType(((VirtualEntity) entity).getVirtualEntityType());

            if (legalTrackedData.size() > 0) {
                List<DataTracker.Entry<?>> entries = new ArrayList<>();
                for (DataTracker.Entry<?> entry : this.trackedValues) {
                    for (DataTracker.Entry<?> trackedData : legalTrackedData) {
                        if (trackedData.getData().getId() == entry.getData().getId() && entry.get().getClass().isInstance(trackedData.get())) {
                            entries.add(entry);
                            break;
                        }
                    }
                }

                ((VirtualEntity) entity).modifyTrackedData(entries);
                this.trackedValues = entries;
            } else {
                List<DataTracker.Entry<?>> list = new ArrayList<>();
                for (DataTracker.Entry<?> entry : this.trackedValues) {
                    if (entry.getData().getId() <= 13) {
                        list.add(entry);
                    }
                }

                ((VirtualEntity) entity).modifyTrackedData(list);
                this.trackedValues = list;
            }
        }
    }
}
