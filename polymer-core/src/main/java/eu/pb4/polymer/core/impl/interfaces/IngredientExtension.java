package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.mixin.item.IngredientAccessor;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Optional;

public interface IngredientExtension {
    int MAGIC_NUMBER = -0x372ab82;

    void polymer$setPolymerItems(IntList polymerItems);

    record BaseStreamCodec(
            StreamCodec<RegistryFriendlyByteBuf, Ingredient> original) implements StreamCodec<RegistryFriendlyByteBuf, Ingredient> {
        @Override
        public Ingredient decode(RegistryFriendlyByteBuf buf) {
            var pos = buf.readerIndex();
            var id = buf.readVarInt();
            if (id != IngredientExtension.MAGIC_NUMBER) {
                buf.readerIndex(pos);
                return original.decode(buf);
            }
            var size = buf.readVarInt() - 1;
            HolderSet<Item> entries;
            if (size == -1) {
                entries = BuiltInRegistries.ITEM.getOrThrow(TagKey.create(Registries.ITEM, Identifier.STREAM_CODEC.decode(buf)));
            } else {
                var list = new ArrayList<Holder<Item>>(Math.min(size, 65536));
                for (var i = 0; i < size; ++i) {
                    list.add(BuiltInRegistries.ITEM.get(buf.readVarInt()).orElseThrow());
                }
                entries = HolderSet.direct(list);
            }
            Ingredient ingredient;
            if (entries.size() == 0) {
                ingredient = Ingredient.of(Items.COMMAND_BLOCK);
                ((IngredientAccessor) (Object) ingredient).setEntries(entries);
            } else {
                ingredient = Ingredient.of(entries);
            }

            var polymerItems = buf.readIntIdList();
            //noinspection ConstantValue
            if (((Object) ingredient) instanceof IngredientExtension extension) {
                extension.polymer$setPolymerItems(polymerItems);
            }

            return ingredient;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Ingredient value) {
            var context = PacketContext.get().get(PacketContext.CONNECTION);
            if (context == null || !(PacketContext.get().getEncodedPacket() instanceof ClientboundCustomPayloadPacket packet)) {
                original.encode(buf, value);
                return;
            }
            var protocol = PolymerNetworking.getMetadata(context, ClientMetadataKeys.MINECRAFT_PROTOCOL, IntTag.TYPE);
            if (protocol == null || protocol.intValue() != SharedConstants.getProtocolVersion()) {
                original.encode(buf, value);
                return;
            }
            var extendedIngredients = PolymerNetworking.getMetadata(context, ClientMetadataKeys.EXTENDED_RECIPE_INGREDIENTS, ByteTag.TYPE);
            if (extendedIngredients == null || extendedIngredients.byteValue() == 0) {
                original.encode(buf, value);
                return;
            }

            var polymerEntries = value.items().filter(x -> PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, x.value()) instanceof PolymerItem).findAny();
            if (polymerEntries.isEmpty()) {
                original.encode(buf, value);
                return;
            }
            buf.writeVarInt(IngredientExtension.MAGIC_NUMBER);

            var entries = ((IngredientAccessor) (Object) value).getEntries();

            var polymer = new ArrayList<Item>();

            if (entries.unwrapKey().isPresent()) {
                buf.writeVarInt(0);
                buf.writeIdentifier(entries.unwrapKey().orElseThrow().location());
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    }
                }
            } else {
                var regular = new ArrayList<Item>();
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    } else {
                        regular.add(entry.value());
                    }
                }

                buf.writeVarInt(regular.size() + 1);
                for (var entry : regular) {
                    buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry));
                }
            }
            buf.writeVarInt(polymer.size());
            for (var entry : polymer) {
                buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry));
            }
        }

    }

    record OptionalStreamCodec(
            StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> original) implements StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> {
        @Override
        public Optional<Ingredient> decode(RegistryFriendlyByteBuf buf) {
            var pos = buf.readerIndex();
            var id = buf.readVarInt();
            if (id != IngredientExtension.MAGIC_NUMBER) {
                buf.readerIndex(pos);
                return original.decode(buf);
            }
            var size = buf.readVarInt() - 1;
            HolderSet<Item> entries;
            if (size == -1) {
                entries = BuiltInRegistries.ITEM.getOrThrow(TagKey.create(Registries.ITEM, Identifier.STREAM_CODEC.decode(buf)));
            } else {
                var list = new ArrayList<Holder<Item>>(Math.min(size, 65536));
                for (var i = 0; i < size; ++i) {
                    list.add(BuiltInRegistries.ITEM.get(buf.readVarInt()).orElseThrow());
                }
                entries = HolderSet.direct(list);
            }
            Ingredient ingredient;
            if (entries.size() == 0) {
                ingredient = Ingredient.of(Items.COMMAND_BLOCK);
                ((IngredientAccessor) (Object) ingredient).setEntries(entries);
            } else {
                ingredient = Ingredient.of(entries);
            }


            var polymerItems = buf.readIntIdList();
            //noinspection ConstantValue
            if (((Object) ingredient) instanceof IngredientExtension extension) {
                extension.polymer$setPolymerItems(polymerItems);
            }

            return Optional.of(ingredient);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Optional<Ingredient> value) {
            if (value.isEmpty()) {
                original.encode(buf, value);
                return;
            }

            var context = PacketContext.get().get(PacketContext.CONNECTION);
            if (context == null || !(PacketContext.get().getEncodedPacket() instanceof ClientboundCustomPayloadPacket packet)) {
                original.encode(buf, value);
                return;
            }
            var protocol = PolymerNetworking.getMetadata(context, ClientMetadataKeys.MINECRAFT_PROTOCOL, IntTag.TYPE);
            if (protocol == null || protocol.intValue() != SharedConstants.getProtocolVersion()) {
                original.encode(buf, value);
                return;
            }
            var extendedIngredients = PolymerNetworking.getMetadata(context, ClientMetadataKeys.EXTENDED_RECIPE_INGREDIENTS, ByteTag.TYPE);
            if (extendedIngredients == null || extendedIngredients.byteValue() == 0) {
                original.encode(buf, value);
                return;
            }

            var polymerEntries = value.orElseThrow().items().filter(x -> PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, x.value()) instanceof PolymerItem).findAny();
            if (polymerEntries.isEmpty()) {
                original.encode(buf, value);
                return;
            }

            buf.writeVarInt(IngredientExtension.MAGIC_NUMBER);

            var entries = ((IngredientAccessor) (Object) value.orElseThrow()).getEntries();

            var polymer = new ArrayList<Item>();

            if (entries.unwrapKey().isPresent()) {
                buf.writeVarInt(0);
                buf.writeIdentifier(entries.unwrapKey().orElseThrow().location());
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    }
                }
            } else {
                var regular = new ArrayList<Item>();
                for (var entry : entries) {
                    if ((PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, entry.value()) instanceof PolymerItem)) {
                        polymer.add(entry.value());
                    } else {
                        regular.add(entry.value());
                    }
                }

                buf.writeVarInt(regular.size() + 1);
                for (var entry : regular) {
                    buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry));
                }
            }
            buf.writeVarInt(polymer.size());
            for (var entry : polymer) {
                buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry));
            }
        }
    }
}
