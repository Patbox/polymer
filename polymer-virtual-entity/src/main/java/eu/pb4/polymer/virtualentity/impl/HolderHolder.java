package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;

import java.util.Collection;

public interface HolderHolder {
    ElementHolder[] ELEMENT_HOLDERS = new ElementHolder[0];
    HolderAttachment[] HOLDER_ATTACHMENTS = new HolderAttachment[0];

    void polymer$addHolder(ElementHolder holder);
    void polymer$removeHolder(ElementHolder holder);
    Collection<ElementHolder> polymer$getHolders();
}
