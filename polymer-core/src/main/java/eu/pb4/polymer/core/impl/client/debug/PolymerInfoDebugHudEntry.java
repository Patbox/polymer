package eu.pb4.polymer.core.impl.client.debug;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class PolymerInfoDebugHudEntry implements DebugScreenEntry {
    private static final Identifier SECTION_ID = Identifier.fromNamespaceAndPath("polymer", "info");

    @Override
    public void display(DebugScreenDisplayer lines, @Nullable Level world, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
        if (InternalClientRegistry.serverHasPolymer && PolymerImpl.DISPLAY_DEBUG_INFO_CLIENT) {
            var list = new ArrayList<String>();
            list.add(InternalClientRegistry.debugServerInfo);
            list.add(InternalClientRegistry.debugRegistryInfo);
            lines.addToGroup(SECTION_ID, list);
        }
    }
}
