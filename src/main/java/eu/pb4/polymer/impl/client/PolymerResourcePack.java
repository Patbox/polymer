package eu.pb4.polymer.impl.client;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
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
@SuppressWarnings({"deprecation"})
public class PolymerResourcePack extends ZipResourcePack {
    public static boolean GENERATED = false;

    private PolymerResourcePack(File file) {
        super(file);
    }

    @Override
    public String getName() {
        return ClientUtils.PACK_ID;
    }

    @Nullable
    public static PolymerResourcePack setup() {
        Path outputPath = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack.zip");
        if ((outputPath.toFile().exists() && GENERATED) || PolymerRPUtils.build(outputPath)) {
            GENERATED = true;
            return new PolymerResourcePack(outputPath.toFile());
        } else {
            return null;
        }
    }

    public static class Provider implements ResourcePackProvider {
        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {
            ResourcePack pack = PolymerResourcePack.setup();
            if (PolymerRPUtils.shouldGenerate()) {
                if (pack != null) {
                    profileAdder.accept(factory.create(pack.getName(),
                            new TranslatableText("text.polymer.resource_pack.name"),
                            PolymerRPUtils.isRequired(),
                            () -> pack,
                            new PackResourceMetadata(new TranslatableText("text.polymer.resource_pack.description" + (PolymerRPUtils.isRequired() ? ".required" : "")), SharedConstants.field_29738),
                            ResourcePackProfile.InsertionPosition.TOP,
                            ResourcePackSource.PACK_SOURCE_BUILTIN
                    ));
                }
            }
        }
    }
}
