package eu.pb4.polymer.impl.other;

import java.util.function.Consumer;

public class EventRunners {
    public static final Consumer<Runnable> RUN = Runnable::run;
}
