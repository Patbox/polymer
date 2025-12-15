package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.impl.AutoHost;
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

    @Nullable
    public void serverStarted(MinecraftServer minecraftServer) {
        try {
            var address = createBindAddress(minecraftServer, config);
            server = HttpServer.create(address, 0);

            server.createContext("/", this::handle);
            server.setExecutor(Executors.newFixedThreadPool(2, x -> {
                var thread = new Thread(x);
                thread.setDaemon(true);
                return thread;
            }));
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
        if (server != null) {
            server.stop(0);
        }
    }

    @Override
    protected String getAddress(ClientConnection connection, String file) {
        return this.baseAddress + file;
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
            var path = AutoHost.getPath(exchange.getRequestURI().getPath().substring(1));

            if (path != null && Files.exists(path)) {
                try (
                        var input = Files.newInputStream(path);
                        var output = exchange.getResponseBody()
                ) {
                    exchange.getResponseHeaders().add("Server", "polymer-autohost");
                    exchange.getResponseHeaders().add("Content-Type", "application/zip");
                    exchange.getResponseHeaders().add("Cache-Control", "public, max-age=" + AutoHost.config.cacheControlAge);
                    exchange.sendResponseHeaders(HttpStatus.SC_OK, 0);
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
        @SerializedName(value = "external_address", alternate = {"forced_address", "address"})
        public String externalAddress = "http://localhost:25567/";

        public JsonElement toJson() {
            return CommonImpl.GSON.toJsonTree(this);
        }
    }
}