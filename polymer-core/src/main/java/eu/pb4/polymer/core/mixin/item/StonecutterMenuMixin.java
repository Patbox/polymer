package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.mixin.other.AbstractContainerMenuAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

@Mixin(StonecutterMenu.class)
public abstract class StonecutterMenuMixin extends AbstractContainerMenu {

    @Shadow private SelectableRecipe.SingleInputSet<StonecutterRecipe> recipesForInput;
    @Unique
    @Nullable
    private ServerPlayer player;

    protected StonecutterMenuMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void catchPlayer(int syncId, Inventory playerInventory, ContainerLevelAccess context, CallbackInfo ci) {
        if (PolymerItemUtils.isStonecutterFixEnabled() && playerInventory.player instanceof ServerPlayer player) {
            this.player = player;
            player.connection.send(new ClientboundUpdateRecipesPacket(player.level().recipeAccess().getSynchronizedItemProperties(), this.recipesForInput));
        }
    }

    @Inject(method = "setupRecipeList", at = @At("TAIL"))
    private void sendClientUpdates(ItemStack stack, CallbackInfo ci) {
        if (!PolymerItemUtils.isStonecutterFixEnabled() || this.player == null) {
            return;
        }

        if (this.recipesForInput.isEmpty()) {
            player.connection.send(new ClientboundUpdateRecipesPacket(player.level().recipeAccess().getSynchronizedItemProperties(), this.recipesForInput));
        } else {
            var list = new ArrayList<SelectableRecipe.SingleInputEntry<StonecutterRecipe>>();

            var clientItem = Ingredient.of(PolymerItemUtils.getClientItemStack(stack, PacketContext.create(this.player)).getItem());

            for (var x : this.recipesForInput.entries()) {
                list.add(new SelectableRecipe.SingleInputEntry<>(clientItem, x.recipe()));
            }

            player.connection.send(new ClientboundUpdateRecipesPacket(player.level().recipeAccess().getSynchronizedItemProperties(),
                    new SelectableRecipe.SingleInputSet<>(list)));

        }



        var handler =  ((AbstractContainerMenuAccessor) this).getSynchronizer();
        handler.sendSlotChange(this, 0, ItemStack.EMPTY);
        handler.sendSlotChange(this, 0, stack);
    }
}
