package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.NetworkHandlerExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.registry.DynamicRegistryManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Environment(EnvType.CLIENT)
@Mixin(ClientConfigurationNetworkHandler.class)
public abstract class ClientConfigurationNetworkHandlerMixin implements NetworkHandlerExtension {
    @Shadow @Final private DynamicRegistryManager.Immutable registryManager;

    @Override
    public @Nullable DynamicRegistryManager polymer$getDynamicRegistryManager() {
        return this.registryManager;
    }
}
