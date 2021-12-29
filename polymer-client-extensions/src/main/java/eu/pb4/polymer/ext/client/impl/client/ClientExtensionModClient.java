package eu.pb4.polymer.ext.client.impl.client;

import eu.pb4.polymer.ext.client.impl.CEProtocolServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ClientExtensionModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CEProtocolClient.initialize();
	}
}
