package eu.pb4.polymer.core.impl.client.debug;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class LookingAtPolymerBlockDebugHudEntry implements DebugScreenEntry {
    private static final Identifier SECTION_ID = Identifier.withDefaultNamespace("looking_at_block");

    @Override
    public void display(DebugScreenDisplayer lines, @Nullable Level world, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
        Entity entity = Minecraft.getInstance().getCameraEntity();

        if (world == null || entity == null || !InternalClientRegistry.enabled) {
            return;
        }

        HitResult hitResult = entity.pick(20.0, 0.0F, false);
        List<String> list = new ArrayList<>();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockPos = ((BlockHitResult)hitResult).getBlockPos();
            var block = InternalClientRegistry.getBlockAt(blockPos);
            var worldState = world.getBlockState(blockPos);
            if (block != ClientPolymerBlock.NONE_STATE && block.blockState() != worldState) {
                list.add(ChatFormatting.UNDERLINE + "Targeted Client Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                list.add(block.block().identifier().toString());
                for (var entry : block.states().entrySet()) {
                    list.add(entry.getKey() + ": " + switch (entry.getValue()) {
                        case "true" -> ChatFormatting.GREEN + "true";
                        case "false" -> ChatFormatting.RED + "false";
                        default -> entry.getValue();
                    });
                }
            }
            lines.addToGroup(SECTION_ID, list);
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return DebugScreenEntry.super.isAllowed(reducedDebugInfo) && InternalClientRegistry.enabled;
    }
}
