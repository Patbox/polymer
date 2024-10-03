package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.PossiblyInitialPacket;
import net.minecraft.block.Block;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.village.VillagerData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
@Mixin(EntityTrackerUpdateS2CPacket.class)
public class EntityTrackerUpdateS2CPacketMixin implements PossiblyInitialPacket {
    @Shadow
    @Final
    private int id;
    @Unique
    private boolean isInitial = false;

    @Unique
    @Nullable
    private List<DataTracker.SerializedEntry<?>> polymer$createEntries(List<DataTracker.SerializedEntry<?>> trackedValues) {
        var entity = EntityAttachedPacket.get(this, this.id);
        if (entity == null) {
            return trackedValues != null ? new ArrayList<>(trackedValues) : null;
        }

        var entries = new ArrayList<DataTracker.SerializedEntry<?>>();
        var player = PacketContext.get();

        if (entity instanceof PolymerEntity polymerEntity && InternalEntityHelpers.canPatchTrackedData(player.getPlayer(), entity)) {
            var mod = trackedValues != null ? new ArrayList<>(trackedValues) : new ArrayList<DataTracker.SerializedEntry<?>>();
            polymerEntity.modifyRawTrackedData(mod, player.getPlayer(), this.isInitial);

            var legalTrackedData = InternalEntityHelpers.getExampleTrackedDataOfEntityType((polymerEntity.getPolymerEntityType(player)));

            if (!mod.isEmpty() && legalTrackedData != null && legalTrackedData.length != 0) {
                for (var entry : mod) {
                    if (entry.id() < legalTrackedData.length) {
                        var x = legalTrackedData[entry.id()];
                        if (x != null && x.getData().dataType() == entry.handler()) {
                            entries.add(entry);
                        }
                    }
                }
            } else {
                entries.addAll(mod);
            }
        } else if (trackedValues == null) {
            return null;
        } else {
            entries.addAll(trackedValues);
        }

        final var isMinecart = entity instanceof AbstractMinecartEntity;
        final var size = entries.size();
        for (int i = 0; i < size; i++) {
            var entry = entries.get(i);

            if (isMinecart && entry.id() == AbstractMinecartEntityAccessor.getCUSTOM_BLOCK_ID().id()) {
                entries.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId((int) entry.value()), player))));
            } else if (entry.value() instanceof VillagerData data) {
                var x = PolymerEntityUtils.getPolymerProfession(data.getProfession());
                if (x != null) {
                    entries.set(i, new DataTracker.SerializedEntry(entry.id(), entry.handler(), data.withProfession(x.getPolymerProfession(data.getProfession(), player))));
                }
            }
        }

        return entries;
    }

    @ModifyArg(method = "write(Lnet/minecraft/network/RegistryByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntityTrackerUpdateS2CPacket;write(Ljava/util/List;Lnet/minecraft/network/RegistryByteBuf;)V"))
    private List<DataTracker.SerializedEntry<?>> polymer$changeForPacket(List<DataTracker.SerializedEntry<?>> value) {
        return this.polymer$createEntries(value);
    }

    @Override
    public boolean polymer$getInitial() {
        return this.isInitial;
    }

    @Override
    public void polymer$setInitial() {
        this.isInitial = true;
    }
}
