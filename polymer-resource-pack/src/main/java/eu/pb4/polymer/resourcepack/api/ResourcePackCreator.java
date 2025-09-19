package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.resource.PackVersion;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Range;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Utilities allowing simple creation of resource pack
 */
public final class ResourcePackCreator {
    public final SimpleEvent<Consumer<ResourcePackBuilder>> creationEvent = new SimpleEvent<>();
    public final SimpleEvent<Runnable> finishedEvent = new SimpleEvent<>();
    public final SimpleEvent<Consumer<ResourcePackBuilder>> afterInitialCreationEvent = new SimpleEvent<>();

    private final Set<String> modIds = new HashSet<>();
    private final Set<String> modIdsNoCopy = new HashSet<>();
    private final Set<Path> sourcePaths = new HashSet<>();
    private Text packDescription = null;
    private byte[] packIcon = null;

    ResourcePackCreator() {
    }

    public static ResourcePackCreator forDefault() {
        return PolymerResourcePackUtils.getInstance();
    }

    public static ResourcePackCreator create() {
        return new ResourcePackCreator();
    }

    public static ResourcePackCreator createCopy(ResourcePackCreator source, boolean copyEvents) {
        var creator = new ResourcePackCreator();
        if (copyEvents) {
            source.creationEvent.invokers().forEach(creator.creationEvent::register);
            source.afterInitialCreationEvent.invokers().forEach(creator.afterInitialCreationEvent::register);
            source.finishedEvent.invokers().forEach(creator.finishedEvent::register);
        }
        creator.modIds.addAll(source.modIds);
        creator.modIdsNoCopy.addAll(source.modIdsNoCopy);
        creator.packDescription = source.packDescription;
        creator.packIcon = source.packIcon;
        creator.sourcePaths.addAll(source.sourcePaths);
        return creator;
    }

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param modId Id of mods used as a source
     */
    public boolean addAssetSource(String modId) {
        if (CommonImpl.isModLoaded(modId)) {
            this.modIds.add(modId);
            return true;
        }

        return false;
    }

    public boolean addAssetSourceWithoutCopy(String modId) {
        if (CommonImpl.isModLoaded(modId)) {
            this.modIdsNoCopy.add(modId);
            return true;
        }

        return false;
    }

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param sourcePath Path to a source
     */
    public boolean addAssetSource(Path sourcePath) {
        return this.sourcePaths.add(sourcePath);
    }

    @Nullable
    public Text getPackDescription() {
        return this.packDescription;
    }

    /**
     * Sets pack description
     *
     * @param description new description
     */
    public void setPackDescription(String description) {
        this.packDescription = Text.literal(description);
    }

    /**
     * Sets pack description
     *
     * @param description new description
     */
    public void setPackDescription(Text description) {
        this.packDescription = description;
    }

    @Nullable
    public byte[] getPackIcon() {
        return packIcon;
    }

    /**
     * Sets icon of pack
     *
     * @param packIcon bytes representing png image of icon
     */
    public void setPackIcon(byte[] packIcon) {
        this.packIcon = packIcon;
    }

    public boolean isEmpty() {
        return this.modIds.isEmpty() && this.creationEvent.isEmpty();
    }

    public boolean build(Path output) throws ExecutionException, InterruptedException {
        return build(output, (s) -> {
        });
    }

    public boolean build(Path output, Consumer<String> status) throws ExecutionException, InterruptedException {
        boolean successful = true;

        try {
            Files.createDirectories(output.getParent());
        } catch (Throwable e) {
            CommonImpl.LOGGER.error("Couldn't create " + output.getParent() + " directory!", e);
        }

        try {
            if (output.toFile().exists()) {
                Files.deleteIfExists(output );
            }
        } catch (Exception e) {
            CommonImpl.LOGGER.error("Couldn't remove " + output  + " file!", e);
        }

        var builder = new DefaultRPBuilder(ResourcePackBuilder.OutputGenerator.zipGenerator(output), status);
        status.accept("action:created_builder");

        if (this.packDescription != null) {
            builder.getPackMcMetaBuilder().metadata(new PackResourceMetadata(this.packDescription,
                    new Range<>(SharedConstants.getGameVersion().packVersion(ResourceType.CLIENT_RESOURCES))));
        }


        if (this.packIcon != null) {
            builder.addData("pack.png", this.packIcon);
        }

        status.accept("action:creation_event_start");
        this.creationEvent.invoke((x) -> x.accept(builder));
        status.accept("action:creation_event_finish");

        for (var path : this.sourcePaths) {
            successful = builder.copyFromPath(path) && successful;
        }

        for (String modId : this.modIdsNoCopy) {
            successful = builder.addAssetsSource(modId) && successful;
        }

        for (String modId : this.modIds) {
            successful = builder.copyAssets(modId) && successful;
        }

        status.accept("action:late_creation_event_start");
        this.afterInitialCreationEvent.invoke((x) -> x.accept(builder));
        status.accept("action:late_creation_event_finish");

        status.accept("action:build");
        successful = builder.buildResourcePack().get() && successful;

        status.accept("action:done");
        this.finishedEvent.invoke(Runnable::run);
        return successful;
    }
}
