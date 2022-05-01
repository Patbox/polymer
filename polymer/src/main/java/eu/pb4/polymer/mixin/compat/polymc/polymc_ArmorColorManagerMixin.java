package eu.pb4.polymer.mixin.compat.polymc;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.item.ArmorColorManager;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;

import javax.imageio.ImageIO;
import java.io.IOException;

@Pseudo
@Mixin(value = ArmorColorManager.class, remap = false)
public class polymc_ArmorColorManagerMixin {

    @Shadow @Final private Object2IntArrayMap<ArmorMaterial> material2Color;

    /**
     * @author Patbox
     * @reason v
     * I only needed it to copy textures, rest would collide with polymer's system
     * Maybe we should just extract shared rp stuff as it's own library?
     *
     * Original code by: TheEpicBlock
     */
    @Overwrite
    public void addToResourcePack(ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        if (this.material2Color.isEmpty()) {
            return;
        }

        // Do this entire thing twice for both armor layers
        for (int layer = 1; layer <= 2; layer++) {
            // Collect all modded textures and calculate the size of the output
            for (var material : material2Color.keySet()) {
                try {
                    var texturePath = "models/armor/" + material.getName() + "_layer_" + layer;
                    var texture = moddedResources.getTexture("minecraft", texturePath);
                    if (texture == null) {
                        logger.warn("Couldn't find armor texture for " + material.getName() + ", it won't display correctly when worn");
                        continue;
                    }

                    var moddedImage = ImageIO.read(texture.getTexture());
                    if (moddedImage == null) {
                        logger.warn("Couldn't read layer " + layer + " armor texture for " + material.getName());
                        continue;
                    }

                    // Write the modded armor textures standalone
                    pack.setTexture("minecraft", texturePath, moddedResources.getTexture("minecraft", texturePath));
                } catch (IOException e) {
                    logger.error("Couldn't read armor texture " + material.getName() + " (layer #" + layer + ")");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @author Patbox
     * @reason See above
     */
    @Overwrite
    public boolean hasColor(int c) {
        return PolymerRPUtils.isColorTaken(c);
    }

    /**
     * @author Patbox
     * @reason See above
     */
    @Overwrite
    public int getColorForMaterial(ArmorMaterial material) {
        if (!material2Color.containsKey(material)) {
            var color = PolymerRPUtils.requestArmor(new Identifier("minecraft", material.getName()));
            material2Color.put(material, color.value());
        }
        return material2Color.getInt(material);
    }
}
