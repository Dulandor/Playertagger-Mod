package com.karl.playertagger.render;

import com.karl.playertagger.config.PlayerTaggerConfig;
import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.data.TaggedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class NametagRenderer {

    public static final NametagRenderer INSTANCE = new NametagRenderer();

    public boolean onRenderLivingName(EntityLivingBase entity, double x, double y, double z) {
        if (!PlayerTaggerConfig.enabled || !PlayerTaggerConfig.nametagTagging) return false;
        if (!(entity instanceof EntityPlayer)) return false;

        TagManager tagManager = TagManager.getInstance();
        if (!tagManager.hasAnyTags()) return false;

        EntityPlayer player = (EntityPlayer) entity;
        Minecraft mc = Minecraft.getMinecraft();

        TaggedPlayer tag = tagManager.getTaggedPlayer(player.getName());
        if (tag == null) return false;

        if (player == mc.thePlayer) {
            if (mc.gameSettings != null && mc.gameSettings.thirdPersonView > 0) {
                renderVanillaStyle2PassNametag(player, tag, x, y, z);
            }
        } else {
            renderVanillaStyle2PassNametag(player, tag, x, y, z);
        }

        // Return true to cancel vanilla nametag rendering
        return true;
    }

    private void renderVanillaStyle2PassNametag(EntityPlayer player, TaggedPlayer tag, double x, double y, double z) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            RenderManager rm = mc.getRenderManager();
            if (rm == null) return;

            FontRenderer font = mc.fontRendererObj;
            if (font == null) return;

            float distance = player.getDistanceToEntity(mc.thePlayer);
            // Sneaking players have a shorter visibility range in vanilla (32 blocks)
            float maxDist = player.isSneaking() ? 32.0F : 64.0F;
            if (distance > maxDist) return;

            // Vanilla 1.8.9 exact scale & distance positioning
            float baseScale = 0.016666668F * 1.6F;
            float yOffset = player.height + 0.55F - (player.isSneaking() ? 0.25F : 0.0F);

            // Correct pitch angle for front-view F5 mode (thirdPersonView == 2) on all clients
            float viewX = rm.playerViewX;
            if (mc.gameSettings != null && mc.gameSettings.thirdPersonView == 2) {
                viewX = -viewX;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + yOffset, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(viewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-baseScale, -baseScale, baseScale);

            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            String badge = (PlayerTaggerConfig.showSubBadge) ? tag.getFormattedBadge() : "";
            String name = (PlayerTaggerConfig.colorNametag) ? tag.getFormattedName() : player.getDisplayName().getFormattedText();

            boolean hasBadge = !badge.isEmpty();

            if (hasBadge) {
                int badgeWidth = font.getStringWidth(badge) / 2;
                int nameWidth = font.getStringWidth(name) / 2;

                // --- PASS 1: Render behind walls (translucent name, like vanilla) ---
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();

                Gui.drawRect(-badgeWidth - 2, -12, badgeWidth + 2, -2, 0x40000000);
                font.drawString(badge, -badgeWidth, -11, 0x20FFFFFF, false);

                Gui.drawRect(-nameWidth - 2, 0, nameWidth + 2, 10, 0x40000000);
                font.drawString(name, -nameWidth, 1, 0x20FFFFFF, false);

                // --- PASS 2: Render in front of walls (bright color) ---
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);

                font.drawString(badge, -badgeWidth, -11, 0xFFFFFFFF, false);
                font.drawString(name, -nameWidth, 1, 0xFFFFFFFF, false);
            } else {
                // Single line
                String singleLine = (tag.getIcon().isEmpty() ? "" : tag.getIcon() + " ") + name;
                int nameWidth = font.getStringWidth(singleLine) / 2;

                // --- PASS 1: Render behind walls (translucent) ---
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();

                Gui.drawRect(-nameWidth - 2, -2, nameWidth + 2, 8, 0x40000000);
                font.drawString(singleLine, -nameWidth, -1, 0x20FFFFFF, false);

                // --- PASS 2: Render in front of walls (bright) ---
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);

                font.drawString(singleLine, -nameWidth, -1, 0xFFFFFFFF, false);
            }

            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        } catch (Exception ignored) {}
    }
}
