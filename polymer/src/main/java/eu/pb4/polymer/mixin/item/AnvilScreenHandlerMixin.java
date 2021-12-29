package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    private String originalItemName;

    @Shadow private String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }
    
    @Inject(method = "setNewItemName", at = @At("HEAD"), cancellable = true)
    private void polymer_ignoreIncorrectAnvilInput(String newItemName, CallbackInfo ci) {
        if (this.originalItemName == null) {
            this.originalItemName = newItemName;
        }

        if (this.getSlot(0).getStack().getItem() instanceof PolymerObject && !this.getSlot(0).getStack().hasCustomName() && Objects.equals(newItemName, this.originalItemName)) {
            this.newItemName = null;
            this.updateResult();
            ci.cancel();
        }
    }
}
