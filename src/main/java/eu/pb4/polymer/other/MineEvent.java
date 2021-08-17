package eu.pb4.polymer.other;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class MineEvent {
    private List<EventHandler> handlers = new ArrayList<>();

    public void register(EventHandler event) {
        this.handlers.add(event);
    }

    public boolean invoke(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        for (EventHandler consumer : this.handlers) {
            boolean x = consumer.call(player, pos, state);
            if (x) {
                return true;
            }
        }
        return false;
    }


    public interface EventHandler {
        boolean call(ServerPlayerEntity player, BlockPos pos, BlockState state);
    }
}
