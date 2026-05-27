package eu.pb4.polymer.blocks.api;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.Optional;


public interface PolymerTexturedHeadBlock extends PolymerTexturedBlock {
    ResolvableProfile EMPTY_TEXTURE = PolymerUtils.createProfileComponent(new PlayerSkin.Patch(Optional.of(
            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "block/empty"))), Optional.empty(), Optional.empty(), Optional.empty()));

    @Override
    default void onPolymerBlockSend(BlockState blockState, BlockPos.MutableBlockPos pos, ServerPlayer context) {
        CompoundTag main = new CompoundTag();
        main.putString("id", "minecraft:skull");
        main.put("profile", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, EMPTY_TEXTURE).result().get());
        main.putInt("x", pos.getX());
        main.putInt("y", pos.getY());
        main.putInt("z", pos.getZ());
        context.connection.send(PolymerBlockUtils.createBlockEntityPacket(pos.immutable(), BlockEntityTypes.SKULL, main));
    }
}
