package eu.pb4.polymer.autohost.mixin;

import net.minecraft.server.network.ConfigurationTask;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.server.network.ServerConfigurationPacketListenerImpl.class)
public interface ServerConfigurationPacketListenerImplAccessor {
    @Accessor("currentTask")
    ConfigurationTask polymer$getCurrentTask();
}
