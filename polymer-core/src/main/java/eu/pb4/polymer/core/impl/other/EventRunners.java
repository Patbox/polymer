package eu.pb4.polymer.core.impl.other;

import java.util.function.Consumer;

public class EventRunners {
    public static final Consumer<Runnable> RUN = Runnable::run;
}
