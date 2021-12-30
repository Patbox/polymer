package eu.pb4.polymer.ext.client.impl.client;

import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ClientExtensionModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CERegistry.initialize();
		CEClientProtocol.initialize();
	}
}
