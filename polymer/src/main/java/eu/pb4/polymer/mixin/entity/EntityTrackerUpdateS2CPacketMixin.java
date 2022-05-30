package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public class EntityTrackerUpdateS2CPacketMixin {

    @Shadow
    @Final
    private @Nullable List<DataTracker.Entry<?>> trackedValues;

    @Shadow @Final private int id;

    @Nullable
    private List<DataTracker.Entry<?>> polymer_parseEntries() {
        Entity entity = EntityAttachedPacket.get(this);
        if (entity == null || entity.getId() != this.id) {
            return this.trackedValues != null ? new ArrayList<>(this.trackedValues) : null;
        }

        var entries = new ArrayList<DataTracker.Entry<?>>();
        var player = PolymerUtils.getPlayer();

        if (entity instanceof PolymerEntity polymerEntity && InternalEntityHelpers.canPatchTrackedData(player, entity)) {
            var legalTrackedData = InternalEntityHelpers.getExampleTrackedDataOfEntityType((polymerEntity.getPolymerEntityType(player)));

            if (legalTrackedData.size() > 0) {
                if (this.trackedValues != null) {
                    for (DataTracker.Entry<?> entry : this.trackedValues) {
                        for (DataTracker.Entry<?> trackedData : legalTrackedData) {
                            if (trackedData.getData() == entry.getData()) {
                                entries.add(entry);
                                break;
                            }
                        }
                    }
                }

                polymerEntity.modifyTrackedData(entries, player);
            } else {
                if (this.trackedValues != null) {
                    for (DataTracker.Entry<?> entry : this.trackedValues) {
                        if (entry.getData().getId() <= 13) {
                            entries.add(entry);
                        }
                    }
                }

                polymerEntity.modifyTrackedData(entries, player);
            }
        } else if (this.trackedValues == null) {
            return null;
        } else {
            entries.addAll(this.trackedValues);
        }

        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);

            if (entry.getData() == ItemFrameEntityAccessor.getITEM_STACK() && entry.get() instanceof ItemStack stack) {
                var polymerStack = PolymerItemUtils.getPolymerItemStack(stack, PolymerUtils.getPlayer());

                if (!stack.hasCustomName() && !(stack.getItem() instanceof PolymerItem polymerItem && polymerItem.showDefaultNameInItemFrames())) {
                    polymerStack.removeCustomName();
                }

                entries.set(i, new DataTracker.Entry<>(ItemFrameEntityAccessor.getITEM_STACK(), stack));
            } else if (entry.getData() == AbstractMinecartEntityAccessor.getCUSTOM_BLOCK_ID()) {
                entries.set(i, new DataTracker.Entry<>(AbstractMinecartEntityAccessor.getCUSTOM_BLOCK_ID(), Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId((int) entry.get()), player))));
            }
            // Todo: Reintroduce this
            /*else if (entry.getData() == VillagerEntityAccessor.getVILLAGER_DATA() && entry.get() instanceof VillagerData data && data.getProfession() instanceof PolymerVillagerProfession polymerProf) {
                entries.set(i, new DataTracker.Entry<>(VillagerEntityAccessor.getVILLAGER_DATA(), new VillagerData(data.getType(), polymerProf.getPolymerProfession(player), data.getLevel())));
            }*/
        }

        return entries;
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;entriesToPacket(Ljava/util/List;Lnet/minecraft/network/PacketByteBuf;)V", ordinal = 0))
    private List<DataTracker.Entry<?>> polymer_replaceWithPolymer(List<DataTracker.Entry<?>> value) {
        return this.polymer_parseEntries();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getTrackedValues", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceItemsWithPolymerOnes(CallbackInfoReturnable<List<DataTracker.Entry<?>>> cir) {
        if (MinecraftClient.getInstance().getServer() != null && this.trackedValues != null) {
            var list = this.polymer_parseEntries();

            ServerPlayerEntity player = ClientUtils.getPlayer();

            for (int i = 0; i < list.size(); i++) {
                var entry = list.get(i);
                if (entry.get() instanceof Optional<?> optionalO && optionalO.isPresent()
                        && optionalO.get() instanceof BlockState state && state.getBlock() instanceof PolymerBlock polymerBlock) {
                    list.set(i, new DataTracker.Entry(entry.getData(), Optional.of(PolymerBlockUtils.getBlockStateSafely(polymerBlock, state, player))));
                }
            }

            cir.setReturnValue(list);
        }
    }
}
