package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.mixin.item.IngredientAccessor;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Optional;

public interface IngredientExtension {
    int MAGIC_NUMBER = -0x372ab82;

    void polymer$setPolymerItems(IntList polymerItems);

    record BasePacketCodec(
            PacketCodec<RegistryByteBuf, Ingredient> original) implements PacketCodec<RegistryByteBuf, Ingredient> {
        @Override
        public Ingredient decode(RegistryByteBuf buf) {
            var pos = buf.readerIndex();
            var id = buf.readVarInt();
            if (id != IngredientExtension.MAGIC_NUMBER) {
                buf.readerIndex(pos);
                return original.decode(buf);
            }
            var size = buf.readVarInt() - 1;
            RegistryEntryList<Item> entries;
            if (size == -1) {
                entries = Registries.ITEM.getOrThrow(TagKey.of(RegistryKeys.ITEM, Identifier.PACKET_CODEC.decode(buf)));
            } else {
                var list = new ArrayList<RegistryEntry<Item>>(Math.min(size, 65536));
                for (var i = 0; i < size; ++i) {
                    list.add(Registries.ITEM.getEntry(buf.readVarInt()).orElseThrow());
                }
                entries = RegistryEntryList.of(list);
            }
            Ingredient ingredient;
            if (entries.size() == 0) {
                ingredient = Ingredient.ofItem(Items.COMMAND_BLOCK);
                ((IngredientAccessor) (Object) ingredient).setEntries(entries);
            } else {
                ingredient = Ingredient.ofTag(entries);
            }

            var polymerItems = buf.readIntList();
            //noinspection ConstantValue
            if (((Object) ingredient) instanceof IngredientExtension extension) {
                extension.polymer$setPolymerItems(polymerItems);
            }

            return ingredient;
        }

        @Override
        public void encode(RegistryByteBuf buf, Ingredient value) {
            var context = PacketContext.get().getClientConnection();
            if (context == null || !(PacketContext.get().getEncodedPacket() instanceof CustomPayloadS2CPacket packet)) {
                original.encode(buf, value);
                return;
            }
            var protocol = PolymerNetworking.getMetadata(context, ClientMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.TYPE);
            if (protocol == null || protocol.intValue() != SharedConstants.getProtocolVersion()) {
                original.encode(buf, value);
                return;
            }
            var extendedIngredients = PolymerNetworking.getMetadata(context, ClientMetadataKeys.EXTENDED_RECIPE_INGREDIENTS, NbtByte.TYPE);
            if (extendedIngredients == null || extendedIngredients.byteValue() == 0) {
                original.encode(buf, value);
                return;
            }

            var polymerEntries = value.getMatchingItems().filter(x -> PolymerSyncedObject.getSyncedObject(Registries.ITEM, x.value()) instanceof PolymerItem).findAny();
            if (polymerEntries.isEmpty()) {
                original.encode(buf, value);
                return;
            }
            buf.writeVarInt(IngredientExtension.MAGIC_NUMBER);

            var entries = ((IngredientAccessor) (Object) value).getEntries();

            var polymer = new ArrayList<Item>();

            if (entries.getTagKey().isPresent()) {
                buf.writeVarInt(0);
                buf.writeIdentifier(entries.getTagKey().orElseThrow().id());
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(Registries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    }
                }
            } else {
                var regular = new ArrayList<Item>();
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(Registries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    } else {
                        regular.add(entry.value());
                    }
                }

                buf.writeVarInt(regular.size() + 1);
                for (var entry : regular) {
                    buf.writeVarInt(Registries.ITEM.getRawId(entry));
                }
            }
            buf.writeVarInt(polymer.size());
            for (var entry : polymer) {
                buf.writeVarInt(Registries.ITEM.getRawId(entry));
            }
        }

    }

    record OptionalPacketCodec(
            PacketCodec<RegistryByteBuf, Optional<Ingredient>> original) implements PacketCodec<RegistryByteBuf, Optional<Ingredient>> {
        @Override
        public Optional<Ingredient> decode(RegistryByteBuf buf) {
            var pos = buf.readerIndex();
            var id = buf.readVarInt();
            if (id != IngredientExtension.MAGIC_NUMBER) {
                buf.readerIndex(pos);
                return original.decode(buf);
            }
            var size = buf.readVarInt() - 1;
            RegistryEntryList<Item> entries;
            if (size == -1) {
                entries = Registries.ITEM.getOrThrow(TagKey.of(RegistryKeys.ITEM, Identifier.PACKET_CODEC.decode(buf)));
            } else {
                var list = new ArrayList<RegistryEntry<Item>>(Math.min(size, 65536));
                for (var i = 0; i < size; ++i) {
                    list.add(Registries.ITEM.getEntry(buf.readVarInt()).orElseThrow());
                }
                entries = RegistryEntryList.of(list);
            }
            Ingredient ingredient;
            if (entries.size() == 0) {
                ingredient = Ingredient.ofItem(Items.COMMAND_BLOCK);
                ((IngredientAccessor) (Object) ingredient).setEntries(entries);
            } else {
                ingredient = Ingredient.ofTag(entries);
            }


            var polymerItems = buf.readIntList();
            //noinspection ConstantValue
            if (((Object) ingredient) instanceof IngredientExtension extension) {
                extension.polymer$setPolymerItems(polymerItems);
            }

            return Optional.of(ingredient);
        }

        @Override
        public void encode(RegistryByteBuf buf, Optional<Ingredient> value) {
            if (value.isEmpty()) {
                original.encode(buf, value);
                return;
            }

            var context = PacketContext.get().getClientConnection();
            if (context == null || !(PacketContext.get().getEncodedPacket() instanceof CustomPayloadS2CPacket packet)) {
                original.encode(buf, value);
                return;
            }
            var protocol = PolymerNetworking.getMetadata(context, ClientMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.TYPE);
            if (protocol == null || protocol.intValue() != SharedConstants.getProtocolVersion()) {
                original.encode(buf, value);
                return;
            }
            var extendedIngredients = PolymerNetworking.getMetadata(context, ClientMetadataKeys.EXTENDED_RECIPE_INGREDIENTS, NbtByte.TYPE);
            if (extendedIngredients == null || extendedIngredients.byteValue() == 0) {
                original.encode(buf, value);
                return;
            }

            var polymerEntries = value.orElseThrow().getMatchingItems().filter(x -> PolymerSyncedObject.getSyncedObject(Registries.ITEM, x.value()) instanceof PolymerItem).findAny();
            if (polymerEntries.isEmpty()) {
                original.encode(buf, value);
                return;
            }

            buf.writeVarInt(IngredientExtension.MAGIC_NUMBER);

            var entries = ((IngredientAccessor) (Object) value.orElseThrow()).getEntries();

            var polymer = new ArrayList<Item>();

            if (entries.getTagKey().isPresent()) {
                buf.writeVarInt(0);
                buf.writeIdentifier(entries.getTagKey().orElseThrow().id());
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(Registries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    }
                }
            } else {
                var regular = new ArrayList<Item>();
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(Registries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    } else {
                        regular.add(entry.value());
                    }
                }

                buf.writeVarInt(regular.size() + 1);
                for (var entry : regular) {
                    buf.writeVarInt(Registries.ITEM.getRawId(entry));
                }
            }
            buf.writeVarInt(polymer.size());
            for (var entry : polymer) {
                buf.writeVarInt(Registries.ITEM.getRawId(entry));
            }
        }
    }
}
