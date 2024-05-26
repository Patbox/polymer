package eu.pb4.polymer.core.mixin.client.compat;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.plugin.harvest.provider.HarvestProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HarvestProvider.class)
public class wthit_HarvestProviderMixin {

    @Inject(method = "appendBody", at = @At("HEAD"), remap = false, cancellable = true)
    private void polymer$disableHarvestTooltip(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config, CallbackInfo ci) {
        if (InternalClientRegistry.getBlockAt(accessor.getPosition()) != ClientPolymerBlock.NONE_STATE) ci.cancel();
    }

}
