package eu.pb4.polymer.resourcepack.api;

import java.util.function.Consumer;

public interface ResourcePackStatusConsumer {
    static ResourcePackStatusConsumer nonLogging() {
        return new ResourcePackStatusConsumer() {
            boolean hadIssues = false;

            @Override
            public void accept(String string) {
            }

            @Override
            public void setHadIssues() {
                hadIssues = true;
            }

            @Override
            public boolean hadIssues() {
                return hadIssues;
            }
        };
    }

    static ResourcePackStatusConsumer simple(Consumer<String> consumer) {
        return new ResourcePackStatusConsumer() {
            boolean hadIssues = false;

            @Override
            public void accept(String string) {
                consumer.accept(string);
            }

            @Override
            public void setHadIssues() {
                hadIssues = true;
            }

            @Override
            public boolean hadIssues() {
                return hadIssues;
            }
        };
    }


    void accept(String string);

    void setHadIssues();

    boolean hadIssues();
}
