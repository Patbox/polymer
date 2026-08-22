package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolymerTagHacks {
    public static final HashMap<Identifier, Map<Identifier, TagReplacer>> REPLACERS = new HashMap<>();
    public static final HashMap<TagKey<?>, TagKey<?>> REAL_TO_FAKE = new HashMap<>();
    public static final HashMap<Identifier, Map<Identifier, List<Object>>> FAKE_ENTRIES = new HashMap<>();
    public static final HashMap<TagKey<?>, TagKey<?>> FAKE_TO_REAL = new HashMap<>();

    private static boolean bundleHackDisabled = true;

    public static void moveRealTagKey(TagKey<?> original, Identifier target, boolean keepEntries) {
        var fake = TagKey.create(original.registry(), target);
        REAL_TO_FAKE.put(original, fake);
        FAKE_TO_REAL.put(fake, original);
        REPLACERS.computeIfAbsent(original.registry().identifier(), _ -> new HashMap<>()).put(original.location(), new TagReplacer(target, keepEntries));
    }

    public static <T> void addFakeTagEntry(TagKey<T> key, T entry) {
        FAKE_ENTRIES.computeIfAbsent(key.registry().identifier(), _ -> new HashMap<>()).computeIfAbsent(key.location(), _ -> new ArrayList<>()).add(entry);
    }

    public static Item enableAndGetFakeBundleItem() {
        if (bundleHackDisabled) {
            moveRealTagKey(ItemTags.BUNDLES, PolymerImplUtils.id("minecraft/bundles"), true);
            addFakeTagEntry(ItemTags.BUNDLES, Items.MUSIC_DISC_CHIRP);
            bundleHackDisabled = false;
        }

        return Items.MUSIC_DISC_CHIRP;
    }


    public record TagReplacer(Identifier target, boolean keepEntries) {

    }
}
