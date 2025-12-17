package eu.pb4.polymer.core.impl.client.debug;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class LookingAtPolymerEntityDebugHudEntry implements DebugScreenEntry {
    private static final Identifier SECTION_ID = Identifier.withDefaultNamespace("looking_at_entity");

    @Override
    public void display(DebugScreenDisplayer lines, @Nullable Level world, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
        if (!InternalClientRegistry.enabled) {
            return;
        }

        Minecraft minecraftClient = Minecraft.getInstance();
        var type = PolymerClientUtils.getEntityType(minecraftClient.crosshairPickEntity);

        List<String> list = new ArrayList<>();
        if (type != null) {
            list.add(ChatFormatting.UNDERLINE + "Targeted Client Entity");
            list.add(String.valueOf(type.identifier()));
        }

        lines.addToGroup(SECTION_ID, list);
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return DebugScreenEntry.super.isAllowed(reducedDebugInfo) && InternalClientRegistry.enabled;
    }
}
