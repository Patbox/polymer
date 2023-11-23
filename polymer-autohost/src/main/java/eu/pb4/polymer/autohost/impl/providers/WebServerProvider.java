package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.MinecraftServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class WebServerProvider implements ResourcePackDataProvider {
    private Config config;
    private HttpServer server;
    public long size = 0;
    public String hash = "";
    public long lastUpdate = 0;
    public String baseAddress = "";
    public String fullAddress = "";
    public boolean enabled;
    public boolean isPackReady = false;

    @Nullable
    public void serverStarted(MinecraftServer minecraftServer) {
        try {
            var address = createBindAddress(minecraftServer, config);
            server = HttpServer.create(address, 0);

            server.createContext("/", this::handle);
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            this.enabled = true;
            this.baseAddress = config.externalAddress;

            if (!this.baseAddress.endsWith("/")) {
                this.baseAddress += "/";
            }
            this.isPackReady = true;
            updateHash();

            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((x) -> {
                isPackReady = false;
            });

            PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(() -> {
                isPackReady = true;
                updateHash();
            });

            AutoHost.generateAndCall(minecraftServer, minecraftServer::sendMessage, () -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public void serverStopped(MinecraftServer minecraftServer) {
        server.stop(0);
    }

    private void updateHash() {
        try {
            hash = com.google.common.io.Files.asByteSource(PolymerResourcePackUtils.DEFAULT_PATH.toFile()).hash(Hashing.sha1()).toString();
            size = Files.size(PolymerResourcePackUtils.DEFAULT_PATH);
            lastUpdate = Files.getLastModifiedTime(PolymerResourcePackUtils.DEFAULT_PATH).toMillis();
            this.fullAddress = this.baseAddress + this.hash + ".zip";
        } catch (Exception e) {
            hash = "";
            size = 0;
        }

    }

    private static InetSocketAddress createBindAddress(MinecraftServer server, Config config) {
        var serverIp = server.getServerIp();
        if (!Strings.isNullOrEmpty(serverIp)) {
            return new InetSocketAddress(serverIp, config.port);
        } else {
            return new InetSocketAddress(config.port);
        }
    }


    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            if (Files.exists(PolymerResourcePackUtils.DEFAULT_PATH)) {
                var updateTime = Files.getLastModifiedTime(PolymerResourcePackUtils.DEFAULT_PATH).toMillis();
                if (updateTime > lastUpdate) {
                    updateHash();
                }

                try (
                        var input = Files.newInputStream(PolymerResourcePackUtils.DEFAULT_PATH);
                        var output = exchange.getResponseBody()
                ) {
                    exchange.getResponseHeaders().add("Server", "polymer-autohost");
                    exchange.getResponseHeaders().add("Content-Type", "application/zip");
                    exchange.sendResponseHeaders(HttpStatus.SC_OK, size);

                    input.transferTo(output);
                    output.flush();
                }
            }
         }
    }

    @Override
    public Collection<MinecraftServer.ServerResourcePackProperties> getProperties() {
        return List.of(ResourcePackDataProvider.createProperties(PolymerResourcePackUtils.getMainUuid(), this.fullAddress, this.hash));
    }

    @Override
    public boolean isReady() {
        return this.isPackReady;
    }

    @Override
    public JsonElement saveSettings() {
        return this.config.toJson();
    }

    @Override
    public void loadSettings(JsonElement settings) {
        this.config = CommonImpl.GSON.fromJson(settings, Config.class);
        if (this.config == null) {
            this.config = new Config();
        }
    }

    public static class Config {
        public String _c1 = "Port used internally to run http server";
        public int port = 25567;
        public String _c2 = "Public address used for sending requests";
        @SerializedName("external_address")
        public String externalAddress = "http://localhost:25567/";

        public JsonElement toJson() {
            return CommonImpl.GSON.toJsonTree(this);
        }
    }
}