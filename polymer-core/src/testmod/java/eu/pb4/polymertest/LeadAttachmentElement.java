package eu.pb4.polymertest;

import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import org.apache.commons.lang3.function.Consumers;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;


public class LeadAttachmentElement extends GenericEntityElement {
    public static final Identifier LEAD = Identifier.fromNamespaceAndPath("test", "lead");
    public static final Identifier LEAD_SELF = Identifier.fromNamespaceAndPath("test", "lead_self");
    private final float scale;

    public LeadAttachmentElement(float v) {
        this.dataTracker.set(EntityData.SILENT, true);
        this.dataTracker.set(EntityData.NO_GRAVITY, true);
        this.dataTracker.set(EntityData.FLAGS, (byte) ((1 << EntityData.INVISIBLE_FLAG_INDEX)));
        this.scale = v;
    }


    @Override
    public void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        super.startWatching(player, packetConsumer);
        var scale = new AttributeInstance(Attributes.SCALE, Consumers.nop());
        scale.setBaseValue(this.scale);
        packetConsumer.accept(new ClientboundUpdateAttributesPacket(this.getEntityId(), List.of(
                scale
        )));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.VEX;
    }
}