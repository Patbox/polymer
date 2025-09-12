package eu.pb4.polymer.core.impl.client.debug;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntryCategory;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LookingAtPolymerBlockDebugHudEntry implements DebugHudEntry {
    private static final Identifier SECTION_ID = Identifier.ofVanilla("looking_at_block");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        Entity entity = MinecraftClient.getInstance().getCameraEntity();

        if (world == null || entity == null || !InternalClientRegistry.enabled) {
            return;
        }

        HitResult hitResult = entity.raycast(20.0, 0.0F, false);
        List<String> list = new ArrayList<>();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockPos = ((BlockHitResult)hitResult).getBlockPos();
            var block = InternalClientRegistry.getBlockAt(blockPos);
            var worldState = world.getBlockState(blockPos);
            if (block != ClientPolymerBlock.NONE_STATE && block.blockState() != worldState) {
                list.add(Formatting.UNDERLINE + "Targeted Client Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                list.add(block.block().identifier().toString());
                for (var entry : block.states().entrySet()) {
                    list.add(entry.getKey() + ": " + switch (entry.getValue()) {
                        case "true" -> Formatting.GREEN + "true";
                        case "false" -> Formatting.RED + "false";
                        default -> entry.getValue();
                    });
                }
            }
            lines.addLinesToSection(SECTION_ID, list);
        }
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return DebugHudEntry.super.canShow(reducedDebugInfo) && InternalClientRegistry.enabled;
    }
}
