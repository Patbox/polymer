package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.ScreenHandlerPlayerContext;
import eu.pb4.polymer.core.impl.interfaces.ServerPlayerExtension;
import eu.pb4.polymer.core.impl.other.world.PlayerEyedWorld;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerExtension {

    @Unique
    private PlayerEyedWorld polymerCore$playerWorld;

    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void polymer$setPlayerContext(ScreenHandler screenHandler, CallbackInfo ci) {
        if (screenHandler instanceof ScreenHandlerPlayerContext context) {
            context.polymer$setPlayer((ServerPlayerEntity) (Object) this);
        }
    }

    @Inject(method = "setServerWorld", at = @At("HEAD"))
    private void polymerCore$clearWorld(ServerWorld world, CallbackInfo ci) {
        this.polymerCore$playerWorld = null;
    }

    @Override
    public PlayerEyedWorld polymerCore$getOrCreatePlayerEyedWorld() {
        if (this.polymerCore$playerWorld == null) {
            //noinspection ConstantConditions
            this.polymerCore$playerWorld = new PlayerEyedWorld((ServerPlayerEntity) (Object) this);
        }
        return this.polymerCore$playerWorld;
    }

    @Override
    public @Nullable PlayerEyedWorld polymerCore$getPlayerEyedWorld() {
        return this.polymerCore$playerWorld;
    }
}
