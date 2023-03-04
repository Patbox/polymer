package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.ElementHolder;

import java.util.Collection;

public interface HolderHolder {
    void polymer$addHolder(ElementHolder holder);
    void polymer$removeHolder(ElementHolder holder);
    Collection<ElementHolder> polymer$getHolders();
}
