package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.core.impl.interfaces.PolymerGamePacketListenerExtension;
import eu.pb4.polymer.core.impl.other.BlockMapperImpl;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Do not use, unless you really need it, and you are 100% sure about what you need!
 *
 * Allows changing how blocks display for certain players.
 * You can replace any block that way, including vanilla ones.
 *
 * To only change your own blocks see {@link PolymerBlock}
 */
public interface BlockMapper {
    SimpleEvent<BiFunction<PacketContext, BlockMapper, @Nullable BlockMapper>> DEFAULT_MAPPER_EVENT = new SimpleEvent<>();

    BlockState toClientSideState(BlockState state, PacketContext context);
    String getMapperName();

    static BlockMapper createDefault() {
        return BlockMapperImpl.DEFAULT;
    }

    static BlockMapper getDefault(PacketContext context) {
        var obj = new MutableObject<>(BlockMapperImpl.DEFAULT);
        DEFAULT_MAPPER_EVENT.invoke((c) -> {
             var mapper = c.apply(context, obj.getValue());

             if (mapper != null) {
                 obj.setValue(mapper);
             }
        });

        return obj.getValue();
    }

    static BlockMapper createMap(Map<BlockState, BlockState> stateMap) {
        return BlockMapperImpl.getMap(stateMap);
    }

    static BlockMapper createStack(BlockMapper overlay, BlockMapper base) {
        return BlockMapperImpl.createStack(overlay, base);
    }

    static BlockMapper getFrom(PacketContext context) {
        return getFrom(PolymerCommonUtils.getPlayer(context));
    }
    static BlockMapper getFrom(@Nullable ServerPlayer player) {
        return player != null ? PolymerGamePacketListenerExtension.of(player).polymer$getBlockMapper() : BlockMapper.createDefault();
    }

    static void resetMapper(@Nullable ServerPlayer player) {
        if (player != null) {
            PolymerGamePacketListenerExtension.of(player).polymer$setBlockMapper(getDefault(player.connection.getPacketContext()));
        }
    }

    static void set(ServerGamePacketListenerImpl handler, BlockMapper mapper) {
        PolymerGamePacketListenerExtension.of(handler).polymer$setBlockMapper(mapper);
    }

    static BlockMapper get(ServerGamePacketListenerImpl handler) {
        return PolymerGamePacketListenerExtension.of(handler).polymer$getBlockMapper();
    }
}
