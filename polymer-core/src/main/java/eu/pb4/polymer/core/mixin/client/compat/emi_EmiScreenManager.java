package eu.pb4.polymer.core.mixin.client.compat;
/*
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.world.item.ItemStack;

@Pseudo
@Mixin(EmiScreenManager.class)
public class emi_EmiScreenManager {
    @Shadow private static Minecraft client;

    @Inject(method = "give", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;"), require = 0, cancellable = true)
    private static void polymerCore$replaceWithServerItem(EmiStack eStack, int amount, int mode, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack stack) {
        try {
            if (CompatUtils.isServerSide(stack)) {
                var id = PolymerItemUtils.getServerIdentifier(stack);
                var comp = CompatUtils.getBackingComponents(stack);
                var command = new StringBuilder("give @s " + id);
                if (comp != null && !comp.isEmpty()) {
                    command.append('[');
                    for (var e : comp.entrySet()) {
                        command.append(e.getKey().toString());
                        command.append('=');
                        command.append(new SnbtPrinterTagVisitor("", 0, new ArrayList<>()).visit(e.getValue()));
                    }
                    command.append(']');
                }

                if (command.length() > 256) {
                    command = new StringBuilder("give @s " + id);
                }
                if (amount != 1) {
                command.append(" ").append(amount);
                }
                client.player.connection.sendCommand(command.toString());
                cir.setReturnValue(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
*/