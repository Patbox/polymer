package eu.pb4.polymer.mixin;

import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.compat.CompatStatus;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PolymerMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE_ROOT = "eu.pb4.polymer.mixin.";
    private static final String COMPAT_PACKAGE = "compat.";
    private boolean devWarn = false;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var name = mixinClassName.substring(PACKAGE_ROOT.length());

        var disabledReason = PolymerImpl.DISABLED_MIXINS.get(name);
        if (disabledReason != null) {
            PolymerImpl.LOGGER.warn("Mixin \"" + mixinClassName + "\" was disabled by "
                    + disabledReason.source() + ". Reason: " + disabledReason.reason() + ". This might cause issues and isn't generally supported!");

            if (PolymerImpl.DEV_ENV && !devWarn) {
                devWarn = true;
                PolymerImpl.LOGGER.error(
                        "Ok... I see you are disabling my mixins. It's generally not good idea to do that, since most "
                        + "likely this will break polymer. Only do it if you make sure everything will work fine (for example by replicating "
                        + "logic in your own mod). If you need to do more things, it might be better to just ask me to add api for that. "
                        + "I'm open for these if they allow better usage/integration with polymer... Make a github issue or ask on discord!"
                );
            }

            return false;
        }

        name = name.replace("client.", "");
        if (name.startsWith(COMPAT_PACKAGE)) {
            var tmp = name.split("\\.");

            var type = tmp[tmp.length - 1].split("_")[0];

            return switch (type) {
                case "fabricSync" -> CompatStatus.FABRIC_SYNC;
                case "fabricSH" -> CompatStatus.FABRIC_SCREEN_HANDLER;
                case "polymc" -> CompatStatus.POLYMC;
                case "wthit" -> CompatStatus.WTHIT;
                case "rei" -> CompatStatus.REI;
                case "lithium" -> CompatStatus.LITHIUM;
                case "armor" -> PolymerImpl.USE_ALT_ARMOR_HANDLER;
                case "ip" -> CompatStatus.IMMERSIVE_PORTALS;
                case "quiltReg" -> CompatStatus.QUILT_REGISTRY;
                default -> true;
            };
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
