package eu.pb4.polymer.core.mixin.client.debug;

import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DebugHudEntries.class)
public interface DebugHudEntriesAccessor {
    @Invoker
    static Identifier callRegister(Identifier id, DebugHudEntry entry) {
        throw new UnsupportedOperationException();
    }
}
