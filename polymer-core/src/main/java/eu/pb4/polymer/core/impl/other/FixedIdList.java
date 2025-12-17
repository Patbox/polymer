package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.core.mixin.other.IdMapperAccessor;
import net.minecraft.core.IdMapper;

public class FixedIdList<T> extends IdMapper<T> {

    @Override
    public int size() {
        return ((IdMapperAccessor) (Object) this).getIdToT().size();
    }

    public int mapSize() {
        return super.size();
    }
}
