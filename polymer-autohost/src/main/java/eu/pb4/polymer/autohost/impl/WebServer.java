package eu.pb4.polymer.autohost.impl;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import net.minecraft.server.MinecraftServer;
import org.apache.http.HttpStatus;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class WebServer {
    public static long size = 0;
    public static String hash = "";
    public static long lastUpdate = 0;
    public static String baseAddress = "";
    public static String fullAddress = "";
    public static boolean enabled;

    @Nullable
    public static HttpServer start(MinecraftServer minecraftServer, Config config) {
        try {
            var address = createBindAddress(minecraftServer, config);
            var server = HttpServer.create(address, 0);

            server.createContext("/", WebServer::handle);
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            WebServer.enabled = true;
            WebServer.baseAddress = config.externalAddress;

            if (!WebServer.baseAddress.endsWith("/")) {
                WebServer.baseAddress += "/";
            }
            updateHash();

            return server;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateHash() {
        try {
            hash = com.google.common.io.Files.asByteSource(PolymerRPUtils.DEFAULT_PATH.toFile()).hash(Hashing.sha1()).toString();
            size = Files.size(PolymerRPUtils.DEFAULT_PATH);
            lastUpdate = Files.getLastModifiedTime(PolymerRPUtils.DEFAULT_PATH).toMillis();
            WebServer.fullAddress = WebServer.baseAddress + WebServer.hash + ".zip";
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


    public static void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            if (Files.exists(PolymerRPUtils.DEFAULT_PATH)) {
                var updateTime = Files.getLastModifiedTime(PolymerRPUtils.DEFAULT_PATH).toMillis();
                if (updateTime > lastUpdate) {
                    updateHash();
                }

                try (
                        var input = Files.newInputStream(PolymerRPUtils.DEFAULT_PATH);
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

    public static class Config {
        public String _c1 = "Enables Polymer's ResourcePack Auto Hosting";
        public boolean enableHttpServer = PolymerImpl.DEV_ENV;
        public String _c2 = "Port used internally to run http server";
        public int port = 25567;
        public String _c3 = "Public address used for sending requests";
        public String externalAddress = "http://localhost:25567/";
        public String _c4 = "Marks resource pack as required";
        public boolean require = false;
        public String _c5 = "Message sent to clients before pack is loaded";
        public JsonElement message = new JsonPrimitive("This server uses resource pack to enhance gameplay with custom textures and models. It might be unplayable without them.");
        public String _c6 = "Disconnect message in case of failure";
        public JsonElement disconnectMessage = new JsonPrimitive("Couldn't apply server resourcepack!");
    }
}