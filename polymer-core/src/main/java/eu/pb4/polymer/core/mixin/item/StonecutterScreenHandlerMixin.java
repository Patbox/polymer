package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(StonecutterScreenHandler.class)
public class StonecutterScreenHandlerMixin {
    @Shadow
    @Final
    private World world;
    @Shadow private List<RecipeEntry<StonecuttingRecipe>> availableRecipes;
    @Unique
    private ServerPlayerEntity polymerCore$player;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void polymerCore$storePlayer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.polymerCore$player = playerInventory.player instanceof ServerPlayerEntity player ? player : null;
    }

    @Inject(method = "updateInput", at = @At("TAIL"))
    private void polymerCore$fixOrdering(Inventory input, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && this.polymerCore$player != null) {
            var list = new ArrayList<>(this.availableRecipes);

            list.sort(Comparator.comparing(
                    (recipe) -> PolymerItemUtils.getPolymerItemStack(recipe.value().getResult(this.world.getRegistryManager()), this.world.getRegistryManager(), this.polymerCore$player).getItem().getTranslationKey()
            ));
            this.availableRecipes = list;
        }
    }
}
