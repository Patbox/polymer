package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.impl.interfaces.ScreenHandlerPlayerContext;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin implements ScreenHandlerPlayerContext {
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

    @Override
    public void polymer$setPlayer(ServerPlayerEntity player) {
        this.polymer$player = player;
    }
}
