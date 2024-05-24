package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.ScreenHandlerPlayerContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler implements ScreenHandlerPlayerContext {
    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow public abstract void onContentChanged(Inventory inventory);
    @Unique
    private ServerPlayerEntity polymer$player;


    @ModifyArg(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getRawId(Ljava/lang/Object;)I"))
    private Object polymer$replaceEnchantment(@Nullable Object value) {
        if (value instanceof PolymerSyncedObject<?> polymerEnchantment) {
            return polymerEnchantment.getPolymerReplacement(this.polymer$player);
        } else {
            return value;
        }
    }

    /*@Inject(method = "onContentChanged", at = @At("TAIL"))
    private void syncOnContentChanged(Inventory inventory, CallbackInfo ci) {
        if (this.inventory == inventory && this.polymer$player != null) {
            this.syncState();
        }
    }*/

    @Override
    public void polymer$setPlayer(ServerPlayerEntity player) {
        this.polymer$player = player;
    }
}
