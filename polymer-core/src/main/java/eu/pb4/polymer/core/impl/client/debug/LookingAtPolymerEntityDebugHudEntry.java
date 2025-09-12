package eu.pb4.polymer.core.impl.client.debug;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LookingAtPolymerEntityDebugHudEntry implements DebugHudEntry {
    private static final Identifier SECTION_ID = Identifier.ofVanilla("looking_at_entity");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        if (!InternalClientRegistry.enabled) {
            return;
        }

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        var type = PolymerClientUtils.getEntityType(minecraftClient.targetedEntity);

        List<String> list = new ArrayList<>();
        if (type != null) {
            list.add(Formatting.UNDERLINE + "Targeted Client Entity");
            list.add(String.valueOf(type.identifier()));
        }

        lines.addLinesToSection(SECTION_ID, list);
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return DebugHudEntry.super.canShow(reducedDebugInfo) && InternalClientRegistry.enabled;
    }
}
