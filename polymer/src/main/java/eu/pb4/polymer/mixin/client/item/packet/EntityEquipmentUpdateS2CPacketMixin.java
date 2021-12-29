package eu.pb4.polymer.mixin.client.item.packet;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(EntityEquipmentUpdateS2CPacket.class)
public class EntityEquipmentUpdateS2CPacketMixin {
    @Environment(EnvType.CLIENT)
    @Inject(method = "getEquipmentList", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceItemsWithVirtualOnes(CallbackInfoReturnable<List<Pair<EquipmentSlot, ItemStack>>> cir) {
        if (MinecraftClient.getInstance().getServer() != null) {
            List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
            ServerPlayerEntity player = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid());

            for (Pair<EquipmentSlot, ItemStack> pair : cir.getReturnValue()) {
                list.add(new Pair<>(pair.getFirst(), PolymerItemUtils.getPolymerItemStack(pair.getSecond(), player)));
            }

            cir.setReturnValue(list);
        }
    }

}
