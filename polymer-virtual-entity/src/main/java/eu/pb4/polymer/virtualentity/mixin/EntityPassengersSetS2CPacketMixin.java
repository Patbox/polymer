package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Mixin(EntityPassengersSetS2CPacket.class)
public class EntityPassengersSetS2CPacketMixin {
    @Shadow @Mutable
    private int[] passengerIds;
    @Unique
    private final List<Pair<Collection<ServerPlayNetworkHandler>, IntList>> virtualPassengers = new ArrayList<>();

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At(value = "TAIL"))
    private void polymerVE$addExtraPassangers(Entity entity, CallbackInfo ci) {
        var virt = ((EntityExt) entity).polymerVE$getVirtualRidden();
        if (!virt.isEmpty()) {
            var old = this.passengerIds;
            this.passengerIds = Arrays.copyOf(this.passengerIds, old.length + virt.size());
            for (int i = 0; i < virt.size(); i++) {
                this.passengerIds[i + old.length] = virt.getInt(i);
            }
        }

        for (var holder : ((HolderAttachmentHolder) entity).polymerVE$getHolders()) {
            var x = holder.holder().getAttachedPassengerEntityIds();
            if (!x.isEmpty()) {
                this.virtualPassengers.add(new Pair<>(holder.holder().getWatchingPlayers(), x));
            }
        }
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeIntArray([I)Lnet/minecraft/network/PacketByteBuf;"))
    private int[] addDynamicPassengers(int[] a) {
        if (this.virtualPassengers.isEmpty()) {
            return a;
        }
        var player = PolymerCommonUtils.getPlayerContext();
        if (player == null) {
            return a;
        }

        var arr = new IntArrayList(a);

        for (var x : this.virtualPassengers) {
            if (x.getLeft().contains(player.networkHandler)) {
                arr.addAll(x.getRight());
            }
        }

        return arr.toIntArray();
    }
}
