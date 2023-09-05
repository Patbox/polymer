package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements CommonResourcePackInfoHolder {
    @Shadow public ServerPlayNetworkHandler networkHandler;
    @Unique private boolean polymerCommon$hasResourcePack;

    @Override
    public boolean polymerCommon$hasResourcePack() {
        if (this.networkHandler != null) {
            return ((CommonResourcePackInfoHolder) this.networkHandler).polymerCommon$hasResourcePack();
        }

        return this.polymerCommon$hasResourcePack;
    }

    @Override
    public void polymerCommon$setResourcePack(boolean value) {
        if (this.networkHandler != null) {
            ((CommonResourcePackInfoHolder) this.networkHandler).polymerCommon$setResourcePack(value);
        }

        this.polymerCommon$hasResourcePack = value;
    }

    @Override
    public void polymerCommon$setResourcePackNoEvent(boolean value) {
        if (this.networkHandler != null) {
            ((CommonResourcePackInfoHolder) this.networkHandler).polymerCommon$setResourcePackNoEvent(value);
        }

        this.polymerCommon$hasResourcePack = value;
    }

    @Override
    public void polymerCommon$setIgnoreNextResourcePack() {

    }
}
