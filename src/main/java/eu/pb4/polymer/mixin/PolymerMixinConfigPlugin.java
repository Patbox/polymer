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

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var name = mixinClassName.substring(PACKAGE_ROOT.length()).replace("client.", "");

        if (name.startsWith(COMPAT_PACKAGE)) {
            var tmp = name.split("\\.");

            var type = tmp[tmp.length - 1].split("_")[0];


            return switch (type) {
                case "fabricSync" -> CompatStatus.FABRIC_SYNC;
                case "polymc" -> CompatStatus.POLYMC;
                case "wthit" -> CompatStatus.WTHIT;
                case "rei" -> CompatStatus.REI;
                case "lithium" -> CompatStatus.LITHIUM;
                case "armor" -> PolymerImpl.USE_ALT_ARMOR_HANDLER;
                case "ip" -> CompatStatus.IMMERSIVE_PORTALS;
                default -> true;
            };
        }

        if (targetClassName.contains("lithium_BlockPaletteMixin")) {
            return CompatStatus.LITHIUM;
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
