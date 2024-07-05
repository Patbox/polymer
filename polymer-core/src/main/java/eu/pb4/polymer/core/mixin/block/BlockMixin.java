package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Shadow
    @Final
    public static IdList<BlockState> STATE_IDS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polymer$enableMapping(CallbackInfo ci) {
        ((PolymerIdList<BlockState>) STATE_IDS).polymer$setChecker(
                x -> x.getBlock() instanceof PolymerObject,
                x -> PolymerImplUtils.isServerSideSyncableEntry((Registry<Object>) (Object) Registries.BLOCK, x.getBlock()),
                x -> "(Block) " + Registries.BLOCK.getId(x.getBlock())
        );
    }

    @Inject(method = "spawnBreakParticles", at = @At("HEAD"))
    private void addPolymerParticles(World world, PlayerEntity player, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer
                && PolymerBlockUtils.shouldMineServerSide(serverPlayer, pos, state)) {
            serverPlayer.networkHandler.sendPacket(new WorldEventS2CPacket(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state), false));
        }
    }
}
