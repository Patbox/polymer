package eu.pb4.polymertest;

import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.function.Consumers;

import java.util.List;
import java.util.function.Consumer;


public class LeadAttachmentElement extends GenericEntityElement {
    public static final Identifier LEAD = Identifier.of("test", "lead");
    public static final Identifier LEAD_SELF = Identifier.of("test", "lead_self");
    private final float scale;

    public LeadAttachmentElement(float v) {
        this.dataTracker.set(EntityTrackedData.SILENT, true);
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        this.scale = v;
    }


    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        super.startWatching(player, packetConsumer);
        var scale = new EntityAttributeInstance(EntityAttributes.SCALE, Consumers.nop());
        scale.setBaseValue(this.scale);
        packetConsumer.accept(new EntityAttributesS2CPacket(this.getEntityId(), List.of(
                scale
        )));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.VEX;
    }
}