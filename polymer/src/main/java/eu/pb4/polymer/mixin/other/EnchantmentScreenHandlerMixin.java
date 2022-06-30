package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerEnchantment;
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
    private ServerPlayerEntity polymer_player;

    @ModifyArg(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I"))
    private Object polymer_replaceEnchantment(@Nullable Object value) {
        if (value instanceof PolymerEnchantment polymerEnchantment) {
            return polymerEnchantment.getPolymerEnchantment(this.polymer_player);
        } else {
            return value;
        }
    }

    @Override
    public void polymer_setPlayer(ServerPlayerEntity player) {
        this.polymer_player = player;
    }
}
