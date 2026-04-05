package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.mixin.item.DataComponentPatchAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

@Mixin(targets = "net/minecraft/world/item/ItemStack$1", priority = 500)
public abstract class ItemStackPacketCodecMixin {

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ItemStack polymer$replaceWithVanillaItem(ItemStack itemStack, @Local(argsOnly = true) RegistryFriendlyByteBuf buf) {
        var player = PacketContext.orElseThrow();
        return PolymerItemUtils.getPolymerItemStack(itemStack, player, buf.registryAccess());
    }

    @ModifyArg(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 1), index = 1)
    private Object polymer$addSyncedDefaults(Object object, @Local(argsOnly = true) ItemStack stack) {
        var changedDefaults = PolymerItemUtils.getSyncedDefaultComponents(stack.getItem());
        if (changedDefaults.isEmpty()) {
            return object;
        }
        var original = ((DataComponentPatchAccessor) object).getMap();
        var changes = new Reference2ObjectOpenHashMap<DataComponentType<?>, Optional<?>>(changedDefaults.size() + original.size());
        changes.putAll(original);
        for (var type : changedDefaults) {
            if (!changes.containsKey(type)) {
                changes.put(type, Optional.ofNullable(stack.getItem().components().get(type)));
            }
        }

        return DataComponentPatchAccessor.createComponentChanges(changes);
    }
    @ModifyReturnValue(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN", ordinal = 1))
    private ItemStack polymerCore$decodeItemStackServer(ItemStack stack, @Local(argsOnly = true) RegistryFriendlyByteBuf buf) {
        return PolymerCommonUtils.isServerNetworkingThread() ? PolymerItemUtils.getRealItemStack(stack, buf.registryAccess()) : stack;
    }
}