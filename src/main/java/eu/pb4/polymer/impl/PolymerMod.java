package eu.pb4.polymer.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.client.rendering.PolymerResourcePack;
import eu.pb4.polymer.impl.client.compat.ReiCompatibility;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;


import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Polymer");
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Gson GSON2 = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	public static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer").get();
	public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];

	@Override
	public void onInitialize() {
		if (CompatStatus.POLYMC) {
			PolymerRPUtils.markAsRequired();
			ServerLifecycleEvents.SERVER_STARTED.register(PolyMcHelpers::overrideCommand);
		}
	}

	@Override
	public void onInitializeClient() {
		PolymerResourcePack.setup();
		if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
			ReiCompatibility.registerEvents();
		}
	}

	public static <T> T loadConfig(String name, Class<T> clazz) {
		try {
			var folder = FabricLoader.getInstance().getConfigDir().resolve("polymer");
			if (!folder.toFile().isDirectory()) {
				if (folder.toFile().exists()) {
					Files.deleteIfExists(folder);
				}
				folder.toFile().mkdirs();
			}
			var path = folder.resolve(name + ".json");

			if (path.toFile().isFile()) {
				String json = IOUtils.toString(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8));
				var obj = GSON.fromJson(json, clazz);
				saveConfig(name, obj);
				return obj;
			}
		} catch (Exception e) {
			LOGGER.warn("Couldn't load config! " + clazz.toString());
			LOGGER.warn(e.toString());
		}

		try {
			var obj =clazz.getConstructor().newInstance();
			saveConfig(name, obj);

			return obj;
		} catch (Exception e) {
			LOGGER.error("Invalid config class! " + clazz.toString());
			return null;
		}
	}

	public static void saveConfig(String name, Object obj) {
		try {
			var folder = FabricLoader.getInstance().getConfigDir().resolve("polymer");
			if (!folder.toFile().isDirectory()) {
				if (folder.toFile().exists()) {
					Files.deleteIfExists(folder);
				}
				folder.toFile().mkdirs();
			}
			var path = folder.resolve(name + ".json");

			Files.writeString(path, GSON2.toJson(obj), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception e) {
			LOGGER.warn("Couldn't save config! " + obj.getClass().toString());
		}
	}
}
