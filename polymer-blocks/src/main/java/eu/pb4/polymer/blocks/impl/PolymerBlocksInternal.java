package eu.pb4.polymer.blocks.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.MultiPolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class PolymerBlocksInternal implements ModInitializer {
    public static Map<BlockState, Either<PolymerBlockModel[], MultiPolymerBlockModel>> modelMap = Collections.emptyMap();



    public static JsonArray createJsonElement(PolymerBlockModel[] models) {
        var array = new JsonArray();

        for (var model : models) {
            var modelObj = new JsonObject();

            modelObj.addProperty("model", model.model().toString());
            modelObj.addProperty("x", model.x());
            modelObj.addProperty("y", model.y());
            modelObj.addProperty("uvlock", model.uvLock());
            modelObj.addProperty("weight", model.weight());

            array.add(modelObj);
        }

        return array;
    }

    public static String generateStateName(BlockState state) {
        var stringBuilder = new StringBuilder();

        var entries = new ArrayList<>(state.getEntries().entrySet());
        entries.sort(Map.Entry.comparingByKey(Comparator.comparing(Property::getName)));
        var iterator = entries.iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            stringBuilder.append((entry.getKey()).getName()).append("=").append(((Property) entry.getKey()).name(entry.getValue()));

            if (iterator.hasNext()) {
                stringBuilder.append(",");
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void onInitialize() {
        CommonImplUtils.registerCommands(c -> c.then(literal("blocks_module_state_report")
                .requires(CommonImplUtils.permission("blocks_module_state_report", 3))
                .executes(ctx -> {
                    if (CompatStatus.POLYMC) {
                        ctx.getSource().sendMessage(Text.literal("PolyMc is present! Values provided here won't reflect it's state here! Use /polymc command instead!").formatted(Formatting.RED));
                    }
                    if (ctx.getSource().getPlayer() != null) {
                        var text = Text.empty();
                        for (var type : BlockModelType.values()) {
                            text.append("- " + type + " -> " + PolymerBlockResourceUtils.getBlocksLeft(type) + " / " + DefaultModelData.USABLE_STATES.get(type).size() + "\n");
                        }
                        ctx.getSource().getPlayerOrThrow().openDialog(RegistryEntry.of(new NoticeDialog(new DialogCommonData(
                                Text.literal("States of blockstates provided by polymer-blocks module"),
                                Optional.empty(),
                                true,
                                 true,
                                AfterAction.CLOSE,
                                List.of(new PlainMessageDialogBody(text, 300)),
                                List.of()
                        ), new DialogActionButtonData(new DialogButtonData(ScreenTexts.OK, DialogButtonData.DEFAULT_WIDTH), Optional.empty()))));
                    } else {
                        ctx.getSource().sendMessage(Text.literal("States of blockstates provided by polymer-blocks module:"));

                        for (var type : BlockModelType.values()) {
                            ctx.getSource().sendMessage(Text.literal("- " + type + " -> " + PolymerBlockResourceUtils.getBlocksLeft(type) + " / " + DefaultModelData.USABLE_STATES.get(type).size()));
                        }
                    }

                    return 0;
                })));

        /*
        var text = new StringBuilder();

        for (var b : Registries.BLOCK) {
            var x = Registries.BLOCK.getId(b);
            if (x.getNamespace().equals("minecraft") && x.getPath().endsWith("_slab") && !x.getPath().contains("smooth_stone")) {
                var base = x.getPath().substring(0, x.getPath().length() - "_slab".length());
                var other = base + (Registries.BLOCK.containsId(x.withPath(base + "_planks")) ? "_planks" : "");

                text.append("new Pair<>(Blocks.")
                        .append(x.getPath().toUpperCase(Locale.ROOT))
                        .append(", Blocks.")
                        .append(other.toUpperCase(Locale.ROOT))
                        .append("),\n");
            }
        }

        System.out.println(text);
        */
    }
}
