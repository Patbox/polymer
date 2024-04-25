package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.compat.FabricFluids;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocolHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
	@Override
	public void onInitialize() {
		CommonImplUtils.registerCommands(Commands::register);
		CommonImplUtils.registerDevCommands(Commands::registerDev);

		PolymerServerProtocolHandler.register();
		PolymerCommonUtils.ON_RESOURCE_PACK_STATUS_CHANGE.register(((handler, uuid, oldStatus, newStatus) -> {
			if (oldStatus != newStatus && handler instanceof ServerPlayNetworkHandler handler1) {
				PolymerUtils.reloadWorld(handler1.player);
			}
		}));

		PolyMcUtils.register();

		if (PolymerImpl.FORCE_STRICT_UPDATES) {
			PolymerBlockUtils.requireStrictBlockUpdates();
		}

		ImplPolymerRegistryEvent.iterateAndRegister(Registries.ITEM, item -> {
			if (item instanceof PolymerItem x && !PolymerImplUtils.POLYMER_ITEM_CLASS_CACHE.containsKey(item.getClass())) {
                try {
                    var method = item.getClass().getMethod("getPolymerItemStack", ItemStack.class, TooltipType.class, ServerPlayerEntity.class);
					PolymerImplUtils.POLYMER_ITEM_CLASS_CACHE.put(x.getClass(), method.getDeclaringClass() != PolymerItem.class);
                } catch (NoSuchMethodException e) {
					PolymerImplUtils.POLYMER_ITEM_CLASS_CACHE.put(x.getClass(), false);
                }
            }
		});
	}
	@Override
	public void onInitializeClient() {
		PolymerClientProtocolHandler.register();

		if (CompatStatus.FABRIC_FLUID_RENDERING) {
			FabricFluids.register();
		}
	}
}
