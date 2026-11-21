package com.karl.playertagger.command;

import com.karl.playertagger.PlayerTaggerMod;
import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.gui.GuiAddPlayerStep1;
import com.karl.playertagger.gui.GuiTagList;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import org.polyfrost.oneconfig.api.commands.v1.CommandManager;
import org.polyfrost.oneconfig.api.platform.v1.Platform;
import org.polyfrost.oneconfig.internal.legacy.command.FabricClientCommandSource;
import org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt;

public class TagCommand {

    public static void register() {
        registerAlias("tag");
        registerAlias("playertag");
        registerAlias("sweatradar");
        registerAlias("ptag");
    }

    private static void registerAlias(String rootName) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = CommandManager.literal(rootName)
            .executes(ctx -> {
                Platform.screen().display(new GuiTagList(null));
                return 1;
            })
            .then(CommandManager.literal("menu").executes(ctx -> {
                Platform.screen().display(new GuiTagList(null));
                return 1;
            }))
            .then(CommandManager.literal("list").executes(ctx -> {
                Platform.screen().display(new GuiTagList(null));
                return 1;
            }))
            .then(CommandManager.literal("add")
                .executes(ctx -> {
                    Platform.screen().display(new GuiAddPlayerStep1(null, ""));
                    return 1;
                })
                .then(CommandManager.INSTANCE.argument("player", StringArgumentType.word())
                    .suggests((ctx, suggestionsBuilder) -> {
                        suggestOnlinePlayers(suggestionsBuilder);
                        return suggestionsBuilder.buildFuture();
                    })
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "player");
                        Platform.screen().display(new GuiAddPlayerStep1(null, name));
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("remove")
                .then(CommandManager.INSTANCE.argument("player", StringArgumentType.word())
                    .suggests((ctx, suggestionsBuilder) -> {
                        suggestOnlinePlayers(suggestionsBuilder);
                        return suggestionsBuilder.buildFuture();
                    })
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "player");
                        removePlayerByName(name);
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("del")
                .then(CommandManager.INSTANCE.argument("player", StringArgumentType.word())
                    .suggests((ctx, suggestionsBuilder) -> {
                        suggestOnlinePlayers(suggestionsBuilder);
                        return suggestionsBuilder.buildFuture();
                    })
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "player");
                        removePlayerByName(name);
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("sync").executes(ctx -> {
                sendChatMessage("§e[PlayerTagger] Synchronisiere mit Google Firebase...");
                TagManager.getInstance().syncFromFirebase(true);
                return 1;
            }))
            .then(CommandManager.literal("firebase").executes(ctx -> {
                sendChatMessage("§e[PlayerTagger] Synchronisiere mit Google Firebase...");
                TagManager.getInstance().syncFromFirebase(true);
                return 1;
            }))
            .then(CommandManager.literal("config").executes(ctx -> {
                ScreensKt.openUI(PlayerTaggerMod.config);
                return 1;
            }))
            .then(CommandManager.literal("settings").executes(ctx -> {
                ScreensKt.openUI(PlayerTaggerMod.config);
                return 1;
            }))
            .then(CommandManager.literal("oneconfig").executes(ctx -> {
                ScreensKt.openUI(PlayerTaggerMod.config);
                return 1;
            }))
            .then(CommandManager.literal("help").executes(ctx -> {
                printHelp();
                return 1;
            }));

        CommandManager.INSTANCE.register(builder);
    }

    private static void removePlayerByName(String name) {
        boolean removed = TagManager.getInstance().removePlayer(name);
        if (removed) {
            sendChatMessage("§a[PlayerTagger] Spieler '§e" + name + "§a' erfolgreich aus der Liste entfernt!");
        } else {
            sendChatMessage("§c[PlayerTagger] Spieler '§e" + name + "§c' wurde nicht gefunden.");
        }
    }

    private static void suggestOnlinePlayers(com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfoMap() != null) {
                String remaining = builder.getRemaining().toLowerCase();
                for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                    if (info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                        String name = info.getGameProfile().getName();
                        if (name.toLowerCase().startsWith(remaining)) {
                            builder.suggest(name);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void printHelp() {
        sendChatMessage("§6--- PlayerTagger Befehle ---");
        sendChatMessage("§e/tag §7- Öffnet die Spieler-Liste");
        sendChatMessage("§e/tag add [Name] §7- Startet den Assistenten");
        sendChatMessage("§e/tag remove <Name> §7- Löscht einen Spieler");
        sendChatMessage("§e/tag sync §7- Synchronisiert die Shared-Liste");
        sendChatMessage("§e/tag config §7- Öffnet OneConfig");
    }

    private static void sendChatMessage(String text) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(text));
        }
    }
}
