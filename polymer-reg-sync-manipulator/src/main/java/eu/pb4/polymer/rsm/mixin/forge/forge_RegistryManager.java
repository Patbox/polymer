package eu.pb4.polymer.rsm.mixin.forge;

import eu.pb4.polymer.rsm.impl.forge.ForgeRegistryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "net/minecraftforge/registries/RegistryManager", remap = false)
public class forge_RegistryManager {
    @Redirect(method = "generateRegistryPackets(Z)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;")
    )

    private static Stream<Object> polymerRegSync$removePolymerEntries(Stream<Object> stream, Function<Object, Object> function) {
        return stream.map(ForgeRegistryUtils::clearPolymerEntries).map(function);
    }
}
