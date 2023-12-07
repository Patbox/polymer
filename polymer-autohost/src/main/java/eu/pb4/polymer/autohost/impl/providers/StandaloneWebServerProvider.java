package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class StandaloneWebServerProvider extends AbstractProvider  {
    private Config config;
    private HttpServer server;
    public String baseAddress = "";
    public String fullAddress = "";

    @Nullable
    public void serverStarted(MinecraftServer minecraftServer) {
        try {
            var address = createBindAddress(minecraftServer, config);
            server = HttpServer.create(address, 0);

            server.createContext("/", this::handle);
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            this.baseAddress = config.externalAddress;
            if (!this.baseAddress.endsWith("/")) {
                this.baseAddress += "/";
            }

            super.serverStarted(minecraftServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public void serverStopped(MinecraftServer minecraftServer) {
        server.stop(0);
    }

    protected boolean updateHash() {
        if (super.updateHash()) {
            this.fullAddress = this.baseAddress + this.hash + ".zip";
            return true;
        }
        return false;
    }

    @Override
    protected String getAddress(ClientConnection connection) {
        return this.fullAddress;
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
            if (Files.exists(PolymerResourcePackUtils.getMainPath())) {
                var updateTime = Files.getLastModifiedTime(PolymerResourcePackUtils.getMainPath()).toMillis();
                if (updateTime > lastUpdate) {
                    updateHash();
                }

                try (
                        var input = Files.newInputStream(PolymerResourcePackUtils.getMainPath());
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