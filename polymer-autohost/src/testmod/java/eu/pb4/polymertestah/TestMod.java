package eu.pb4.polymertestah;

import com.google.common.hash.Hashing;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;


public class TestMod implements ModInitializer {

    @Override
    public void onInitialize() {
        var id = Identifier.of("testmod", "packtest");
        var path = FabricLoader.getInstance().getModContainer("apolymertestautohost")
                .get().findPath("testpack.zip").orElseThrow();
        AutoHostUtils.registerHostedFile(id, path);

        String hashedFile = null;
        try {

            hashedFile = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(path)));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        String finalHashedFile = hashedFile;
        AutoHostUtils.SEND_RESOURCE_PACK_COLLECTOR.register(((provider, context, consumer) -> {
            consumer.accept(provider.createProperties(context, id, finalHashedFile));
        }));

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((b) -> {
            //try {
                //Thread.sleep(Duration.ofSeconds(15));
            //} catch (InterruptedException e) {
            //    throw new RuntimeException(e);
            //}
        });
    }
}
