package eu.pb4.polymer.resourcepack.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.util.IdentifierPattern;

@Mixin(IdentifierPattern.class)
public interface IdentifierPatternAccessor {
    @Invoker("<init>")
    static IdentifierPattern create(Optional<Pattern> namespace, Optional<Pattern> path) {
        throw new UnsupportedOperationException();
    }
}
