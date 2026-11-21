package com.karl.playertagger.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAddPlayerStep2 extends GuiScreen {

    private final GuiScreen parentScreen;
    private final String playerName;
    private GuiTextField modeField;
    private GuiButton btnBack;
    private GuiButton btnSkip;
    private GuiButton btnNext;

    private static final String[] MODES = {
        "BedWars", "SkyWars", "Duels",
        "UHC", "The Bridge", "BuildUHC",
        "Survival Games", "All-Rounder", "Ranked"
    };

    public GuiAddPlayerStep2(GuiScreen parentScreen, String playerName) {
        this.parentScreen = parentScreen;
        this.playerName = playerName;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Custom Mode text field
        this.modeField = new GuiTextField(0, this.fontRendererObj, centerX - 130, centerY - 35, 260, 20);
        this.modeField.setMaxStringLength(30);
        this.modeField.setFocused(true);

        // Mode preset chip buttons (3x3 grid)
        int presetStartY = centerY - 5;
        for (int i = 0; i < MODES.length; i++) {
            int row = i / 3;
            int col = i % 3;
            int bx = centerX - 130 + col * 88;
            int by = presetStartY + row * 22;
            this.buttonList.add(new GuiButton(100 + i, bx, by, 84, 18, "§b" + MODES[i]));
        }

        // Bottom Navigation
        btnBack = new GuiButton(1, centerX - 130, centerY + 70, 80, 20, "< Zurück");
        btnSkip = new GuiButton(2, centerX - 45, centerY + 70, 85, 20, "§7Überspringen »");
        btnNext = new GuiButton(3, centerX + 45, centerY + 70, 85, 20, "Weiter >");

        this.buttonList.add(btnBack);
        this.buttonList.add(btnSkip);
        this.buttonList.add(btnNext);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(new GuiAddPlayerStep1(parentScreen, playerName));
            return;
        }

        if (this.modeField.textboxKeyTyped(typedChar, keyCode)) {
            if (keyCode == Keyboard.KEY_RETURN) {
                proceedNext();
            }
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.modeField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { // Back
            this.mc.displayGuiScreen(new GuiAddPlayerStep1(parentScreen, playerName));
        } else if (button.id == 2) { // Skip
            this.mc.displayGuiScreen(new GuiAddPlayerStep3(parentScreen, playerName, ""));
        } else if (button.id == 3) { // Next
            proceedNext();
        } else if (button.id >= 100 && button.id < 100 + MODES.length) {
            int index = button.id - 100;
            if (index < MODES.length) {
                modeField.setText(MODES[index]);
            }
        }
    }

    private void proceedNext() {
        String mode = modeField.getText().trim();
        this.mc.displayGuiScreen(new GuiAddPlayerStep3(parentScreen, playerName, mode));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.modeField.updateCursorCounter();
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
        this.drawCenteredString(this.fontRendererObj, "§6§l[PlayerTagger] Spieler: §f" + playerName, centerX, centerY - 82, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "§e§nSchritt 2 von 4§r§7: In welchem Spielmodus ist er aktiv/gut?", centerX, centerY - 65, 0xAAAAAA);

        // Input label
        this.drawString(this.fontRendererObj, "§fSpielmodus (kann übersprungen werden):", centerX - 130, centerY - 48, 0xFFFFFF);

        this.modeField.drawTextBox();

        this.drawString(this.fontRendererObj, "§8Beliebte Modi zum Anklicken:", centerX - 130, centerY - 14, 0x888888);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
