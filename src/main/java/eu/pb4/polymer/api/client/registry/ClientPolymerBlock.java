package eu.pb4.polymer.api.client.registry;

import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;

public record ClientPolymerBlock(Identifier identifier, int numId, Text name, BlockState defaultBlockState) {
    public static final ClientPolymerBlock NONE = new ClientPolymerBlock(PolymerImpl.id("none"), 0, LiteralText.EMPTY, Blocks.AIR.getDefaultState());
    public static final State NONE_STATE = new State(Collections.emptyMap(), NONE);

    public static final PolymerRegistry<ClientPolymerBlock> REGISTRY = InternalClientRegistry.BLOCKS;

    public record State(Map<String, String> states, ClientPolymerBlock block) {}
}
