package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;


@ApiStatus.Internal
public record PolymerBlockEntry(Identifier identifier, int numId, float hardness, MiningDeltaLogic miningDeltaLogic,
                                Component text, BlockState visual, ItemStack visualStack) {
    private static final StreamCodec<ByteBuf, BlockState> STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
    public static final StreamCodec<ContextByteBuf, PolymerBlockEntry> CODEC = StreamCodec.ofMember(PolymerBlockEntry::write, PolymerBlockEntry::read);

    private static final Map<Class<?>, Boolean> HAS_OVERRIDDEN_DELTA = new IdentityHashMap<>();

    public static PolymerBlockEntry of(Block block) {
        return new PolymerBlockEntry(BuiltInRegistries.BLOCK.getKey(block), BuiltInRegistries.BLOCK.getId(block), block.defaultDestroyTime(),
                HAS_OVERRIDDEN_DELTA.getOrDefault(block.getClass(), Boolean.TRUE)
                        ? MiningDeltaLogic.CUSTOM_SERVER
                        : (block.defaultBlockState().requiresCorrectToolForDrops()
                        ? MiningDeltaLogic.TOOL_REQUIRED
                        : MiningDeltaLogic.DEFAULT),
                block.getName(), block.defaultBlockState(), block.asItem() != null ? block.asItem().getDefaultInstance() : ItemStack.EMPTY);
    }

    public static void cacheCalcDeltaOverride(Block block) {
        var value = HAS_OVERRIDDEN_DELTA.get(block.getClass());
        if (value != null) {
            return;
        }

        Class<?> clazz = block.getClass();

        while (clazz != BlockBehaviour.class) {
            try {
                clazz.getDeclaredMethod("getDestroyProgress", BlockState.class, Player.class, BlockGetter.class, BlockPos.class);
                HAS_OVERRIDDEN_DELTA.put(block.getClass(), true);
                return;
            } catch (Throwable e) {
                //
            }
            clazz = clazz.getSuperclass();
        }
        HAS_OVERRIDDEN_DELTA.put(block.getClass(), false);
    }

    public static PolymerBlockEntry read(ContextByteBuf buf) {
        var id = buf.readIdentifier();
        var numId = buf.readVarInt();
        var name = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf);

        BlockState visual;
        if (buf.version() < 16 || buf.readBoolean()) {
            visual = STATE.decode(buf);
        } else {
            visual = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
        }
        var visualStack = PolymerItemEntry.readSafeItemStack(buf, visual::getBlock);

        var hardness = buf.readFloat();
        var miningDeltaLogic = buf.readEnum(MiningDeltaLogic.class);

        return new PolymerBlockEntry(id, numId, hardness, miningDeltaLogic, name, visual, visualStack);
    }

    public void write(ContextByteBuf buf) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(numId);
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, text);

        {
            var writtenStack = PolymerBlockUtils.getPolymerBlockState(this.visual, buf.packetContext());
            var isSafeWrite = !(PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, writtenStack.getBlock()) instanceof PolymerBlock);

            if (buf.version() >= 16) {
                buf.writeBoolean(isSafeWrite);
                if (isSafeWrite) {
                    STATE.encode(buf, this.visual);
                }
            } else {
                STATE.encode(buf, isSafeWrite ? this.visual : Blocks.STONE.defaultBlockState());
            }
        }

        PolymerItemEntry.writeSafeItemStack(buf, this.visualStack);

        buf.writeFloat(this.hardness);
        buf.writeEnum(this.miningDeltaLogic);
    }

    public enum MiningDeltaLogic {
        DEFAULT,
        TOOL_REQUIRED,
        CUSTOM_SERVER,
        VANILLA
    }
}
