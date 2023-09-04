package eu.pb4.polymer.core.mixin.client.compat;

import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiScreenManager.class)
public class emi_EmiScreenManager {
    @Shadow private static MinecraftClient client;

    @Inject(method = "give", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"), require = 0, cancellable = true)
    private static void polymerCore$replaceWithServerItem(EmiStack eStack, int amount, int mode, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack stack) {
        try {
            if (CompatUtils.isServerSide(stack)) {
                var id = PolymerItemUtils.getServerIdentifier(stack);
                var nbt = CompatUtils.getBackingNbt(stack);
                String command = "give @s " + id;
                if (nbt != null) {
                    var nbtString = nbt.toString();
                    if (nbtString.length() + command.length() + 3 < 256) {
                        command += nbtString;
                    }
                }
                command += " " + amount;
                client.player.networkHandler.sendChatCommand(command);
                cir.setReturnValue(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
