package eu.pb4.polymer.mixin.client.compat;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.IModInfo;
import mcp.mobius.waila.api.IWailaConfig;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.config.PluginConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WailaClient.class)
public class wthit_WailaClientMixin {
    @Inject(method = "onItemTooltip", at = @At("HEAD"), cancellable = true)
    private static void polymer_overrideTooltip(ItemStack stack, List<Text> tooltip, CallbackInfo ci) {
        if (PluginConfig.INSTANCE.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
            if (stack.getItem() instanceof PolymerItem || !ClientUtils.isClientSide()) {
                ci.cancel();
                return;
            }

            var id = PolymerItemUtils.getPolymerIdentifier(stack);

            if (id != null) {
                String modName = null;
                var regBlock = Registry.ITEM.get(id);
                if (regBlock != null) {
                    modName = IModInfo.get(regBlock).getName();
                }

                if (modName == null || modName.isEmpty() || (modName.equals("Minecraft") && !id.getNamespace().equals("minecraft"))) {
                    modName = "Server";
                }

                tooltip.add(new LiteralText(IWailaConfig.get().getFormatting().formatModName(modName)));
                ci.cancel();
            }
        }
    }
}
