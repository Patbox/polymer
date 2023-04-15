package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;

public record VoidUpdateType() implements HolderAttachment.UpdateType {
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
