package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.networking.ClientPacketBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public HitResult crosshairTarget;

    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();

    @ModifyVariable(method = "initializeSearchableContainers", at = @At(value = "STORE", ordinal = 0))
    private DefaultedList<?> polymer_removePolymerItemsFromSearch(DefaultedList<?> og) {
        return new DefaultedList<>(new ArrayList<>(), null) {
            @Override
            public void add(int value, Object element) {
                if (element instanceof ItemStack stack && !PolymerItemUtils.isPolymerServerItem(stack)) {
                    super.add(value, element);
                }
            }
        };
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    private void polymer_onDisconnect(CallbackInfo ci) {
        InternalClientRegistry.disable();
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void polymer_pickBlock(CallbackInfo ci) {
        if (InternalClientRegistry.ENABLED && this.getNetworkHandler() != null) {
            if (this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                var pos = ((BlockHitResult)this.crosshairTarget).getBlockPos();

                if (InternalClientRegistry.getBlockAt(pos) != null) {
                    ClientPacketBuilder.sendPickBlock(this.getNetworkHandler(), pos);
                    ci.cancel();
                }
            }
        }
    }
}
