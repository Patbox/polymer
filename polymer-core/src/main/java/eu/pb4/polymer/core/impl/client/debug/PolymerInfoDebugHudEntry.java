package eu.pb4.polymer.core.impl.client.debug;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PolymerInfoDebugHudEntry implements DebugHudEntry {
    private static final Identifier SECTION_ID = Identifier.of("polymer", "info");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        if (InternalClientRegistry.serverHasPolymer && PolymerImpl.DISPLAY_DEBUG_INFO_CLIENT) {
            var list = new ArrayList<String>();
            list.add(InternalClientRegistry.debugServerInfo);
            list.add(InternalClientRegistry.debugRegistryInfo);
            lines.addLinesToSection(SECTION_ID, list);
        }
    }
}
