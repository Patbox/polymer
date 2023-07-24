package eu.pb4.polymer.core.impl.other.world;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;

@SuppressWarnings("unchecked")
public class PlayerEyedWorld extends VirtualWorld {
    private final ServerPlayerEntity player;


    public PlayerEyedWorld(ServerPlayerEntity player) {
        super(player.getServerWorld());
        this.player = player;
    }


    public void clearUpdates() {
        /*if (this.sections.isEmpty()) {
            this.stateMap.clear();
            return;
        }
        this.sections.forEach(((aLong, state) -> {



            //this.world.getChunkManager().markForUpdate(mut.set(aLong));
        }));
        this.sections.clear();
        this.stateMap.clear();*/
    }

    @Override
    protected BlockState getContextState(BlockState x) {
        return PolymerBlockUtils.getPolymerBlockState(x, this.player);
    }
}
