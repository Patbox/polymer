package eu.pb4.polymer.core.mixin.client.debug;

import eu.pb4.polymer.core.impl.client.ClientDebugFlags;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
    @ModifyVariable(method = "getFontStorage", at = @At("HEAD"), argsOnly = true)
    private Identifier replaceFonts(Identifier id) {
        if (ClientDebugFlags.customFonts || id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) return id;
        return Style.DEFAULT_FONT_ID;
    }
}
