package eu.pb4.polymer.core.impl.other;

public record DelayedAction(String id, long current, int delayMs, Runnable action) {
    public DelayedAction(String id, int delayMs, Runnable action) {
        this(id, System.currentTimeMillis(), delayMs, action);
    }

    public boolean tryDoing() {
        if (System.currentTimeMillis() > this.current + this.delayMs) {
            this.action.run();
            return true;
        }
        return false;
    }
}
