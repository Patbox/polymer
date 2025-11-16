package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.mixin.other.ScreenHandlerAccessor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;

@Mixin(StonecutterScreenHandler.class)
public abstract class StonecutterScreenHandlerMixin extends ScreenHandler {

    @Shadow private CuttingRecipeDisplay.Grouping<StonecuttingRecipe> availableRecipes;
    @Unique
    @Nullable
    private ServerPlayerEntity player;

    protected StonecutterScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void catchPlayer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        if (PolymerItemUtils.isStonecutterFixEnabled() && playerInventory.player instanceof ServerPlayerEntity player) {
            this.player = player;
            player.networkHandler.sendPacket(new SynchronizeRecipesS2CPacket(player.getEntityWorld().getRecipeManager().getPropertySets(), this.availableRecipes));
        }
    }

    @Inject(method = "updateInput", at = @At("TAIL"))
    private void sendClientUpdates(ItemStack stack, CallbackInfo ci) {
        if (!PolymerItemUtils.isStonecutterFixEnabled() || this.player == null) {
            return;
        }

        if (this.availableRecipes.isEmpty()) {
            player.networkHandler.sendPacket(new SynchronizeRecipesS2CPacket(player.getEntityWorld().getRecipeManager().getPropertySets(), this.availableRecipes));
        } else {
            var list = new ArrayList<CuttingRecipeDisplay.GroupEntry<StonecuttingRecipe>>();

            var clientItem = Ingredient.ofItem(stack.getItem());

            for (var x : this.availableRecipes.entries()) {
                list.add(new CuttingRecipeDisplay.GroupEntry<>(clientItem, x.recipe()));
            }

            player.networkHandler.sendPacket(new SynchronizeRecipesS2CPacket(player.getEntityWorld().getRecipeManager().getPropertySets(),
                    new CuttingRecipeDisplay.Grouping<>(list)));

        }



        var handler =  ((ScreenHandlerAccessor) this).getSyncHandler();
        handler.updateSlot(this, 0, ItemStack.EMPTY);
        handler.updateSlot(this, 0, stack);
    }
}
