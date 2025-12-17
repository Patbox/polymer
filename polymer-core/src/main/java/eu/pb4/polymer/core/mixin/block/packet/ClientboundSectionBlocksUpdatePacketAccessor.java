package eu.pb4.polymer.core.mixin.block.packet;

import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
public interface ClientboundSectionBlocksUpdatePacketAccessor {
    @Accessor("sectionPos")
    SectionPos polymer_getSectionPos();

    @Accessor("positions")
    short[] polymer_getPositions();

    @Accessor("states")
    BlockState[] polymer_getBlockStates();
}
