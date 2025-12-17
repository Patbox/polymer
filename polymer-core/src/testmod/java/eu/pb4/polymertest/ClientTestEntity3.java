package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.core.impl.PolymerImpl;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class ClientTestEntity3 extends Creeper implements PolymerEntity, PolymerClientDecoded, PolymerKeepModel {
    public ClientTestEntity3(EntityType<ClientTestEntity3> entityEntityType, Level world) {
        super(entityEntityType, world);
    }
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return PolymerImpl.IS_CLIENT ? this.getType() : EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
    }
}
