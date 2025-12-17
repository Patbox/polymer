package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Environment(EnvType.CLIENT)
@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationPacketListenerImplMixin implements PacketListenerImplExtension {
    @Shadow @Final private RegistryAccess.Frozen receivedRegistries;

    @Override
    public @Nullable RegistryAccess polymer$getDynamicRegistryManager() {
        return this.receivedRegistries;
    }
}
