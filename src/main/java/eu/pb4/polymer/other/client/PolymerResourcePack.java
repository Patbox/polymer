package eu.pb4.polymer.other.client;

import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolymerResourcePack extends DirectoryResourcePack {
    private PolymerResourcePack(File file) {
        super(file);
    }

    @Override
    public String getName() {
        return ClientUtils.PACK_ID;
    }

    @Nullable
    public static PolymerResourcePack setup() {
        Path basePath = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack");
        if (ResourcePackUtils.build(basePath)) {
            return new PolymerResourcePack(basePath.resolve("output").toFile());
        } else {
            return null;
        }
    }

    public static class Provider implements ResourcePackProvider {
        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {
            if (ResourcePackUtils.shouldGenerate()) {
                ResourcePack pack = PolymerResourcePack.setup();

                if (pack != null) {
                    profileAdder.accept(factory.create(pack.getName(),
                            new TranslatableText("text.polymer.resource_pack.name"),
                            ResourcePackUtils.isRequired(),
                            () -> pack,
                            new PackResourceMetadata(new TranslatableText("text.polymer.resource_pack.description" + (ResourcePackUtils.isRequired() ? ".required" : "")), SharedConstants.RESOURCE_PACK_VERSION),
                            ResourcePackProfile.InsertionPosition.TOP,
                            ResourcePackSource.PACK_SOURCE_BUILTIN
                    ));
                }
            }
        }
    }
}
