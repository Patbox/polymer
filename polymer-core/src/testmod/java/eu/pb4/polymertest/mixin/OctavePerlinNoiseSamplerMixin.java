package eu.pb4.polymertest.mixin;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(OctavePerlinNoiseSampler.class)
public class OctavePerlinNoiseSamplerMixin {
    /**
     * @author a
     * @reason a
     */
    @Overwrite
    public static double maintainPrecision(double value) {
        return value;
    }
}
