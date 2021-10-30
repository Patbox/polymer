package eu.pb4.polymer.api.client.block;

import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public record ClientPolymerBlock(Identifier identifier, int numId, Text name, BlockState defaultBlockState) {

    public record State(Map<String, String> states, ClientPolymerBlock block) {}
}
