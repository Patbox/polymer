package eu.pb4.polymertest;

import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.impl.PolymerImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class TestMod implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        var path = PolymerImpl.getGameDir().resolve("test_logo.png");


        if (path.toFile().exists()) {
            try {
                var bufferedImage = ImageIO.read(Files.newInputStream(path));

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
                byte[] bs = Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());
                var image = new String(bs, StandardCharsets.UTF_8);

                PolymerSyncUtils.ON_SYNC_CUSTOM.register((handler, bool) -> {
                    PolymerClientExtensions.setReloadScreen(handler, 0x225522, 0x88FF88, PolymerClientExtensions.ReloadLogoOverride.ICON, image);
                });
            } catch (Exception e) {
                // noop
            }
        }
    }

    @Override
    public void onInitializeClient() {

    }
}
