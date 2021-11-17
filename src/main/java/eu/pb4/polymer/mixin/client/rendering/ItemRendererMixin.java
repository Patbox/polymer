package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.item.PolymerItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @ModifyVariable(method = {"getHeldItemModel", "renderGuiItemModel", "renderInGui", "renderGuiItemIcon", "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = @At("HEAD"), require = 0)
    private ItemStack polymer_replaceItemStack(ItemStack stack) {
        return stack.getItem() instanceof PolymerItem item ? item.getPolymerItemStack(stack, PolymerUtils.getPlayer()) : stack;
    }
}
