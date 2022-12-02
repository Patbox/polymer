package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class CoreClientUtils {
    public static ItemStack getRenderingStack(ItemStack stack) {
        if (stack.getItem() instanceof VirtualClientItem virtualItem) {
            var og = stack;

            stack = virtualItem.getPolymerEntry().visualStack().copy();
            stack.setCount(og.getCount());
        }

        return stack.getItem() instanceof PolymerItem item && !PolymerKeepModel.is(item) ? item.getPolymerItemStack(stack, PolymerUtils.getTooltipContext(ClientUtils.getPlayer()), ClientUtils.getPlayer()) : stack;
    }
}
