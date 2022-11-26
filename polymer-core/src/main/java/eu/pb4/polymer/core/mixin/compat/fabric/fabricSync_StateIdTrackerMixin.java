package eu.pb4.polymer.core.mixin.compat.fabric;

import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import net.fabricmc.fabric.impl.registry.sync.trackers.StateIdTracker;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StateIdTracker.class)
public class fabricSync_StateIdTrackerMixin {
    @Shadow @Final private IdList<?> stateList;

    @Inject(method = "recalcStateMap", at = @At("HEAD"), remap = false)
    private void polymer_clear(CallbackInfo ci) {
        ((PolymerIdList) this.stateList).polymer$setIgnoreCalls(true);
        ((PolymerIdList) this.stateList).polymer$clear();
    }

    @Inject(method = "recalcStateMap", at = @At("TAIL"), remap = false)
    private void polymer_unignore(CallbackInfo ci) {
        ((PolymerIdList) this.stateList).polymer$setIgnoreCalls(false);
    }
}
