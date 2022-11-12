package eu.pb4.polymer.impl.client.rendering;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolymerResourcePack extends ZipResourcePack {
    public static boolean generated = false;

    private PolymerResourcePack(String string, File file) {
        super(string, file, true);
    }

    @Nullable
    public static PolymerResourcePack setup() {
        Path outputPath = PolymerRPUtils.DEFAULT_PATH;
        if ((outputPath.toFile().exists() && generated) || PolymerRPUtils.build(outputPath)) {
            generated = true;
            return new PolymerResourcePack(ClientUtils.PACK_ID, outputPath.toFile());
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return ClientUtils.PACK_ID;
    }

    public static class Provider implements ResourcePackProvider {
        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder) {
            if (PolymerRPUtils.shouldGenerate()) {
                ResourcePack pack = PolymerResourcePack.setup();

                if (pack != null) {
                    profileAdder.accept(ResourcePackProfile.of(pack.getName(),
                            Text.translatable("text.polymer.resource_pack.name"),
                            PolymerRPUtils.isRequired() || PolymerImpl.FORCE_RESOURCE_PACK_CLIENT,
                            (x) -> pack,
                            new ResourcePackProfile.Metadata(Text.translatable("text.polymer.resource_pack.description" + (PolymerRPUtils.isRequired() ? ".required" : "")), SharedConstants.RESOURCE_PACK_VERSION, FeatureSet.empty()),
                            ResourceType.CLIENT_RESOURCES,
                            ResourcePackProfile.InsertionPosition.TOP,
                            true,
                            ResourcePackSource.BUILTIN
                    ));
                }
            }
        }
    }
}
