package eu.pb4.polymer.soundpatcher.impl;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class CoreBridge {
    public static SoundType getClientSideSoundGroup(BlockState state, PacketContext context) {
        if (CompatStatus.POLYMER_CORE) {
            state = PolymerBlockUtils.getServerSideBlockState(state, context);
        }

        return state.getSoundType();
    }

    public static SoundType getClientSideSoundGroupBreaking(BlockState state, PacketContext context) {
        if (CompatStatus.POLYMER_CORE) {
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock polymerBlock) {
                state = PolymerBlockUtils.getBlockBreakBlockStateSafely(polymerBlock, state,
                        PolymerBlockUtils.NESTED_DEFAULT_DISTANCE, context);
            }
            state = PolymerBlockUtils.getServerSideBlockState(state, context);
        }

        return state.getSoundType();
    }
}
