package net.raynna.raynnarpg.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.raynna.raynnarpg.RaynnaRPG;
import net.raynna.raynnarpg.client.events.ClientBlockEvents;
import net.raynna.raynnarpg.client.player.ClientSkills;
import net.raynna.raynnarpg.server.player.skills.Skill;
import net.raynna.raynnarpg.server.player.skills.SkillType;
import net.raynna.raynnarpg.server.player.skills.Skills;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RaynnaRPG.MOD_ID, value = Dist.CLIENT)
public class SkillsHud {

    private static final int XP_BAR_WIDTH = 100;
    private static final int XP_BAR_HEIGHT = 5;
    private static final int LINE_SPACING = 26;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderHUD(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;


        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int xOffset = 10;
        int yOffset = screenHeight / 4;
        boolean shift = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        if (shift) {
            return;
        }
        for (SkillType type : SkillType.values()) {
            Skill skill = ClientSkills.getSkill(type);
            if (skill == null) continue;

            double totalXpInSkillNeededForNextLevel = ClientSkills.getXpForLevel(skill.getLevel() + 1);
            double xpForCurrentLevel = ClientSkills.getXpForLevel(skill.getLevel());
            double currentTotalXpInSkill = skill.getXp();
            drawSkillHUD(event.getGuiGraphics(), type.getName(), skill.getLevel(), currentTotalXpInSkill, xpForCurrentLevel, totalXpInSkillNeededForNextLevel, xOffset, yOffset);
            yOffset += LINE_SPACING;
        }
    }

    private static void drawSkillHUD(GuiGraphics guiGraphics, String skillName, int level, double currentTotalXpInSkill, double xpForCurrentLevel, double totalXpInSkillNeededForNextLevel, int xOffset, int yOffset) {
        Minecraft mc = Minecraft.getInstance();

        guiGraphics.drawString(mc.font, Component.literal(skillName + " Lv. " + level), xOffset, yOffset, 0xFFFFFF);

        double progress = Math.min((currentTotalXpInSkill - xpForCurrentLevel) / (totalXpInSkillNeededForNextLevel - xpForCurrentLevel), 1.0);

        int barWidth = (int) (XP_BAR_WIDTH * progress);

        guiGraphics.fill(xOffset, yOffset + 12, xOffset + XP_BAR_WIDTH, yOffset + 12 + XP_BAR_HEIGHT, 0xFF444444); // background
        guiGraphics.fill(xOffset, yOffset + 12, xOffset + barWidth, yOffset + 12 + XP_BAR_HEIGHT, 0xFF00AA00); // progress
        boolean isMaxLevel = level == 50;
        String xpText = "";
        if (isMaxLevel) {
            xpText = "Max Lvl";
        } else {
            xpText = String.format("%.0f / %.0f", currentTotalXpInSkill, totalXpInSkillNeededForNextLevel);
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.6F, 1, 0.6F);
        int textWidth = (int) ((double) mc.font.width(xpText) * 0.75);
        //int textXPosition = xOffset + 16 - (XP_BAR_WIDTH / 2);
        int textXPosition = xOffset + (XP_BAR_WIDTH - textWidth);
        guiGraphics.drawString(mc.font, Component.literal(xpText), textXPosition, yOffset + 11, 0xFFFFFF);
        guiGraphics.pose().popPose();
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(SkillsHud.class);
    }
}