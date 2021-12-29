package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.impl.interfaces.PlayerAwarePacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
        ClickSlotC2SPacket.class,
        CreativeInventoryActionC2SPacket.class,
        EntityEquipmentUpdateS2CPacket.class,
        InventoryS2CPacket.class,
        ScreenHandlerSlotUpdateS2CPacket.class,
        SetTradeOffersS2CPacket.class
})
public class PacketMarkPlayerAwareMixin implements PlayerAwarePacket {
}
