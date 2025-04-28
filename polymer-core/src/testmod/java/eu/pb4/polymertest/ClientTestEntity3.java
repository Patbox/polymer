package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.core.impl.PolymerImpl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class ClientTestEntity3 extends CreeperEntity implements PolymerEntity, PolymerClientDecoded, PolymerKeepModel {
    public ClientTestEntity3(EntityType<ClientTestEntity3> entityEntityType, World world) {
        super(entityEntityType, world);
    }
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return PolymerImpl.IS_CLIENT ? this.getType() : EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
    }
}
