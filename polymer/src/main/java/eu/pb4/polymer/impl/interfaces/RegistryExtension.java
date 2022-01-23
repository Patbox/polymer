package eu.pb4.polymer.impl.interfaces;

public interface RegistryExtension {
    Status polymer_getStatus();
    void polymer_setStatus(Status status);
    boolean polymer_updateStatus(Status status);

    enum Status {
        VANILLA_ONLY(0),
        WITH_POLYMER(1),
        WITH_REGULAR_MODS(2);

        private final int priority;

        Status(int priority) {
            this.priority = priority;
        }

        public boolean canReplace(Status status) {
            return this.priority >= status.priority;
        }
    }
}
