package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

@Environment(EnvType.CLIENT)
public record ClientPolymerBlock(Identifier identifier, int numId, float hardness, MiningDeltaLogic miningDeltaLogic, Text name, BlockState defaultBlockState,
                                 @Nullable Block registryEntry, ItemStack displayStack) implements ClientPolymerEntry<Block> {
    public static final ClientPolymerBlock NONE = new ClientPolymerBlock(PolymerImplUtils.id("none"), 0, -2, MiningDeltaLogic.VANILLA, Text.empty(), Blocks.AIR.getDefaultState(), null, ItemStack.EMPTY);
    public static final State NONE_STATE = new State(Collections.emptyMap(), NONE);
    public static final PolymerRegistry<ClientPolymerBlock> REGISTRY = InternalClientRegistry.BLOCKS;

    public ClientPolymerBlock(Identifier identifier, int numId, Text name, BlockState defaultBlockState, @Nullable Block registryEntry) {
        this(identifier, numId, name, defaultBlockState, registryEntry, defaultBlockState.getBlock().asItem().getDefaultStack());
    }

    public ClientPolymerBlock(Identifier identifier, int numId, Text name, BlockState defaultBlockState) {
        this(identifier, numId, name, defaultBlockState, null);
    }

    public boolean isEmpty() {
        return this == NONE;
    }

    public ClientPolymerBlock(Identifier identifier, int numId, Text name, BlockState defaultBlockState,
                              @Nullable Block registryEntry, ItemStack displayStack) {
        this(identifier, numId, -2, MiningDeltaLogic.CUSTOM_SERVER, name, defaultBlockState, registryEntry, displayStack);
    }

    public record State(Map<String, String> states, ClientPolymerBlock block, @Nullable BlockState blockState) {
        public State(Map<String, String> states, ClientPolymerBlock block) {
            this(states, block, null);
        }

        public boolean isEmpty() {
            return this == NONE_STATE;
        }
    }

    public enum MiningDeltaLogic {
        DEFAULT,
        TOOL_REQUIRED,
        CUSTOM_SERVER,
        VANILLA
    }
}
