package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.compat.FabricFluids;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocolHandler;
import eu.pb4.polymer.core.impl.networking.entry.PolymerBlockEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
	@Override
	public void onInitialize() {
		CommonImplUtils.registerCommands(Commands::register);
		CommonImplUtils.registerDevCommands(Commands::registerDev);

		PolymerServerProtocolHandler.register();
		PolymerCommonUtils.ON_RESOURCE_PACK_STATUS_CHANGE.register(((handler, uuid, oldStatus, newStatus) -> {
			if (oldStatus != newStatus && handler instanceof ServerGamePacketListenerImpl handler1) {
				PolymerUtils.reloadWorld(handler1.player);
			}
		}));
		ImplPolymerRegistryEvent.iterateAndRegister(BuiltInRegistries.BLOCK, PolymerBlockEntry::cacheCalcDeltaOverride);
	}
	@Override
	public void onInitializeClient() {
		PolymerClientProtocolHandler.register();
		InternalClientRegistry.register();

		if (CompatStatus.FABRIC_FLUID_RENDERING) {
			FabricFluids.register();
		}
	}
}
