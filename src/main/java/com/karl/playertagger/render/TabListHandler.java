package com.karl.playertagger.render;

import com.karl.playertagger.config.PlayerTaggerConfig;
import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.data.TaggedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TabListHandler {

    public static final TabListHandler INSTANCE = new TabListHandler();

    private static final Set<String> modifiedPlayerNames = new HashSet<String>();
    private static final Map<String, IChatComponent> originalDisplayNames = new HashMap<String, IChatComponent>();

    public void onWorldChange() {
        // Clear caches when switching servers, lobbies or loading a new game world
        originalDisplayNames.clear();
        modifiedPlayerNames.clear();
    }

    public void onClientTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.getNetHandler() == null) return;

        boolean active = PlayerTaggerConfig.enabled && PlayerTaggerConfig.tabListTagging;
        TagManager tagManager = TagManager.getInstance();

        if (!active || !tagManager.hasAnyTags()) {
            if (!modifiedPlayerNames.isEmpty()) {
                resetAllModifiedNames();
            }
            return;
        }

        // Check every 5 ticks (0.25s) for instant response when players join/leave/update
        if (mc.theWorld.getTotalWorldTime() % 5 == 0) {
            updateTabDisplayNames(tagManager);
        }
    }

    private void updateTabDisplayNames(TagManager tagManager) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getNetHandler() == null) return;

            Collection<NetworkPlayerInfo> playerInfoMap = mc.getNetHandler().getPlayerInfoMap();
            if (playerInfoMap == null || playerInfoMap.isEmpty()) return;

            NetworkPlayerInfo[] infoArray;
            synchronized (playerInfoMap) {
                infoArray = playerInfoMap.toArray(new NetworkPlayerInfo[0]);
            }

            for (NetworkPlayerInfo info : infoArray) {
                if (info == null || info.getGameProfile() == null) continue;
                String realName = info.getGameProfile().getName();
                if (realName == null) continue;

                String lowerName = realName.toLowerCase();
                TaggedPlayer tag = tagManager.getTaggedPlayer(realName);

                if (tag != null) {
                    IChatComponent origComp = originalDisplayNames.get(lowerName);
                    IChatComponent currentComp = info.getDisplayName();

                    // Detect if the server updated the player's display name (e.g. team join/leave/spectator status).
                    // If currentComp does not contain our tag color, icon, or sub-badges, it's a new original from the server!
                    if (currentComp != null) {
                        String currentFormatted = currentComp.getFormattedText();
                        boolean isModifiedByUs = currentFormatted.contains(tag.getColorCode())
                                || (!tag.getIcon().isEmpty() && currentFormatted.contains(tag.getIcon()))
                                || (!tag.getGameMode().isEmpty() && currentFormatted.contains(tag.getGameMode()))
                                || (!tag.getCustomInfo().isEmpty() && currentFormatted.contains(tag.getCustomInfo()));

                        if (!isModifiedByUs) {
                            origComp = currentComp;
                            originalDisplayNames.put(lowerName, origComp);
                        }
                    }

                    String origFormatted = (origComp != null) ? origComp.getFormattedText() : null;
                    String prefix = "";
                    String suffix = "";

                    if (origFormatted != null && !origFormatted.isEmpty()) {
                        int nameIndex = origFormatted.toLowerCase().indexOf(realName.toLowerCase());
                        if (nameIndex != -1) {
                            prefix = origFormatted.substring(0, nameIndex);
                            suffix = origFormatted.substring(nameIndex + realName.length());
                        }
                    } else if (info.getPlayerTeam() != null) {
                        String formattedTeam = ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), realName);
                        int nameIndex = formattedTeam.toLowerCase().indexOf(realName.toLowerCase());
                        if (nameIndex != -1) {
                            prefix = formattedTeam.substring(0, nameIndex);
                            suffix = formattedTeam.substring(nameIndex + realName.length());
                        }
                    }

                    StringBuilder formatted = new StringBuilder(64);
                    // 1. Keep original prefix (e.g. team colors or letter prefix like "A ")
                    formatted.append(prefix);

                    // 2. Icon prefix inside team/rank boundaries
                    formatted.append(tag.getColorCode());
                    if (!tag.getIcon().isEmpty()) {
                        formatted.append("[").append(tag.getIcon()).append("] ");
                    }

                    // 3. Colored Name
                    formatted.append(tag.getColorCode()).append(realName).append("§r");

                    // 4. Keep original suffix
                    formatted.append(suffix);

                    // 5. Extra Info
                    StringBuilder subInfo = new StringBuilder(32);
                    if (!tag.getGameMode().isEmpty()) {
                        subInfo.append(tag.getGameMode());
                    }
                    if (!tag.getCustomInfo().isEmpty()) {
                        if (subInfo.length() > 0) subInfo.append(" • ");
                        subInfo.append(tag.getCustomInfo());
                    }

                    if (subInfo.length() > 0) {
                        formatted.append(" §7[").append(tag.getColorCode()).append(subInfo).append("§7]§r");
                    }

                    String formattedStr = formatted.toString();
                    if (info.getDisplayName() == null || !formattedStr.equals(info.getDisplayName().getFormattedText())) {
                        info.setDisplayName(new ChatComponentText(formattedStr));
                        modifiedPlayerNames.add(lowerName);
                    }
                } else if (modifiedPlayerNames.contains(lowerName)) {
                    // Player was deleted or untagged -> restore original name instantly
                    IChatComponent original = originalDisplayNames.remove(lowerName);
                    info.setDisplayName(original);
                    modifiedPlayerNames.remove(lowerName);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void resetPlayer(String username) {
        if (username == null) return;
        try {
            String lower = username.toLowerCase();
            modifiedPlayerNames.remove(lower);
            IChatComponent original = originalDisplayNames.remove(lower);

            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getNetHandler() == null) return;

            Collection<NetworkPlayerInfo> playerInfoMap = mc.getNetHandler().getPlayerInfoMap();
            if (playerInfoMap != null) {
                for (NetworkPlayerInfo info : playerInfoMap) {
                    if (info != null && info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                        if (info.getGameProfile().getName().equalsIgnoreCase(username)) {
                            info.setDisplayName(original);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void resetAllModifiedNames() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getNetHandler() == null) return;

            Collection<NetworkPlayerInfo> playerInfoMap = mc.getNetHandler().getPlayerInfoMap();
            if (playerInfoMap != null) {
                for (NetworkPlayerInfo info : playerInfoMap) {
                    if (info != null && info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                        String lower = info.getGameProfile().getName().toLowerCase();
                        if (modifiedPlayerNames.contains(lower)) {
                            IChatComponent original = originalDisplayNames.remove(lower);
                            info.setDisplayName(original);
                        }
                    }
                }
            }
            modifiedPlayerNames.clear();
            originalDisplayNames.clear();
        } catch (Exception ignored) {}
    }
}
