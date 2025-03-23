package eu.pb4.polymer.core.mixin.item.packet;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.interfaces.GenericPlayerContext;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/screen/sync/TrackedSlot$Impl")
public class TrackedSlotImplMixin implements GenericPlayerContext {
    @Unique
    private PacketContext context = PacketContext.create();

    @Override
    public void polymer$setPlayer(ServerPlayerEntity player) {
        this.context = PacketContext.create(player);
    }

    @ModifyArg(method = "isInSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/sync/ItemStackHash;hashEquals(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/sync/ComponentChangesHash$ComponentHasher;)Z"))
    private ItemStack polymerifyCheckedStack(ItemStack stack) {
        if (PolymerItemUtils.isServerItem(stack, this.context) && this.context.getPlayer() != null) {
            var buf = new RegistryByteBuf(Unpooled.buffer(), this.context.getPlayer().getRegistryManager());
            PolymerCommonUtils.executeWithNetworkingLogic(context.getBackingPacketListener(), () -> ItemStack.PACKET_CODEC.encode(buf, stack));
            return ItemStack.PACKET_CODEC.decode(buf);
        }
        return stack;
    }
}
