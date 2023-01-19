package eu.pb4.polymer.mixin;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(GeneratorOptions.class)
public interface GeneratorOptionsAccessor {
    @Invoker("<init>")
    static GeneratorOptions createGeneratorOptions(long seed, boolean generateStructures, boolean bonusChest, Registry<DimensionOptions> options, Optional<String> legacyCustomOptions) {
        throw new UnsupportedOperationException();
    }
}
