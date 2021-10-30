package eu.pb4.polymer.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PolymerMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE_ROOT = "eu.pb4.polymer.mixin.";
    private static final String COMPAT_PACKAGE = "compat.";

    private boolean fabricSync = false;
    private boolean polymc = false;
    private boolean wthit = false;
    private boolean lithium = false;

    @Override
    public void onLoad(String mixinPackage) {
        var loader = FabricLoader.getInstance();

        this.fabricSync = loader.isModLoaded("fabric-registry-sync-v0");
        this.polymc = loader.isModLoaded("polymc");
        this.wthit = loader.isModLoaded("wthit");
        this.lithium = loader.isModLoaded("lithium");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var name = mixinClassName.substring(PACKAGE_ROOT.length());

        if (name.startsWith(COMPAT_PACKAGE)) {
            name = name.substring(COMPAT_PACKAGE.length());

            var type = name.split("_")[0];


            return switch (type) {
                case "fabricSync" -> this.fabricSync;
                case "polymc" -> this.polymc;
                case "wthit" -> this.wthit;
                default -> true;
            };
        }

        if (targetClassName.contains("lithium")) {
            return this.lithium;
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
