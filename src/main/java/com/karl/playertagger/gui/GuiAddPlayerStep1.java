package com.karl.playertagger.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiAddPlayerStep1 extends GuiScreen {

    private final GuiScreen parentScreen;
    private GuiTextField nameField;
    private GuiButton btnNext;
    private GuiButton btnCancel;
    private String prefilledName = "";
    private final List<String> onlinePlayers = new ArrayList<String>();

    public GuiAddPlayerStep1(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public GuiAddPlayerStep1(GuiScreen parentScreen, String prefill) {
        this.parentScreen = parentScreen;
        this.prefilledName = prefill == null ? "" : prefill;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.onlinePlayers.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Name input field
        this.nameField = new GuiTextField(0, this.fontRendererObj, centerX - 120, centerY - 25, 240, 20);
        this.nameField.setMaxStringLength(24);
        this.nameField.setFocused(true);
        if (!prefilledName.isEmpty()) {
            this.nameField.setText(prefilledName);
        }

        // Navigation Buttons
        btnCancel = new GuiButton(1, centerX - 120, centerY + 65, 115, 20, "§cAbbrechen");
        btnNext = new GuiButton(2, centerX + 5, centerY + 65, 115, 20, "Weiter zu Schritt 2 >");
        this.buttonList.add(btnCancel);
        this.buttonList.add(btnNext);

        // Fetch online tab players for quick suggestion
        try {
            if (mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfoMap() != null) {
                for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                    if (info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                        String pName = info.getGameProfile().getName();
                        if (!pName.equalsIgnoreCase(mc.thePlayer.getName()) && !onlinePlayers.contains(pName)) {
                            onlinePlayers.add(pName);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Add suggestion buttons if online players found
        int suggestionY = centerY + 5;
        int count = 0;
        for (final String onlineName : onlinePlayers) {
            if (count >= 4) break;
            int btnX = centerX - 120 + (count % 2) * 125;
            int btnY = suggestionY + (count / 2) * 22;
            this.buttonList.add(new GuiButton(100 + count, btnX, btnY, 115, 18, "§7" + onlineName));
            count++;
        }

        updateNextButton();
    }

    private void updateNextButton() {
        String text = nameField.getText().trim();
        btnNext.enabled = !text.isEmpty();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(parentScreen);
            return;
        }

        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            updateNextButton();
            if (keyCode == Keyboard.KEY_RETURN && btnNext.enabled) {
                actionPerformed(btnNext);
            }
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { // Cancel
            this.mc.displayGuiScreen(parentScreen);
        } else if (button.id == 2) { // Next -> Step 2 (Game Mode)
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                this.mc.displayGuiScreen(new GuiAddPlayerStep2(parentScreen, name));
            }
        } else if (button.id >= 100 && button.id < 100 + onlinePlayers.size()) {
            int index = button.id - 100;
            if (index < onlinePlayers.size()) {
                nameField.setText(onlinePlayers.get(index));
                updateNextButton();
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.nameField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Header & Step indicator
        this.drawCenteredString(this.fontRendererObj, "§6§l[PlayerTagger] Spieler hinzufügen", centerX, centerY - 80, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "§e§nSchritt 1 von 4§r§7: Wie heißt der Spieler?", centerX, centerY - 62, 0xAAAAAA);

        // Input label
        this.drawString(this.fontRendererObj, "§fSpielername eingeben (Pflichtfeld):", centerX - 120, centerY - 38, 0xFFFFFF);

        this.nameField.drawTextBox();

        if (!onlinePlayers.isEmpty()) {
            this.drawString(this.fontRendererObj, "§8Schnell-Auswahl (Spieler in deiner Runde):", centerX - 120, centerY - 2, 0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
