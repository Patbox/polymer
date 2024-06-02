package eu.pb4.polymer.core.mixin.client.compat;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.search.EmiSearch;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
@Pseudo
@Mixin(value = ItemStack.class, priority = 499)
public abstract class emi_ItemStackMixin {
    /*@Inject(at = @At("RETURN"), method = "getTooltip")
    private void getTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
        try {
            if (EmiConfig.appendItemModId && EmiConfig.appendModId && Thread.currentThread() != EmiSearch.searchThread) {
                List<Text> text = info.getReturnValue();
                String namespace = EmiPort.getItemRegistry().getId(((ItemStack) (Object) this).getItem()).getNamespace();
                String mod = EmiUtil.getModName(namespace);
                var base = EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC);
                if (text.size() > 0) {

                }
            }
        } catch (Throwable e) {

        }
    }*/
}
