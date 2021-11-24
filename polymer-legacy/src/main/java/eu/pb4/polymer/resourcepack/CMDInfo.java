package eu.pb4.polymer.resourcepack;

import com.google.common.collect.ImmutableList;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;

@Deprecated
public record CMDInfo(Item item, int value, Identifier modelPath) {
    public PolymerModelData toNew() {
        return new PolymerModelData(item, value, modelPath);
    }

    public static final CMDInfo fromNew(PolymerModelData info) {
        return new CMDInfo(info.item(), info.value(), info.modelPath());
    }

    public static final List<CMDInfo> fromNew(List<PolymerModelData> infos) {
        var list = new ImmutableList.Builder<CMDInfo>();

        for (var info : infos) {
            list.add(fromNew(info));
        }

        return list.build();
    }
}
