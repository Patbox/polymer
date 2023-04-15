package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;

public record SimpleUpdateType(String value) implements HolderAttachment.UpdateType {
}
