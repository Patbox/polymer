package eu.pb4.polymer.mixin;

import eu.pb4.polymer.PolymerMod;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "eu.pb4.polymer.mixin.";
    private boolean isPolyMc = false;

    @Override
    public void onLoad(String mixinPackage) {
        if (FabricLoader.getInstance().isModLoaded("polymc")) {
            PolymerMod.LOGGER.info("PolyMC detected! Enabling compatibility mode!");
            this.isPolyMc = true;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());

        if (this.isPolyMc) {
            switch (mixin) {
                case "block.ServerPlayInteractionManagerMixin":
                    return false;
            }
        }

        if (mixin.startsWith("polymc")) {
            return this.isPolyMc;
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
