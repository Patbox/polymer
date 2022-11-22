package eu.pb4.polymer.rsm.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RegistrySyncExtension<T> {
    void polymer_registry_sync$setServerEntry(T obj, boolean value);
    boolean polymer_registry_sync$isServerEntry(T obj);

    Status polymer_registry_sync$getStatus();
    void polymer_registry_sync$setStatus(Status status);
    boolean polymer_registry_sync$updateStatus(Status status);
    void polymer_registry_sync$clearStatus();

    enum Status {
        VANILLA(0),
        WITH_SERVER_ONLY(1),
        WITH_MODDED(2);

        private final int priority;

        Status(int priority) {
            this.priority = priority;
        }
    }
}
