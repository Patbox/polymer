package eu.pb4.polymer.core.impl.networking.entry;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface WritableEntry {
    void write(PacketByteBuf buf, int version);


    interface Reader {
        WritableEntry read(PacketByteBuf buf, int version);
    }

}
