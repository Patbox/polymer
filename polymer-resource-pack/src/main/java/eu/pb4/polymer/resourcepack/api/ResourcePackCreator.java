package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.EventImplUtils;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.Nullable;

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
    public final Event<Consumer<ResourcePackBuilder>> creationEvent = EventImplUtils.createConsumerEvent();
    public final Event<Runnable> finishedEvent = EventImplUtils.createRunnableEvent();
    public final Event<Consumer<ResourcePackBuilder>> afterInitialCreationEvent = EventImplUtils.createConsumerEvent();

    private final Set<String> modIds = new HashSet<>();
    private final Set<String> modIdsNoCopy = new HashSet<>();
    private final Set<Path> sourcePaths = new HashSet<>();
    private Component packDescription = null;
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
            EventImplUtils.copyEvent(source.creationEvent, creator.creationEvent);
            EventImplUtils.copyEvent(source.afterInitialCreationEvent, creator.afterInitialCreationEvent);
            EventImplUtils.copyEvent(source.finishedEvent, creator.finishedEvent);
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
    public Component getPackDescription() {
        return this.packDescription;
    }

    /**
     * Sets pack description
     *
     * @param description new description
     */
    public void setPackDescription(String description) {
        this.packDescription = Component.literal(description);
    }

    /**
     * Sets pack description
     *
     * @param description new description
     */
    public void setPackDescription(Component description) {
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
        return this.modIds.isEmpty() && EventImplUtils.isEmpty(this.creationEvent) && EventImplUtils.isEmpty(this.afterInitialCreationEvent);
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
            builder.getPackMcMetaBuilder().metadata(new PackMetadataSection(this.packDescription, builder.getPackMcMetaBuilder().metadata().supportedFormats()));
        }


        if (this.packIcon != null) {
            builder.addData("pack.png", this.packIcon);
        }

        status.accept("action:creation_event_start");
        this.creationEvent.invoker().accept(builder);
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
        this.afterInitialCreationEvent.invoker().accept(builder);
        status.accept("action:late_creation_event_finish");

        status.accept("action:build");
        successful = builder.buildResourcePack().get() && successful;

        status.accept("action:done");
        this.finishedEvent.invoker().run();
        return successful;
    }
}
