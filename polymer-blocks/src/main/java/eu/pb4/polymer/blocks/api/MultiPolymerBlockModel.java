package eu.pb4.polymer.blocks.api;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record MultiPolymerBlockModel(List<PolymerBlockModel[]> models) {
    public static MultiPolymerBlockModel of() {
        return new MultiPolymerBlockModel(new ArrayList<>());
    }


    public MultiPolymerBlockModel with(PolymerBlockModel... models) {
        this.models.add(models);
        return this;
    }

    public MultiPolymerBlockModel with(Identifier identifier) {
        this.models.add(new PolymerBlockModel[]{ PolymerBlockModel.of(identifier) });
        return this;
    }
}
