package eu.pb4.polymer.soundpatcher.impl;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class CoreBridge {
    public static SoundType getClientSideSoundGroup(BlockState state, Entity entity) {
        if (CompatStatus.POLYMER_CORE && entity instanceof ServerPlayer player) {
            state = PolymerBlockUtils.getPolymerBlockState(state, player.connection.getPacketContext());
        }

        return state.getSoundType();
    }

    public static SoundType getClientSideSoundGroupBreaking(BlockState state, Entity entity) {
        if (CompatStatus.POLYMER_CORE && entity instanceof ServerPlayer player) {
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock polymerBlock) {
                state = PolymerBlockUtils.getBlockBreakBlockStateSafely(polymerBlock, state,
                        PolymerBlockUtils.NESTED_DEFAULT_DISTANCE, player.connection.getPacketContext());
            }
            state = PolymerBlockUtils.getPolymerBlockState(state, player.connection.getPacketContext());
        }

        return state.getSoundType();
    }
}
