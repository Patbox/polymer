package eu.pb4.polymer.core.mixin.client.debug;

import eu.pb4.polymer.core.impl.client.ClientDebugFlags;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
    @ModifyVariable(method = "getGlyphs", at = @At("HEAD"), argsOnly = true)
    private StyleSpriteSource replaceFonts(StyleSpriteSource source) {
        if (ClientDebugFlags.customFonts) return source;
        return StyleSpriteSource.DEFAULT;
    }
}
