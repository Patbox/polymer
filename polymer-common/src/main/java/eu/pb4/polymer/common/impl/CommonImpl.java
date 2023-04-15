package eu.pb4.polymer.common.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.Person;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommonImpl {
    public static final Logger LOGGER = LoggerFactory.getLogger("Polymer");
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final int CORE_COMMAND_MINIMAL_OP;
    public static final boolean DEVELOPER_MODE;
    public static final boolean MINIMAL_ABOUT;
    public static final String DESCRIPTION = "Library for creating better server side content!";
    public static final FabricLoader LOADER = FabricLoader.getInstance();
    public static final boolean DEV_ENV = LOADER.isDevelopmentEnvironment();
    public static final boolean IS_CLIENT = LOADER.getEnvironmentType() == EnvType.CLIENT;
    public static final boolean LOG_MORE_ERRORS;
    public static final boolean ENABLE_TEMPLATE_ENTITY_WARNINGS;

    private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer-common").get();
    public static final List<String> CONTRIBUTORS = new ArrayList<>();
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];
    public static final String GITHUB_URL = CONTAINER.getMetadata().getContact().get("sources").orElse("https://pb4.eu");

    public static final Map<String, DisabledMixinReason> DISABLED_MIXINS = new HashMap<>();
    private static boolean devWarn;

    public static DisabledMixinReason getDisabledMixin(String source, String mixin) {
        return DISABLED_MIXINS.get(source + ":" + mixin);
    }

    public static void addContributor(Person person) {
        if (!CONTRIBUTORS.contains(person.getName())) {
            CONTRIBUTORS.add(person.getName());
        }
    }

    static {
        new CompatStatus();

        var config = loadConfig("common", CommonConfig.class);
        CORE_COMMAND_MINIMAL_OP = config.coreCommandOperatorLevel;
        DEVELOPER_MODE = config.enableDevTools || DEV_ENV;
        MINIMAL_ABOUT = config.minimalisticAbout;
        LOG_MORE_ERRORS = config.logAllExceptions || CommonImpl.DEVELOPER_MODE;
        ENABLE_TEMPLATE_ENTITY_WARNINGS = config.enableTemplateEntityWarnings;

        CONTAINER.getMetadata().getAuthors().forEach(CommonImpl::addContributor);
        CONTAINER.getMetadata().getContributors().forEach(CommonImpl::addContributor);

        if (configDir().resolve("mixins.json").toFile().isFile()) {
            for (var mixin : loadConfig("mixins", MixinOverrideConfig.class).disabledMixins) {
                if (!mixin.contains(":")) {
                    mixin = "polymer-core:" + mixin;
                }
                DISABLED_MIXINS.put(mixin, new DisabledMixinReason("Config file (polymer/mixins.json)", "User/config specified, unknown reason"));
            }
        }

        for (var mods : LOADER.getAllMods()) {
            var meta = mods.getMetadata();
            var customValue = meta.getCustomValue("polymer:disable_mixin");

            if (customValue instanceof CustomValue.CvArray cvArray) {
                for (var value : cvArray) {
                    var key = value.getAsString();
                    if (!key.contains(":")) {
                        key = "polymer-core:" + key;
                    }

                    DISABLED_MIXINS.put(key,
                            new DisabledMixinReason(meta.getName() + " (" + meta.getId() + ")", "Unknown reason! I hope author knew what they were doing.."));
                }
            } else if (customValue instanceof CustomValue.CvObject cvObject) {
                for (var value : cvObject) {
                    var key = value.getKey();
                    if (!key.contains(":")) {
                        key = "polymer-core:" + key;
                    }

                    DISABLED_MIXINS.put(key,
                            new DisabledMixinReason(meta.getName() + " (" + meta.getId() + ")", value.getValue().getAsString()));
                }
            }
        }
    }

    public static boolean isModLoaded(String modId) {
        return LOADER.isModLoaded(modId);
    }

    public static Path configDir() {
        return LOADER.getConfigDir().resolve("polymer");
    }

    public static <T> T loadConfig(String name, Class<T> clazz) {
        try {
            var folder = configDir();
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
            var obj = clazz.getConstructor().newInstance();
            saveConfig(name, obj);

            return obj;
        } catch (Exception e) {
            LOGGER.error("Invalid config class! " + clazz.toString());
            return null;
        }
    }

    public static void saveConfig(String name, Object obj) {
        try {
            var folder = configDir();
            if (!folder.toFile().isDirectory()) {
                if (folder.toFile().exists()) {
                    Files.deleteIfExists(folder);
                }
                folder.toFile().mkdirs();
            }
            var path = folder.resolve(name + ".json");

            Files.writeString(path, GSON_PRETTY.toJson(obj), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.warn("Couldn't save config! " + obj.getClass().toString());
        }
    }


    public static Path getJarPath(String path) {
        return CONTAINER.getPath(path);
    }

    public static Path getGameDir() {
        return LOADER.getGameDir();
    }


    public static boolean shouldApplyMixin(String source, String mixinClassName) {
        return shouldApplyMixin(source, mixinClassName, false);
    }
    public static boolean shouldApplyMixin(String source, String mixinClassName, boolean armor) {
        var disabledReason = getDisabledMixin(source, mixinClassName);
        if (disabledReason != null) {
            CommonImpl.LOGGER.warn("Mixin '" + mixinClassName + "' from '" + source + "' was disabled by "
                    + disabledReason.source() + ". Reason: " + disabledReason.reason() + ". This might cause issues and isn't generally supported!");

            if (CommonImpl.DEV_ENV && !devWarn) {
                devWarn = true;
                CommonImpl.LOGGER.error(
                        "Ok... I see you are disabling my mixins. It's generally not good idea to do that, since most "
                                + "likely this will break polymer. Only do it if you make sure everything will work fine (for example by replicating "
                                + "logic in your own mod). If you need to do more things, it might be better to just ask me to add api for that. "
                                + "I'm open for these if they allow better usage/integration with polymer... Make a github issue or ask on discord!"
                );
            }

            return false;
        }

        var name = mixinClassName;

        name = name.replace("client.", "");
        if (name.startsWith("compat.")) {
            var tmp = name.split("\\.");

            var type = tmp[tmp.length - 1].split("_")[0];

            return switch (type) {
                case "fabricSync" -> CompatStatus.FABRIC_SYNC;
                case "fabricSH" -> CompatStatus.FABRIC_SCREEN_HANDLER;
                case "fabricItemGroup" -> CompatStatus.FABRIC_ITEM_GROUP;
                case "fabricNetworking" -> CompatStatus.FABRIC_NETWORKING;
                case "polymc" -> CompatStatus.POLYMC;
                case "wthit" -> CompatStatus.WTHIT;
                case "rei" -> CompatStatus.REI;
                case "emi" -> CompatStatus.EMI;
                case "lithium" -> CompatStatus.LITHIUM;
                case "jei" -> CompatStatus.JEI;
                case "armor" -> CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || armor;
                case "ip" -> CompatStatus.IMMERSIVE_PORTALS;
                case "quiltReg" -> CompatStatus.QUILT_REGISTRY;
                default -> true;
            };
        }

        return true;
    }


    public record DisabledMixinReason(String source, String reason) {};
}
