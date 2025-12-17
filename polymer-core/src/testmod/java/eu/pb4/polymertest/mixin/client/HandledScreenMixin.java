package eu.pb4.polymertest.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
    /*@Shadow protected abstract void drawForeground(DrawContext context, int mouseX, int mouseY);

    @Redirect(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V"))
    private void dontDrawForeground(HandledScreen instance, DrawContext context, int mouseX, int mouseY) {}

    @Inject(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlightBack(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    private void actuallyDrawForeground(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        this.drawForeground(context, mouseX, mouseY);
    }*/
}
