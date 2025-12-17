package eu.pb4.polymertest.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommonPlayerSpawnInfo.class)
public class CommonPlayerSpawnInfoMixin {
    //@ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryByteBuf;writeBoolean(Z)Lnet/minecraft/network/PacketByteBuf;"))
    //private boolean itsDebugWorld(boolean v) {
    //    return true;
    //}
}
