package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements PacketListenerImplExtension {

    @Shadow public abstract RegistryAccess.Frozen registryAccess();

    @Override
    public @Nullable RegistryAccess polymer$getDynamicRegistryManager() {
        return this.registryAccess();
    }
}
