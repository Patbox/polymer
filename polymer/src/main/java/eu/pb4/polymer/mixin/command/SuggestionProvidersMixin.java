package eu.pb4.polymer.mixin.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SuggestionProviders.class)
public class SuggestionProvidersMixin {
    @Shadow
    @Final
    private static Identifier ASK_SERVER_NAME;

    /**
     * Makes sure that client always asks for entity completions
     */
    @Inject(method = "computeId", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private static void polymer_onComputeName(SuggestionProvider<CommandSource> provider, CallbackInfoReturnable<Identifier> cir) {
        if (cir.getReturnValue().getPath().equals("summonable_entities")) {
            cir.setReturnValue(ASK_SERVER_NAME);
        }
    }
}
