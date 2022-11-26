package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatisticsS2CPacket.class)
public class StatisticsS2CPacketMixin {

    @Mutable
    @Shadow @Final private Object2IntMap<Stat<?>> stats;

    @Inject(method = "<init>(Lit/unimi/dsi/fastutil/objects/Object2IntMap;)V", at = @At("TAIL"))
    public void polymer$onWrite(Object2IntMap<Stat<?>> stats, CallbackInfo ci) {
        this.stats = stats.object2IntEntrySet().stream().filter(statEntry -> {
            if (statEntry.getKey().getType() == Stats.CUSTOM) {
                Identifier key = (Identifier) statEntry.getKey().getValue();
                return !(key instanceof PolymerObject);
            }
            return true;
        }).collect(Object2IntOpenHashMap::new, (map, statEntry) -> map.addTo(statEntry.getKey(), statEntry.getIntValue()), Object2IntOpenHashMap::putAll);
    }
}
