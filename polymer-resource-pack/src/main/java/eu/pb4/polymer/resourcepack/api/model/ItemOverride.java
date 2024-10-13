package eu.pb4.polymer.resourcepack.api.model;

import com.google.gson.JsonObject;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;

public record ItemOverride(Object2FloatMap<Identifier> predicate, Identifier model) {
    /**
     * All Items
     */
    public static Identifier CUSTOM_MODEL_DATA = Identifier.of("custom_model_data");
    public static Identifier LEFT_HANDED = Identifier.of("lefthanded");
    public static Identifier COOLDOWN = Identifier.of("cooldown");
    public static Identifier DAMAGED = Identifier.of("damaged");
    public static Identifier DAMAGE = Identifier.of("damage");

    /**
     * Bows and crossbows
     */
    public static Identifier PULL = Identifier.of("pull");
    public static Identifier PULLING = Identifier.of("pulling");
    /**
     * Crossbows
     */
    public static Identifier CHARGED = Identifier.of("charged");
    public static Identifier FIREWORK = Identifier.of("firework");
    /**
     * Bundle
     */
    public static Identifier FILLED = Identifier.of("filled");
    /**
     * Clock
     */
    public static Identifier TIME = Identifier.of("time");
    /**
     * Compass
     */
    public static Identifier ANGLE = Identifier.of("angle");
    /**
     * Elytra
     */
    public static Identifier BROKEN = Identifier.of("broken");
    /**
     * Fishing rod
     */
    public static Identifier CAST = Identifier.of("cast");
    /**
     * Shield
     */
    public static Identifier BLOCKING = Identifier.of("blocking");
    /**
     * Trident
     */
    public static Identifier THROWING = Identifier.of("throwing");
    /**
     * Light
     */
    public static Identifier LEVEL = Identifier.of("level");
    /**
     * Goat Horn
     */
    public static Identifier TOOTING = Identifier.of("tooting");

    public static ItemOverride of(Identifier model) {
        return new ItemOverride(new Object2FloatOpenHashMap<>(), model);
    }

    public static ItemOverride of(Identifier model, Identifier key, float value) {
        return of(model).set(key, value);
    }

    public ItemOverride set(Identifier key, float value) {
        this.predicate.put(key, value);
        return this;
    }

    public JsonObject toJson() {
        var object = new JsonObject();
        object.addProperty("model", CommonImplUtils.shortId(this.model));
        var pred = new JsonObject();
        var entries = new ArrayList<>(predicate.keySet());
        entries.sort(Comparator.comparing(x -> x));
        for (var key : entries) {
            pred.addProperty(CommonImplUtils.shortId(key), predicate.getFloat(key));
        }
        object.add("predicate", pred);
        return object;
    }

    public boolean containsPredicate(Identifier key) {
        return this.predicate.containsKey(key);
    }
}
