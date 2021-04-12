package eu.pb4.polymer.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.stream.Collectors;

public class ItemHelper {
    protected static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    public static String VIRTUAL_ITEM_ID = "Polymer$itemId";
    public static String REAL_TAG = "Polymer$itemTag";

    public static Style CLEAN_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);
    public static Style NON_ITALIC_STYLE = Style.EMPTY.withItalic(false);

    public static ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        if (itemStack.getItem() instanceof VirtualItem) {
            VirtualItem item = (VirtualItem) itemStack.getItem();

            return item.getVirtualItemStack(itemStack, player);
        }

        return itemStack;
    }

    public static ItemStack getRealItemStack(ItemStack itemStack) {
        ItemStack out = itemStack;

        String id = out.getOrCreateTag().getString(VIRTUAL_ITEM_ID);

        if (id != null) {
            try {
                Identifier identifier = Identifier.tryParse(id);
                Item item = Registry.ITEM.get(identifier);

                if (item instanceof VirtualItem) {
                    out = new ItemStack(item, itemStack.getCount());
                    CompoundTag tag = itemStack.getSubTag(REAL_TAG);
                    if (tag != null) {
                        out.setTag(tag);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return out;
    }


    protected static List<Text> buildTooltip(ItemStack stack, ServerPlayerEntity player) {
        List<Text> list = Lists.newArrayList();
        int hideFlags = getHideFlags(stack);

        int j;
        if (stack.hasTag()) {
            if (isSectionHidden(hideFlags, ItemStack.TooltipSection.ENCHANTMENTS)) {
                for (int u = 0; u < stack.getEnchantments().size(); ++u) {
                    CompoundTag compoundTag = stack.getEnchantments().getCompound(hideFlags);
                    Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(compoundTag.getString("id"))).ifPresent((e) -> list.add(e.getName(compoundTag.getInt("lvl"))));
                }
            }

            if (stack.getTag() != null && stack.getTag().contains("display", 10)) {
                CompoundTag compoundTag = stack.getTag().getCompound("display");

                if (compoundTag.getType("Lore") == 9) {
                    ListTag listTag = compoundTag.getList("Lore", 8);

                    for (j = 0; j < listTag.size(); ++j) {
                        String string = listTag.getString(j);

                        try {
                            MutableText mutableText2 = Text.Serializer.fromJson(string);
                            if (mutableText2 != null) {
                                list.add(Texts.setStyleIfAbsent(mutableText2, Style.EMPTY.withItalic(true).withColor(Formatting.DARK_PURPLE)));
                            }
                        } catch (JsonParseException var19) {
                            compoundTag.remove("Lore");
                        }
                    }
                }
            }
        }

        int l;
        if (isSectionHidden(hideFlags, ItemStack.TooltipSection.MODIFIERS)) {
            EquipmentSlot[] var20 = EquipmentSlot.values();
            l = var20.length;

            for (j = 0; j < l; ++j) {
                EquipmentSlot equipmentSlot = var20[j];
                Multimap<EntityAttribute, EntityAttributeModifier> multimap = stack.getAttributeModifiers(equipmentSlot);
                if (!multimap.isEmpty()) {
                    list.add(LiteralText.EMPTY);
                    list.add((new TranslatableText("item.modifiers." + equipmentSlot.getName())).formatted(Formatting.GRAY));

                    for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
                        EntityAttributeModifier entityAttributeModifier = entry.getValue();
                        double value = entityAttributeModifier.getValue();
                        boolean bl = false;
                        if (player != null) {
                            if (entityAttributeModifier.getId().equals(ATTACK_DAMAGE_MODIFIER_ID)) {
                                value += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                                value += EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
                                bl = true;
                            } else if (entityAttributeModifier.getId().equals(ATTACK_SPEED_MODIFIER_ID)) {
                                value += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
                                bl = true;
                            }
                        }

                        double g;
                        if (entityAttributeModifier.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_BASE && entityAttributeModifier.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
                            if ((entry.getKey()).equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                                g = value * 10.0D;
                            } else {
                                g = value;
                            }
                        } else {
                            g = value * 100.0D;
                        }

                        if (bl) {
                            list.add((new LiteralText(" ")).append(new TranslatableText("attribute.modifier.equals." + entityAttributeModifier.getOperation().getId(), new Object[]{ItemStack.MODIFIER_FORMAT.format(g), new TranslatableText(entry.getKey().getTranslationKey())})).formatted(Formatting.DARK_GREEN));
                        } else if (value > 0.0D) {
                            list.add((new TranslatableText("attribute.modifier.plus." + entityAttributeModifier.getOperation().getId(), new Object[]{ItemStack.MODIFIER_FORMAT.format(g), new TranslatableText(entry.getKey().getTranslationKey())})).formatted(Formatting.BLUE));
                        } else if (value < 0.0D) {
                            g *= -1.0D;
                            list.add((new TranslatableText("attribute.modifier.take." + entityAttributeModifier.getOperation().getId(), new Object[]{ItemStack.MODIFIER_FORMAT.format(g), new TranslatableText(entry.getKey().getTranslationKey())})).formatted(Formatting.RED));
                        }
                    }
                }
            }
        }

        if (stack.hasTag()) {
            if (isSectionHidden(hideFlags, ItemStack.TooltipSection.UNBREAKABLE) && stack.getTag().getBoolean("Unbreakable")) {
                list.add((new TranslatableText("item.unbreakable")).formatted(Formatting.BLUE));
            }

            ListTag listTag3;
            if (isSectionHidden(hideFlags, ItemStack.TooltipSection.CAN_DESTROY) && stack.getTag().contains("CanDestroy", 9)) {
                listTag3 = stack.getTag().getList("CanDestroy", 8);
                if (!listTag3.isEmpty()) {
                    list.add(LiteralText.EMPTY);
                    list.add((new TranslatableText("item.canBreak")).formatted(Formatting.GRAY));

                    for (l = 0; l < listTag3.size(); ++l) {
                        list.addAll(parseBlockTag(listTag3.getString(l)));
                    }
                }
            }

            if (isSectionHidden(hideFlags, ItemStack.TooltipSection.CAN_PLACE) && stack.getTag().contains("CanPlaceOn", 9)) {
                listTag3 = stack.getTag().getList("CanPlaceOn", 8);
                if (!listTag3.isEmpty()) {
                    list.add(LiteralText.EMPTY);
                    list.add((new TranslatableText("item.canPlace")).formatted(Formatting.GRAY));

                    for (l = 0; l < listTag3.size(); ++l) {
                        list.addAll(parseBlockTag(listTag3.getString(l)));
                    }
                }
            }
        }
        return list;
    }

    private static Collection<Text> parseBlockTag(String tag) {
        try {
            BlockArgumentParser blockArgumentParser = (new BlockArgumentParser(new StringReader(tag), true)).parse(true);
            BlockState blockState = blockArgumentParser.getBlockState();
            Identifier identifier = blockArgumentParser.getTagId();
            boolean bl = blockState != null;
            boolean bl2 = identifier != null;
            if (bl || bl2) {
                if (bl) {
                    return Lists.newArrayList(new Text[]{blockState.getBlock().getName().formatted(Formatting.DARK_GRAY)});
                }

                Tag<Block> tag2 = BlockTags.getTagGroup().getTag(identifier);
                if (tag2 != null) {
                    Collection<Block> collection = tag2.values();
                    if (!collection.isEmpty()) {
                        return collection.stream().map(Block::getName).map((text) -> {
                            return text.formatted(Formatting.DARK_GRAY);
                        }).collect(Collectors.toList());
                    }
                }
            }
        } catch (CommandSyntaxException var8) {
        }

        return Lists.newArrayList(new Text[]{(new LiteralText("missingno")).formatted(Formatting.DARK_GRAY)});
    }


    private static boolean isSectionHidden(int flags, ItemStack.TooltipSection tooltipSection) {
        return (flags & tooltipSection.getFlag()) == 0;
    }

    public static int getHideFlags(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : 0;
    }
}
