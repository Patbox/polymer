package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;

import java.util.Collection;

public interface HolderAttachmentHolder {
    void polymer$addHolder(HolderAttachment holderAttachment);
    void polymer$removeHolder(HolderAttachment holderAttachment);
    Collection<HolderAttachment> polymer$getHolders();
}
