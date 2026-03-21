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
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import java.util.*;

import static net.minecraft.commands.Commands.literal;

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

        var entries = new ArrayList<>(state.getValues().toList());
        entries.sort(Comparator.comparing(x -> x.property().getName()));
        var iterator = entries.iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            stringBuilder.append(entry.property().getName()).append("=").append(entry.valueName());

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
                    if (ctx.getSource().getPlayer() != null) {
                        var text = Component.empty();
                        for (var type : BlockModelType.values()) {
                            text.append("- " + type + " -> " + PolymerBlockResourceUtils.getBlocksLeft(type) + " / " + DefaultModelData.USABLE_STATES.get(type).size() + "\n");
                        }
                        ctx.getSource().getPlayerOrException().openDialog(Holder.direct(new NoticeDialog(new CommonDialogData(
                                Component.literal("States of blockstates provided by polymer-blocks module"),
                                Optional.empty(),
                                true,
                                 true,
                                DialogAction.CLOSE,
                                List.of(new PlainMessage(text, 300)),
                                List.of()
                        ), new ActionButton(new CommonButtonData(CommonComponents.GUI_OK, CommonButtonData.DEFAULT_WIDTH), Optional.empty()))));
                    } else {
                        ctx.getSource().sendSystemMessage(Component.literal("States of blockstates provided by polymer-blocks module:"));

                        for (var type : BlockModelType.values()) {
                            ctx.getSource().sendSystemMessage(Component.literal("- " + type + " -> " + PolymerBlockResourceUtils.getBlocksLeft(type) + " / " + DefaultModelData.USABLE_STATES.get(type).size()));
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
