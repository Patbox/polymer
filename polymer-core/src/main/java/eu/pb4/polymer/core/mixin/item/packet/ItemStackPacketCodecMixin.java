package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.mixin.item.ComponentChangesAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

@Mixin(targets = "net/minecraft/item/ItemStack$1", priority = 500)
public abstract class ItemStackPacketCodecMixin {

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ItemStack polymer$replaceWithVanillaItem(ItemStack itemStack, @Local(argsOnly = true) RegistryByteBuf buf) {
        var player = PacketContext.get();
        return PolymerItemUtils.getPolymerItemStack(itemStack, player);
    }

    @ModifyArg(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 1), index = 1)
    private Object polymer$addSyncedDefaults(Object object, @Local(argsOnly = true) ItemStack stack) {
        var changedDefaults = PolymerItemUtils.getSyncedDefaultComponents(stack.getItem());
        if (changedDefaults.isEmpty()) {
            return object;
        }
        var original = ((ComponentChangesAccessor) object).getChangedComponents();
        var changes = new Reference2ObjectOpenHashMap<ComponentType<?>, Optional<?>>(changedDefaults.size() + original.size());
        changes.putAll(original);
        for (var type : changedDefaults) {
            if (!changes.containsKey(type)) {
                changes.put(type, Optional.ofNullable(stack.getItem().getComponents().get(type)));
            }
        }

        return ComponentChangesAccessor.createComponentChanges(changes);
    }
    @ModifyReturnValue(method = "decode(Lnet/minecraft/network/RegistryByteBuf;)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN", ordinal = 1))
    private ItemStack polymerCore$decodeItemStackServer(ItemStack stack, @Local(argsOnly = true) RegistryByteBuf buf) {
        return PolymerCommonUtils.isServerNetworkingThread() ? PolymerItemUtils.getRealItemStack(stack, buf.getRegistryManager()) : stack;
    }
}