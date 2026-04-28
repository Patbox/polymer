package eu.pb4.polymer.core.mixin.client.item.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(StackedItemContents.class)
public class RecipeFinderMixin {
    @Inject(method = "accountStack(Lnet/minecraft/world/item/ItemStack;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;account(Ljava/lang/Object;I)V"))
    private void passStack(ItemStack item, int maxCount, CallbackInfo ci) {

    }
}
