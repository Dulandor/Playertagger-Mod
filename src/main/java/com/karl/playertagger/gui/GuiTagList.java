package com.karl.playertagger.gui;

import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.data.TaggedPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiTagList extends GuiScreen {

    private final GuiScreen parentScreen;
    private GuiTextField searchField;
    private int page = 0;
    private final int itemsPerPage = 6;
    private List<TaggedPlayer> filteredList = new ArrayList<TaggedPlayer>();

    public GuiTagList(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int centerX = this.width / 2;

        // Search field
        this.searchField = new GuiTextField(0, this.fontRendererObj, centerX - 180, 32, 160, 18);
        this.searchField.setMaxStringLength(20);

        // Top Action Buttons
        this.buttonList.add(new GuiButton(1, centerX - 10, 31, 110, 20, "§a§l+ Hinzufügen"));
        this.buttonList.add(new GuiButton(2, centerX + 105, 31, 80, 20, "🔥 Sync"));

        // Navigation & Close at bottom
        int bottomY = this.height - 28;
        this.buttonList.add(new GuiButton(3, centerX - 180, bottomY, 80, 20, "< Vorherige"));
        this.buttonList.add(new GuiButton(4, centerX - 90, bottomY, 80, 20, "Nächste >"));
        this.buttonList.add(new GuiButton(5, centerX + 10, bottomY, 90, 20, "⚙ Config"));
        this.buttonList.add(new GuiButton(6, centerX + 105, bottomY, 80, 20, "Schließen"));

        updateFilteredList();
        refreshItemButtons();
    }

    private void updateFilteredList() {
        String query = (searchField == null ? "" : searchField.getText()).trim().toLowerCase();
        List<TaggedPlayer> all = TagManager.getInstance().getAllPlayers();
        filteredList.clear();

        for (TaggedPlayer p : all) {
            if (query.isEmpty() || 
                p.getName().toLowerCase().contains(query) || 
                p.getCustomInfo().toLowerCase().contains(query) || 
                p.getGameMode().toLowerCase().contains(query) || 
                p.getCategory().toLowerCase().contains(query)) {
                filteredList.add(p);
            }
        }
    }

    private void refreshItemButtons() {
        // Remove existing delete buttons (ids >= 100)
        Iterator<GuiButton> iter = this.buttonList.iterator();
        while (iter.hasNext()) {
            if (iter.next().id >= 100) {
                iter.remove();
            }
        }

        int centerX = this.width / 2;
        int startY = 56;
        int startIndex = page * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int index = startIndex + i;
            if (index >= filteredList.size()) break;

            int rowY = startY + i * 26;
            // Delete button on every row
            this.buttonList.add(new GuiButton(100 + index, centerX + 140, rowY + 2, 46, 18, "§c✕ Löschen"));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(parentScreen);
            return;
        }

        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            page = 0;
            updateFilteredList();
            refreshItemButtons();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { // Add Player Wizard (Step 1)
            this.mc.displayGuiScreen(new GuiAddPlayerStep1(this));
        } else if (button.id == 2) { // Firebase Cloud Sync
            TagManager.getInstance().syncFromFirebase(true);
            updateFilteredList();
            refreshItemButtons();
        } else if (button.id == 3) { // Previous Page
            if (page > 0) {
                page--;
                refreshItemButtons();
            }
        } else if (button.id == 4) { // Next Page
            if ((page + 1) * itemsPerPage < filteredList.size()) {
                page++;
                refreshItemButtons();
            }
        } else if (button.id == 5) { // OneConfig
            org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt.openUI(com.karl.playertagger.PlayerTaggerMod.config);
        } else if (button.id == 6) { // Close
            this.mc.displayGuiScreen(parentScreen);
        } else if (button.id >= 100) { // Delete
            int index = button.id - 100;
            if (index < filteredList.size()) {
                TaggedPlayer tp = filteredList.get(index);
                TagManager.getInstance().removePlayer(tp.getName());
                updateFilteredList();
                refreshItemButtons();
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.searchField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;

        // Title
        this.drawCenteredString(this.fontRendererObj, "§6§l[PlayerTagger] Markierte Spieler (" + filteredList.size() + ")", centerX, 12, 0xFFFFFF);

        this.searchField.drawTextBox();
        if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
            this.drawString(this.fontRendererObj, "§8Suchen...", centerX - 176, 37, 0x777777);
        }

        // Draw Player Rows
        int startY = 56;
        int startIndex = page * itemsPerPage;

        if (filteredList.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "§7Keine markierten Spieler gefunden. Klicke auf '§a+ Hinzufügen§7'!", centerX, startY + 40, 0x888888);
        }

        for (int i = 0; i < itemsPerPage; i++) {
            int index = startIndex + i;
            if (index >= filteredList.size()) break;

            TaggedPlayer tp = filteredList.get(index);
            int rowY = startY + i * 26;

            // Row background
            int bgColor = (i % 2 == 0) ? 0x661a1a1a : 0x662a2a2a;
            Gui.drawRect(centerX - 185, rowY, centerX + 190, rowY + 23, bgColor);

            // Icon & Name
            String nameText = tp.getColorCode() + (tp.getIcon().isEmpty() ? "" : tp.getIcon() + " ") + tp.getName() + "§r";
            this.drawString(this.fontRendererObj, nameText, centerX - 180, rowY + 4, 0xFFFFFF);

            // Badge & Info
            String badgeText = tp.getFormattedBadge();
            if (badgeText.isEmpty()) {
                badgeText = "§7(" + tp.getCategory() + ")";
            }
            this.drawString(this.fontRendererObj, badgeText, centerX - 65, rowY + 4, 0xCCCCCC);

            // Cloud vs Local label
            String sourceLabel = tp.isShared() ? "§6🔥 Firebase" : "§a★ Lokal";
            this.drawString(this.fontRendererObj, sourceLabel, centerX + 90, rowY + 7, 0xAAAAAA);
        }

        // Page info
        int maxPages = Math.max(1, (int) Math.ceil((double) filteredList.size() / itemsPerPage));
        this.drawCenteredString(this.fontRendererObj, "§7Seite " + (page + 1) + " / " + maxPages, centerX - 135, this.height - 40, 0xAAAAAA);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
