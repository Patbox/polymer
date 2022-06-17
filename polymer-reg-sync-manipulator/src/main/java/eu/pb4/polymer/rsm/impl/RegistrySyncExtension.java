package eu.pb4.polymer.rsm.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RegistrySyncExtension<T> {
    void polymerRSM_setServerEntry(T obj, boolean value);
    boolean polymerRSM_isServerEntry(T obj);

    Status polymerRSM_getStatus();
    void polymerRSM_setStatus(Status status);
    boolean polymerRSM_updateStatus(Status status);
    void polymerRSM_clearStatus();

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
