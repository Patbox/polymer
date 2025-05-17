package eu.pb4.polymer.soundpatcher.impl;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import xyz.nucleoid.packettweaker.PacketContext;

public class CoreBridge {
    public static BlockSoundGroup getClientSideSoundGroup(BlockState state, PacketContext context) {
        if (CompatStatus.POLYMER_CORE) {
            state = PolymerBlockUtils.getServerSideBlockState(state, context);
        }

        return state.getSoundGroup();
    }

    public static BlockSoundGroup getClientSideSoundGroupBreaking(BlockState state, PacketContext context) {
        if (CompatStatus.POLYMER_CORE) {
            if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, state.getBlock()) instanceof PolymerBlock polymerBlock) {
                state = PolymerBlockUtils.getBlockBreakBlockStateSafely(polymerBlock, state,
                        PolymerBlockUtils.NESTED_DEFAULT_DISTANCE, context);
            }
            state = PolymerBlockUtils.getServerSideBlockState(state, context);
        }

        return state.getSoundGroup();
    }
}
