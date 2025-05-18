package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import io.netty.buffer.ByteBuf;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;


@ApiStatus.Internal
public record PolymerBlockEntry(Identifier identifier, int numId, float hardness, boolean customBreakingDelta,
                                Text text, BlockState visual, ItemStack visualStack) {
    private static final PacketCodec<ByteBuf, BlockState> STATE = PacketCodecs.entryOf(Block.STATE_IDS);
    public static final PacketCodec<ContextByteBuf, PolymerBlockEntry> CODEC = PacketCodec.of(PolymerBlockEntry::write, PolymerBlockEntry::read);
    private static final String REMAPPED_calcBlockBreakingDelta = FabricLoader.getInstance().isDevelopmentEnvironment()
            ? FabricLoader.getInstance().getMappingResolver()
            .mapMethodName("intermediary", "net.minecraft.class_4970", "method_9594",
                    "(Lnet/minecraft/class_2680;Lnet/minecraft/class_1657;Lnet/minecraft/class_1922;Lnet/minecraft/class_2338;)F")
            : "method_9594";

    private static final Map<Class<?>, Boolean> HAS_OVERRIDDEN_DELTA = new IdentityHashMap<>();

    public static PolymerBlockEntry of(Block block) {
        return new PolymerBlockEntry(Registries.BLOCK.getId(block), Registries.BLOCK.getRawId(block),
                block.getHardness(), HAS_OVERRIDDEN_DELTA.getOrDefault(block.getClass(), Boolean.TRUE),
                block.getName(), block.getDefaultState(), block.asItem() != null ? block.asItem().getDefaultStack() : ItemStack.EMPTY);
    }

    public static void cacheCalcDeltaOverride(Block block) {
        var value = HAS_OVERRIDDEN_DELTA.get(block.getClass());
        if (value != null) {
            return;
        }

        Class<?> clazz = block.getClass();

        while (clazz != AbstractBlock.class) {
            try {
                clazz.getDeclaredMethod(REMAPPED_calcBlockBreakingDelta,
                        BlockState.class,
                        PlayerEntity.class,
                        BlockView.class,
                        BlockPos.class
                );
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
        var name = TextCodecs.PACKET_CODEC.decode(buf);
        var visual = STATE.decode(buf);
        var visualStack = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
        float hardness = -2;
        boolean customBreakingDelta = true;
        if (buf.version() >= 12) {
            hardness = buf.readFloat();
            customBreakingDelta = buf.readBoolean();
        }

        return new PolymerBlockEntry(id, numId, hardness, customBreakingDelta, name, visual, visualStack);
    }

    public void write(ContextByteBuf buf) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(numId);
        TextCodecs.PACKET_CODEC.encode(buf, text);
        STATE.encode(buf, visual);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, this.visualStack);
        if (buf.version() >= 12) {
            buf.writeFloat(this.hardness);
            buf.writeBoolean(this.customBreakingDelta);
        }
    }
}
