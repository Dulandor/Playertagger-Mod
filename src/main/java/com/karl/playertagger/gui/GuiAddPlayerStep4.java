package com.karl.playertagger.gui;

import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.data.TaggedPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAddPlayerStep4 extends GuiScreen {

    private final GuiScreen parentScreen;
    private final String playerName;
    private final String gameMode;
    private final String customInfo;

    private String selectedColor = "§c";
    private String selectedIcon = "☠";
    private String selectedCategory = "Cheater";
    private int threatLevel = 5;

    // Color options
    private static final String[][] COLORS = {
        {"§c", "Rot", "Cheater / Blatant Hacker", "☠"},
        {"§6", "Gold", "Sweat / High-Star Tryhard", "⚡"},
        {"§d", "Lila", "BedWars God / Ranked Pro", "⚔"},
        {"§b", "Cyan", "Fireball Jumper / Rusher", "☄"},
        {"§e", "Gelb", "Bow Spammer / Tactical", "🎯"},
        {"§a", "Grün", "Friend / Trusted Player", "🛡"},
        {"§3", "Dunkeltürkis", "Sniper / Targeter", "👁"},
        {"§7", "Grau", "Neutral / Notiz", ""}
    };

    private static final String[] ICONS = {"☠", "⚡", "☄", "⚔", "🛡", "🎯", "👁", "None"};

    public GuiAddPlayerStep4(GuiScreen parentScreen, String playerName, String gameMode, String customInfo) {
        this.parentScreen = parentScreen;
        this.playerName = playerName;
        this.gameMode = gameMode == null ? "" : gameMode.trim();
        this.customInfo = customInfo == null ? "" : customInfo.trim();

        // Auto-match initial color & category based on mode/info
        String combined = (gameMode + " " + customInfo).toLowerCase();
        if (combined.contains("sweat") || combined.contains("tryhard")) {
            selectedColor = "§6";
            selectedIcon = "⚡";
            selectedCategory = "Sweat";
            threatLevel = 4;
        } else if (combined.contains("bedwar") || combined.contains("god")) {
            selectedColor = "§d";
            selectedIcon = "⚔";
            selectedCategory = "BedWars";
            threatLevel = 4;
        } else if (combined.contains("fireball") || combined.contains("skywar")) {
            selectedColor = "§b";
            selectedIcon = "☄";
            selectedCategory = "Fireball";
            threatLevel = 3;
        } else if (combined.contains("friend") || combined.contains("team")) {
            selectedColor = "§a";
            selectedIcon = "🛡";
            selectedCategory = "Friend";
            threatLevel = 1;
        }
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = this.width / 2;
        int startY = 38;

        // Color selection buttons (2 columns)
        for (int i = 0; i < COLORS.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int btnX = centerX - 180 + col * 185;
            int btnY = startY + row * 22;
            String colorCode = COLORS[i][0];
            String name = COLORS[i][1];
            String desc = COLORS[i][2];

            String label = (selectedColor.equals(colorCode) ? "§l> " : "") + colorCode + name + " §8(" + desc + ")";
            this.buttonList.add(new GuiButton(100 + i, btnX, btnY, 180, 20, label));
        }

        // Icon selector buttons
        int iconStartY = startY + (COLORS.length / 2) * 22 + 8;
        for (int i = 0; i < ICONS.length; i++) {
            int bx = centerX - 180 + i * 46;
            String ic = ICONS[i].equals("None") ? "" : ICONS[i];
            String btnText = (selectedIcon.equals(ic) ? "§a§l" : "§f") + ICONS[i];
            this.buttonList.add(new GuiButton(200 + i, bx, iconStartY, 42, 18, btnText));
        }

        // Bottom Navigation Buttons
        int bottomY = this.height - 30;
        this.buttonList.add(new GuiButton(1, centerX - 180, bottomY, 110, 20, "< Zurück"));
        this.buttonList.add(new GuiButton(2, centerX - 60, bottomY, 240, 20, "§a§l✓ Fertigstellen & Speichern"));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(new GuiAddPlayerStep3(parentScreen, playerName, gameMode));
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { // Back
            this.mc.displayGuiScreen(new GuiAddPlayerStep3(parentScreen, playerName, gameMode));
        } else if (button.id == 2) { // Save & Finish
            TaggedPlayer tp = new TaggedPlayer(
                playerName,
                gameMode,
                customInfo,
                selectedColor,
                selectedIcon,
                selectedCategory,
                threatLevel,
                false
            );
            TagManager.getInstance().addPlayer(tp);

            // Return to main list
            this.mc.displayGuiScreen(new GuiTagList(null));
        } else if (button.id >= 100 && button.id < 100 + COLORS.length) {
            int index = button.id - 100;
            selectedColor = COLORS[index][0];
            selectedCategory = COLORS[index][1];
            if (!COLORS[index][3].isEmpty() && selectedIcon.isEmpty()) {
                selectedIcon = COLORS[index][3];
            }
            initGui(); // Refresh button labels
        } else if (button.id >= 200 && button.id < 200 + ICONS.length) {
            int index = button.id - 200;
            selectedIcon = ICONS[index].equals("None") ? "" : ICONS[index];
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;

        // Title
        this.drawCenteredString(this.fontRendererObj, "§6§l[PlayerTagger] Farbauswahl & Legende", centerX, 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "§e§nSchritt 4 von 4§r§7: Wähle die Farbe & das Icon für den Nametag", centerX, 24, 0xAAAAAA);

        // Icon Label
        int iconStartY = 38 + (COLORS.length / 2) * 22 + 8;
        this.drawString(this.fontRendererObj, "§fIcon auswählen:", centerX - 180, iconStartY - 10, 0xFFFFFF);

        // LIVE PREVIEW BOX
        int previewBoxY = iconStartY + 26;
        int boxWidth = 365;
        int boxHeight = 52;
        int boxX = centerX - boxWidth / 2;

        // Box border & dark background
        Gui.drawRect(boxX, previewBoxY, boxX + boxWidth, previewBoxY + boxHeight, 0xDD111111);
        Gui.drawRect(boxX, previewBoxY, boxX + boxWidth, previewBoxY + 1, 0xFF444444);
        Gui.drawRect(boxX, previewBoxY + boxHeight - 1, boxX + boxWidth, previewBoxY + boxHeight, 0xFF444444);
        Gui.drawRect(boxX, previewBoxY, boxX + 1, previewBoxY + boxHeight, 0xFF444444);
        Gui.drawRect(boxX + boxWidth - 1, previewBoxY, boxX + boxWidth, previewBoxY + boxHeight, 0xFF444444);

        this.drawCenteredString(this.fontRendererObj, "§e✦ Live-Vorschau (Overhead Nametag) ✦", centerX, previewBoxY + 4, 0xFFFFAA);

        // Sub-badge preview (Line 1)
        StringBuilder badgeContent = new StringBuilder();
        if (!gameMode.isEmpty()) {
            badgeContent.append(gameMode);
        }
        if (!customInfo.isEmpty()) {
            if (badgeContent.length() > 0) badgeContent.append(" • ");
            badgeContent.append(customInfo);
        }

        String badgeLine = "";
        if (badgeContent.length() > 0 || !selectedIcon.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(selectedColor).append("[");
            if (!selectedIcon.isEmpty()) {
                sb.append(selectedIcon).append(" ");
            }
            if (badgeContent.length() > 0) {
                sb.append(badgeContent.toString());
            } else {
                sb.append(selectedCategory);
            }
            sb.append("]§r");
            badgeLine = sb.toString();
        } else {
            badgeLine = "§8(Kein Sub-Badge)";
        }

        this.drawCenteredString(this.fontRendererObj, badgeLine, centerX, previewBoxY + 19, 0xFFFFFF);

        // Name preview (Line 2)
        String nameLine = selectedColor + playerName + "§r";
        this.drawCenteredString(this.fontRendererObj, nameLine, centerX, previewBoxY + 33, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
