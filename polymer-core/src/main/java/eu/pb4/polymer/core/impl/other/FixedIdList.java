package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.core.mixin.other.IdListAccessor;
import net.minecraft.util.collection.IdList;

public class FixedIdList<T> extends IdList<T> {

    @Override
    public int size() {
        return ((IdListAccessor) (Object) this).getList().size();
    }

    public int mapSize() {
        return super.size();
    }
}
