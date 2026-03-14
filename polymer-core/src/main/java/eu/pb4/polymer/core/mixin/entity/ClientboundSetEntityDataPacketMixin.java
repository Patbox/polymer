package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.PossiblyInitialPacket;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.npc.villager.VillagerData;

@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
@Mixin(ClientboundSetEntityDataPacket.class)
public class ClientboundSetEntityDataPacketMixin implements PossiblyInitialPacket {
    @Shadow
    @Final
    private int id;
    @Unique
    private boolean isInitial = false;

    @Unique
    @Nullable
    private List<SynchedEntityData.DataValue<?>> polymer$createEntries(List<SynchedEntityData.DataValue<?>> trackedValues) {
        var entity = EntityAttachedPacket.get(this, this.id);
        if (entity == null) {
            return trackedValues != null ? new ArrayList<>(trackedValues) : null;
        }

        var entries = new ArrayList<SynchedEntityData.DataValue<?>>();
        var player = PacketContext.get();

        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null && InternalEntityHelpers.canPatchTrackedData(PolymerCommonUtils.getPlayer(player), entity)) {
            var mod = trackedValues != null ? new ArrayList<>(trackedValues) : new ArrayList<SynchedEntityData.DataValue<?>>();
            polymerEntity.modifyRawTrackedData(mod, PolymerCommonUtils.getPlayer(player), this.isInitial);

            var legalTrackedData = InternalEntityHelpers.getExampleTrackedDataOfEntityType((polymerEntity.getPolymerEntityType(player)));

            if (!mod.isEmpty() && legalTrackedData != null && legalTrackedData.length != 0) {
                for (var entry : mod) {
                    if (entry.id() < legalTrackedData.length) {
                        var x = legalTrackedData[entry.id()];
                        if (x != null && x.getAccessor().serializer() == entry.serializer()) {
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

        final var size = entries.size();
        for (int i = 0; i < size; i++) {
            var entry = entries.get(i);

            if (entry.value() instanceof VillagerData data) {
                var x = PolymerEntityUtils.getPolymerProfession(data.profession().value());
                if (x != null) {
                    entries.set(i, new SynchedEntityData.DataValue(entry.id(), entry.serializer(), data.withProfession(BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(x.getPolymerReplacement(data.profession().value(), player)))));
                }
            }
        }

        return entries;
    }

    @ModifyArg(method = "write(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundSetEntityDataPacket;pack(Ljava/util/List;Lnet/minecraft/network/RegistryFriendlyByteBuf;)V"))
    private List<SynchedEntityData.DataValue<?>> polymer$changeForPacket(List<SynchedEntityData.DataValue<?>> value) {
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
