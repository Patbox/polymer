package eu.pb4.polymer.core.mixin.item.packet;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.interfaces.GenericPlayerContext;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/world/inventory/RemoteSlot$Synchronized")
public class RemoteSlotSynchronizedMixin implements GenericPlayerContext {
    @Unique
    private PacketContext context = PacketContext.create();

    @Override
    public void polymer$setPlayer(ServerPlayer player) {
        this.context = PacketContext.create(player);
    }

    @ModifyArg(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/HashedStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/network/HashedPatchMap$HashGenerator;)Z"))
    private ItemStack polymerifyCheckedStack(ItemStack stack) {
        if (PolymerItemUtils.isServerItem(stack, this.context) && this.context.getPlayer() != null) {
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.context.getPlayer().registryAccess());
            PolymerCommonUtils.executeWithNetworkingLogic(context.getBackingPacketListener(), () -> ItemStack.STREAM_CODEC.encode(buf, stack));
            return ItemStack.STREAM_CODEC.decode(buf);
        }
        return stack;
    }
}
