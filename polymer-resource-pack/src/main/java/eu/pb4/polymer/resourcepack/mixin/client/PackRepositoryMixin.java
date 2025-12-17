package eu.pb4.polymer.resourcepack.mixin.client;

import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import eu.pb4.polymer.resourcepack.mixin.accessors.BuiltInPackSourceAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin<T extends Pack> {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void polymer_construct(RepositorySource[] providers, CallbackInfo ci) {
        for (var x : providers) {
            if (x instanceof BuiltInPackSourceAccessor accessor && accessor.getPackType() == PackType.CLIENT_RESOURCES) {
                this.sources = new LinkedHashSet<>(this.sources);
                this.sources.add(new PolymerResourcePack.Provider());
            }
        }
    }
}
