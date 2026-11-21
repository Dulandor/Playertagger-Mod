package com.karl.playertagger.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAddPlayerStep3 extends GuiScreen {

    private final GuiScreen parentScreen;
    private final String playerName;
    private final String gameMode;
    private GuiTextField infoField;
    private GuiButton btnBack;
    private GuiButton btnSkip;
    private GuiButton btnNext;

    private static final String[] PRESETS = {
        "Cheater", "B-Hop / Killaura", "3.4 Reach",
        "Sweat", "Fireball Jumper", "Bow Spammer",
        "Rod God", "Block Clutch", "Fast Bridge"
    };

    public GuiAddPlayerStep3(GuiScreen parentScreen, String playerName, String gameMode) {
        this.parentScreen = parentScreen;
        this.playerName = playerName;
        this.gameMode = gameMode == null ? "" : gameMode.trim();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Custom Info text field
        this.infoField = new GuiTextField(0, this.fontRendererObj, centerX - 130, centerY - 35, 260, 20);
        this.infoField.setMaxStringLength(40);
        this.infoField.setFocused(true);

        // Preset chip buttons (3x3 grid)
        int presetStartY = centerY - 5;
        for (int i = 0; i < PRESETS.length; i++) {
            int row = i / 3;
            int col = i % 3;
            int bx = centerX - 130 + col * 88;
            int by = presetStartY + row * 22;
            this.buttonList.add(new GuiButton(100 + i, bx, by, 84, 18, "§e" + PRESETS[i]));
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
            this.mc.displayGuiScreen(new GuiAddPlayerStep2(parentScreen, playerName));
            return;
        }

        if (this.infoField.textboxKeyTyped(typedChar, keyCode)) {
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
        this.infoField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { // Back
            this.mc.displayGuiScreen(new GuiAddPlayerStep2(parentScreen, playerName));
        } else if (button.id == 2) { // Skip
            this.mc.displayGuiScreen(new GuiAddPlayerStep4(parentScreen, playerName, gameMode, ""));
        } else if (button.id == 3) { // Next
            proceedNext();
        } else if (button.id >= 100 && button.id < 100 + PRESETS.length) {
            int index = button.id - 100;
            if (index < PRESETS.length) {
                String current = infoField.getText().trim();
                String preset = PRESETS[index];
                if (current.isEmpty()) {
                    infoField.setText(preset);
                } else if (!current.contains(preset)) {
                    infoField.setText(current + " / " + preset);
                }
            }
        }
    }

    private void proceedNext() {
        String info = infoField.getText().trim();
        this.mc.displayGuiScreen(new GuiAddPlayerStep4(parentScreen, playerName, gameMode, info));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.infoField.updateCursorCounter();
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
        this.drawCenteredString(this.fontRendererObj, "§6§l[PlayerTagger] Spieler: §f" + playerName + (gameMode.isEmpty() ? "" : " §7(" + gameMode + ")"), centerX, centerY - 82, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "§e§nSchritt 3 von 4§r§7: Zusatz-Information / Cheats / Notiz (Optional)", centerX, centerY - 65, 0xAAAAAA);

        // Input label
        this.drawString(this.fontRendererObj, "§fBeschreibung (z.B. Cheater, Sweat, Fireball God - kann übersprungen werden):", centerX - 130, centerY - 48, 0xFFFFFF);

        this.infoField.drawTextBox();

        this.drawString(this.fontRendererObj, "§8Schnell-Vorlagen zum Anklicken:", centerX - 130, centerY - 14, 0x888888);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
