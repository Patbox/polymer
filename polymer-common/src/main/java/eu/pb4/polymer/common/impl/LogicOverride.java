package eu.pb4.polymer.common.impl;

public interface LogicOverride {
    LogicOverride TRUE = (v) -> true;
    LogicOverride FALSE = (v) -> false;
    LogicOverride DEFAULT = (v) -> v;

    boolean value(boolean fallback);
}
