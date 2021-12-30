package eu.pb4.polymer.ext.client.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class ClientExtensionModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CERegistry.initialize();
		CEClientProtocol.initialize();
	}
}
