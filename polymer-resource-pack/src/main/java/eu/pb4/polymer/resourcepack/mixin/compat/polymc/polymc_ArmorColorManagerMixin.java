package eu.pb4.polymer.resourcepack.mixin.compat.polymc;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.item.ArmorColorManager;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;

import javax.imageio.ImageIO;

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
        for (var bool : new boolean[] { false, true }) {
            // Collect all modded textures and calculate the size of the output
            for (var material : material2Color.keySet()) {
                for (var layer : material.layers()) {
                    var id = layer.getTexture(bool);
                    try {
                        assert id != null;
                        var texturePath = id.getPath().substring("textures/".length(), id.getPath().length() - ".png".length());
                        var texture = moddedResources.getTexture(id.getNamespace(), texturePath);
                        if (texture == null) {
                            logger.warn("Couldn't find armor texture for " + id + ", it won't display correctly when worn");
                            continue;
                        }

                        var moddedImage = ImageIO.read(texture.getTexture());
                        if (moddedImage == null) {
                            logger.warn("Couldn't read layer " + layer + " armor texture for " + id);
                            continue;
                        }

                        // Write the modded armor textures standalone
                        pack.setTexture(id.getNamespace(), texturePath, moddedResources.getTexture(id.getNamespace(), texturePath));
                    } catch (Throwable e) {
                        logger.error("Couldn't read armor texture " + id + " (layer #" + layer + ")");
                        e.printStackTrace();
                    }
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
        return PolymerResourcePackUtils.isColorTaken(c);
    }

    /**
     * @author Patbox
     * @reason See above
     */
    @Overwrite
    public int getColorForMaterial(ArmorMaterial material) {
        if (!material2Color.containsKey(material)) {
            var color = PolymerResourcePackUtils.requestArmor(Registries.ARMOR_MATERIAL.getEntry(material));
            material2Color.put(material, color.color());
        }
        return material2Color.getInt(material);
    }
}
