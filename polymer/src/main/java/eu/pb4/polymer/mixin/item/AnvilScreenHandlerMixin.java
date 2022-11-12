package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow private String newItemName;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }
    
    @Inject(method = "setNewItemName", at = @At("HEAD"), cancellable = true)
    private void polymer_ignoreIncorrectAnvilInput(String newItemName, CallbackInfo ci) {
        if (this.player instanceof ServerPlayerEntity serverPlayer && !StringUtils.isBlank(newItemName)) {
            var stack = this.getSlot(0).getStack();
            if (stack.getItem() instanceof PolymerObject && !stack.hasCustomName()
                    && Objects.equals(newItemName, ServerTranslationUtils.parseFor(serverPlayer.networkHandler, stack.getName()).getString())) {
                this.newItemName = null;
                this.updateResult();
                ci.cancel();
            }
        }
    }
}
