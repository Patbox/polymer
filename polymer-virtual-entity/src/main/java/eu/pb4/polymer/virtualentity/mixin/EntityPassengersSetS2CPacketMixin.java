package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;

@Mixin(ClientboundSetPassengersPacket.class)
public class EntityPassengersSetS2CPacketMixin {
    @Shadow @Mutable
    private int[] passengers;
    @Unique
    private final List<Tuple<Collection<ServerGamePacketListenerImpl>, IntList>> virtualPassengers = new ArrayList<>();

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "TAIL"))
    private void polymerVE$addExtraPassangers(Entity entity, CallbackInfo ci) {
        var virt = ((EntityExt) entity).polymerVE$getVirtualRidden();
        if (!virt.isEmpty()) {
            var old = this.passengers;
            this.passengers = Arrays.copyOf(this.passengers, old.length + virt.size());
            for (int i = 0; i < virt.size(); i++) {
                this.passengers[i + old.length] = virt.getInt(i);
            }
        }

        for (var holder : ((HolderAttachmentHolder) entity).polymerVE$getHolders()) {
            var x = holder.holder().getAttachedPassengerEntityIds();
            if (!x.isEmpty()) {
                this.virtualPassengers.add(new Tuple<>(holder.holder().getWatchingPlayers(), x));
            }
        }
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeVarIntArray([I)Lnet/minecraft/network/FriendlyByteBuf;"))
    private int[] addDynamicPassengers(int[] a) {
        // This can be null due to Unsafe!
        //noinspection ConstantValue
        if (this.virtualPassengers == null || this.virtualPassengers.isEmpty()) {
            return a;
        }
        var player = PacketContext.get();
        if (player.getPlayer() == null) {
            return a;
        }

        var arr = new IntArrayList(a);

        for (var x : this.virtualPassengers) {
            if (x.getA().contains(player.getPlayer().connection)) {
                arr.addAll(x.getB());
            }
        }

        return arr.toIntArray();
    }
}
