package eu.pb4.polymer.patches.mixin;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all"})
@Pseudo
@Mixin(targets = "tfar/dankstorage/network/DankPacketHandler", remap = false)
public class DankPacketHandlerMixin {
    @Unique
    private static ServerPlayerEntity tempPlayer;

    // sendSyncSlot

    @Inject(method = "sendSyncSlot", at = @At("HEAD"))
    private static void polymer_storePlayer1(ServerPlayerEntity player, int id, int slot, ItemStack stack, CallbackInfo ci) {
        tempPlayer = player;
    }

    @ModifyArg(method = "sendSyncSlot", at = @At(value = "INVOKE", target = "Ltfar/dankstorage/utils/PacketBufferEX;writeExtendedItemStack(Lnet/minecraft/class_2540;Lnet/minecraft/class_1799;)V"))
    private static ItemStack polymer_modifyStack1(ItemStack stack) {
        return PolymerItemUtils.getPolymerItemStack(stack, tempPlayer);
    }

    @Inject(method = "sendSyncSlot", at = @At("TAIL"))
    private static void polymer_removePlayer1(ServerPlayerEntity player, int id, int slot, ItemStack stack, CallbackInfo ci) {
        tempPlayer = null;
    }

    // sendSelectedItem

    @Inject(method = "sendSelectedItem", at = @At("HEAD"))
    private static void polymer_storePlayer2(ServerPlayerEntity player, ItemStack stack, CallbackInfo ci) {
        tempPlayer = player;
    }

    @ModifyArg(method = "sendSelectedItem", at = @At(value = "INVOKE", target = "Ltfar/dankstorage/utils/PacketBufferEX;writeExtendedItemStack(Lnet/minecraft/class_2540;Lnet/minecraft/class_1799;)V"))
    private static ItemStack polymer_modifyStack2(ItemStack stack) {
        return PolymerItemUtils.getPolymerItemStack(stack, tempPlayer);
    }

    @Inject(method = "sendSelectedItem", at = @At("TAIL"))
    private static void polymer_removePlayer2(ServerPlayerEntity player, ItemStack stack, CallbackInfo ci) {
        tempPlayer = null;
    }

    // sendList

    @Inject(method = "sendList", at = @At("HEAD"))
    private static void polymer_storePlayer3(ServerPlayerEntity player, List<ItemStack> stacks, CallbackInfo ci) {
        tempPlayer = player;
    }

    @ModifyArg(method = "sendList", at = @At(value = "INVOKE", target = "Ltfar/dankstorage/utils/PacketBufferEX;writeList(Lnet/minecraft/class_2540;Ljava/util/List;)V"))
    private static List<ItemStack> polymer_modifyStack3(List<ItemStack> stacks) {
        var list = new ArrayList<ItemStack>();

        for (var stack : stacks) {
            list.add(PolymerItemUtils.getPolymerItemStack(stack, tempPlayer));
        }

        return null;
    }

    @Inject(method = "sendList", at = @At("TAIL"))
    private static void polymer_removePlayer3(ServerPlayerEntity player, List<ItemStack> stacks, CallbackInfo ci) {
        tempPlayer = null;
    }

    // sendSyncContainer

    @Inject(method = "sendSyncContainer", at = @At("HEAD"))
    private static void polymer_storePlayer4(ServerPlayerEntity player, int i, int stateID, DefaultedList<ItemStack> containerID, ItemStack stacks, CallbackInfo ci) {
        tempPlayer = player;
    }

    @ModifyArg(method = "sendSyncContainer", at = @At(value = "INVOKE", target = "Ltfar/dankstorage/utils/PacketBufferEX;writeExtendedItemStack(Lnet/minecraft/class_2540;Lnet/minecraft/class_1799;)V"))
    private static ItemStack polymer_modifyStack4(ItemStack stack) {
        return PolymerItemUtils.getPolymerItemStack(stack, tempPlayer);
    }

    @ModifyArg(method = "sendSyncContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2540;method_10793(Lnet/minecraft/class_1799;)Lnet/minecraft/class_2540;"))
    private static ItemStack polymer_modifyStack4_2(ItemStack stack) {
        return PolymerItemUtils.getPolymerItemStack(stack, tempPlayer);
    }

    @Inject(method = "sendSyncContainer", at = @At("TAIL"))
    private static void polymer_removePlayer4(ServerPlayerEntity player, int i, int stateID, DefaultedList<ItemStack> containerID, ItemStack stacks, CallbackInfo ci) {
        tempPlayer = null;
    }
}
