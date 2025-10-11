package eu.pb4.polymer.blocks.api;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;
import java.util.Optional;


public interface PolymerTexturedHeadBlock extends PolymerTexturedBlock {
    ProfileComponent EMPTY_TEXTURE = PolymerUtils.createProfileComponent(new SkinTextures.SkinOverride(Optional.of(
            new AssetInfo.TextureAssetInfo(Identifier.of("polymer", "block/empty"))), Optional.empty(), Optional.empty(), Optional.empty()));

    @Override
    default void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, PacketContext.NotNullWithPlayer context) {
        NbtCompound main = new NbtCompound();
        main.putString("id", "minecraft:skull");
        main.put("profile", ProfileComponent.CODEC.encodeStart(NbtOps.INSTANCE, EMPTY_TEXTURE).result().get());
        main.putInt("x", pos.getX());
        main.putInt("y", pos.getY());
        main.putInt("z", pos.getZ());
        Objects.requireNonNull(context.getPlayer()).networkHandler.sendPacket(PolymerBlockUtils.createBlockEntityPacket(pos.toImmutable(), BlockEntityType.SKULL, main));
    }
}
