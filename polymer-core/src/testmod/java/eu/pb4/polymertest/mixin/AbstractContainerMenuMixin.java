package eu.pb4.polymertest.mixin;


import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import eu.pb4.polymertest.TestScrollableItem;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @WrapWithCondition(method = "setSelectedBundleItemIndex", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;toggleSelectedItem(Lnet/minecraft/world/item/ItemStack;I)V"))
    private boolean redirect(ItemStack stack, int selectedItem) {
        if (stack.getItem() instanceof TestScrollableItem item) {
            item.handleScroll(stack, selectedItem);
            return false;
        }

        return true;
    }
}
