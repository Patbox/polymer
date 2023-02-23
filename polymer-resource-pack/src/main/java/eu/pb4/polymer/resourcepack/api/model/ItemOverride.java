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
    public static Identifier CUSTOM_MODEL_DATA = new Identifier("custom_model_data");
    public static Identifier LEFT_HANDED = new Identifier("lefthanded");
    public static Identifier COOLDOWN = new Identifier("cooldown");
    public static Identifier DAMAGED = new Identifier("damaged");
    public static Identifier DAMAGE = new Identifier("damage");

    /**
     * Bows and crossbows
     */
    public static Identifier PULL = new Identifier("pull");
    public static Identifier PULLING = new Identifier("pulling");
    /**
     * Crossbows
     */
    public static Identifier CHARGED = new Identifier("charged");
    public static Identifier FIREWORK = new Identifier("firework");
    /**
     * Bundle
     */
    public static Identifier FILLED = new Identifier("filled");
    /**
     * Clock
     */
    public static Identifier TIME = new Identifier("time");
    /**
     * Compass
     */
    public static Identifier ANGLE = new Identifier("angle");
    /**
     * Elytra
     */
    public static Identifier BROKEN = new Identifier("broken");
    /**
     * Fishing rod
     */
    public static Identifier CAST = new Identifier("cast");
    /**
     * Shield
     */
    public static Identifier BLOCKING = new Identifier("blocking");
    /**
     * Trident
     */
    public static Identifier THROWING = new Identifier("throwing");
    /**
     * Light
     */
    public static Identifier LEVEL = new Identifier("level");
    /**
     * Goat Horn
     */
    public static Identifier TOOTING = new Identifier("tooting");

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
}
