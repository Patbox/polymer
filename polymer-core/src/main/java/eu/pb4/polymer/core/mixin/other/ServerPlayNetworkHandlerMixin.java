package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.nbt.NbtByte;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PolymerPlayNetworkHandlerExtension {
    @Shadow
    public ServerPlayerEntity player;
    @Unique
    private boolean polymer$advancedTooltip = false;
    @Unique
    private BlockMapper polymer$blockMapper;
    @Unique
    private final List<Runnable> polymer$afterSequence = new ArrayList<>();

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Shadow private int sequence;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer$setupInitial(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        this.polymer$blockMapper = BlockMapper.getDefault(player);
        var advTool = PolymerNetworking.getMetadata(connection, ClientMetadataKeys.ADVANCED_TOOLTIP, NbtByte.TYPE);

        this.polymer$advancedTooltip = advTool != null && advTool.intValue() > 0;
    }


    @Override
    public BlockMapper polymer$getBlockMapper() {
        return this.polymer$blockMapper;
    }

    @Override
    public void polymer$setBlockMapper(BlockMapper mapper) {
        this.polymer$blockMapper = mapper;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void polymer$sendSequencePackets(CallbackInfo ci) {
        if (!this.polymer$afterSequence.isEmpty()) {
            for (var entry : this.polymer$afterSequence) {
                entry.run();
            }
            this.polymer$afterSequence.clear();
        }
    }

    @Override
    public void polymer$setAdvancedTooltip(boolean value) {
        this.polymer$advancedTooltip = value;
    }

    @Override
    public boolean polymer$advancedTooltip() {
        return this.polymer$advancedTooltip;
    }

    @Override
    public void polymer$delayAfterSequence(Runnable runnable) {
        if (this.sequence == -1) {
            runnable.run();
        } else {
            this.polymer$afterSequence.add(runnable);
        }
    }
}
