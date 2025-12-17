package eu.pb4.polymer.core.mixin.client.debug;

import eu.pb4.polymer.core.impl.client.ClientDebugFlags;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontMixin {
    @ModifyVariable(method = "getGlyphSource", at = @At("HEAD"), argsOnly = true)
    private FontDescription replaceFonts(FontDescription source) {
        if (ClientDebugFlags.customFonts) return source;
        return FontDescription.DEFAULT;
    }
}
