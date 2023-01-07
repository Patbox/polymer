package eu.pb4.polymer.autohost.impl;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.MinecraftServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;

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
    public static boolean isPackReady = false;

    @Nullable
    public static HttpServer start(MinecraftServer minecraftServer, AutoHostConfig config) {
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
            WebServer.isPackReady = true;
            updateHash();

            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((x) -> {
                isPackReady = false;
            });

            PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(() -> {
                isPackReady = true;
                updateHash();
            });

            return server;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateHash() {
        try {
            hash = com.google.common.io.Files.asByteSource(PolymerResourcePackUtils.DEFAULT_PATH.toFile()).hash(Hashing.sha1()).toString();
            size = Files.size(PolymerResourcePackUtils.DEFAULT_PATH);
            lastUpdate = Files.getLastModifiedTime(PolymerResourcePackUtils.DEFAULT_PATH).toMillis();
            WebServer.fullAddress = WebServer.baseAddress + WebServer.hash + ".zip";
        } catch (Exception e) {
            hash = "";
            size = 0;
        }

    }

    private static InetSocketAddress createBindAddress(MinecraftServer server, AutoHostConfig config) {
        var serverIp = server.getServerIp();
        if (!Strings.isNullOrEmpty(serverIp)) {
            return new InetSocketAddress(serverIp, config.port);
        } else {
            return new InetSocketAddress(config.port);
        }
    }


    public static void handle(HttpExchange exchange) throws IOException {
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

}