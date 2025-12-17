package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundAwardStatsPacket.class)
public abstract class ClientboundAwardStatsPacketMixin {

    @Mutable
    @Shadow @Final private Object2IntMap<Stat<?>> stats;

    @Shadow public abstract boolean equals(Object par1);

    @Inject(method = "<init>(Lit/unimi/dsi/fastutil/objects/Object2IntMap;)V", at = @At("TAIL"))
    public void polymer$onWrite(Object2IntMap<Stat<?>> stats, CallbackInfo ci) {
        //noinspection RedundantSuppression
        this.stats = stats.object2IntEntrySet().stream().filter(statEntry -> {
            //noinspection unchecked,rawtypes
            return !RegistrySyncUtils.isServerEntry((Registry) statEntry.getKey().getType().getRegistry(), statEntry.getKey().getValue());
        }).collect(Object2IntOpenHashMap::new, (map, statEntry) -> map.addTo(statEntry.getKey(), statEntry.getIntValue()), Object2IntOpenHashMap::putAll);
    }
}
