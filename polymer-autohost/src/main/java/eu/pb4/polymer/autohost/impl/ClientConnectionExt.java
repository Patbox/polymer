package eu.pb4.polymer.autohost.impl;

public interface ClientConnectionExt {
    void polymerAutoHost$setAddress(String address, int port);

    String polymerAutoHost$getAddress();
    int polymerAutoHost$getPort();

    default String polymerAutoHost$getFullAddress() {
        return polymerAutoHost$getAddress() + ":" + polymerAutoHost$getPort();
    }
}
