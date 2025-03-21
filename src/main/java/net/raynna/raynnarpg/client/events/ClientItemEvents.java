package net.raynna.raynnarpg.client.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.raynna.raynnarpg.RaynnaRPG;
import net.raynna.raynnarpg.client.player.ClientSkills;
import net.raynna.raynnarpg.data.*;
import net.raynna.raynnarpg.network.packets.message.MessagePacketSender;
import net.raynna.raynnarpg.server.player.skills.SkillType;
import net.silentchaos512.gear.api.item.GearItem;
import net.silentchaos512.gear.core.component.GearPropertiesData;
import net.silentchaos512.gear.util.GearData;
import org.apache.logging.log4j.core.jmx.Server;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = RaynnaRPG.MOD_ID, value = Dist.CLIENT)
public class ClientItemEvents {

    @SubscribeEvent
    public static void onItemHover(ItemTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        boolean isPlayerCreative = mc.player.isCreative();
        boolean isSinglePlayer = mc.isSingleplayer();
        ItemStack stack = event.getItemStack();
        String itemId = stack.getItem().getDescriptionId();
        Map<String, String> data = new HashMap<>();
        List<Component> toolTips = event.getToolTip();
        AtomicInteger insertIndex = new AtomicInteger(1);
        ClientSkills skills = new ClientSkills(mc.player);
        BlockData blockData = DataRegistry.getDataFromItem(stack, BlockData.class);
        if (blockData != null) {
            int miningLevel = skills.getSkillLevel(SkillType.MINING);
            int levelReq = blockData.getLevelRequirement();
            double xp = blockData.getExperience();
            StringBuilder miningText = new StringBuilder();

            if (miningLevel < levelReq) {
                miningText.append("§cLevel: ")
                        .append(levelReq)
                        .append(" (Current level: ")
                        .append(miningLevel).append("), Xp: ").append(xp);
            } else {
                miningText.append("§aLevel: ")
                        .append(levelReq)
                        .append(", Xp: ")
                        .append(xp);
            }
            data.put("Mining", miningText.toString());
        }

        CraftingData craftingData = DataRegistry.getDataFromItem(stack, CraftingData.class);
        if (craftingData != null) {
            int craftingLevel = skills.getSkillLevel(SkillType.CRAFTING);
            StringBuilder craftingText = new StringBuilder();
            if (craftingLevel < craftingData.getLevelRequirement()) {
                craftingText.append("§cLevel: ")
                        .append(craftingData.getLevelRequirement())
                        .append(" (Current level: ")
                        .append(craftingLevel).append("), Xp: ").append(craftingData.getExperience());
            } else {
                craftingText.append("§aLevel: ")
                        .append(craftingData.getLevelRequirement()).append(", Xp: ").append(craftingData.getExperience());
            }
            data.put("Crafting", craftingText.toString());
        }

        SmeltingData smeltingData = DataRegistry.getDataFromItem(stack, SmeltingData.class);
        if (smeltingData != null) {
            int smeltingLevel = skills.getSkillLevel(SkillType.SMELTING);
            StringBuilder smeltingText = new StringBuilder();
            if (smeltingLevel < smeltingData.getLevelRequirement()) {
                smeltingText.append("§cLevel: ")
                        .append(smeltingData.getLevelRequirement())
                        .append(" (Current level: ")
                        .append(smeltingLevel).append("), Xp: ").append(smeltingData.getExperience());
            } else {
                smeltingText.append("§aLevel: ")
                        .append(smeltingData.getLevelRequirement()).append(", Xp: ").append(smeltingData.getExperience());
            }
            data.put("Smelting", smeltingText.toString());
        }
        if (ModList.get().isLoaded("silentgear")) {
            if (stack.getItem() instanceof GearItem silent) {
                String toolName = silent.asItem().getName(stack).getString();

                Map<String, String> properties = new HashMap<>();
                GearPropertiesData propertiesData = GearData.getProperties(stack);
                propertiesData.properties().forEach((key, value) -> {
                    properties.put(key.getDisplayName().getString(), value.toString());
                });
                String harvestTierByName = properties.get("Harvest Tier");
                ToolData toolData = DataRegistry.getTool(harvestTierByName);

                if (toolData != null) {
                    StringBuilder toolText = new StringBuilder();

                    int playerLevel = skills.getSkillLevel(SkillType.MINING);
                    if (playerLevel < toolData.getLevelRequirement()) {
                        toolText.append("§cLevel: ")
                                .append(toolData.getLevelRequirement())
                                .append(" (Current level: ")
                                .append(playerLevel).append(")");
                    } else {
                        toolText.append("§aLevel: ")
                                .append(toolData.getLevelRequirement());
                    }

                    data.put("Mining", toolText.toString());
                }
            }
        }
        ToolData toolData = DataRegistry.getTool(stack.getDescriptionId());
        if (toolData != null) {
            StringBuilder toolText = new StringBuilder();
            int playerLevel = skills.getSkillLevel(SkillType.MINING);
            if (playerLevel < toolData.getLevelRequirement()) {
                toolText.append("§cLevel: ")
                        .append(toolData.getLevelRequirement())
                        .append(" (Current level: ")
                        .append(playerLevel).append(")");
            } else {
                toolText.append("§aLevel: ")
                        .append(toolData.getLevelRequirement());
            }

            data.put("Mining", toolText.toString());
        }

        if (!data.isEmpty()) {
            toolTips.add(insertIndex.getAndIncrement(), Component.literal("§6RaynnaRPG: "));
            data.forEach((key, value) ->
                    toolTips.add(insertIndex.getAndIncrement(), Component.literal("§7" + key + " " + value))
            );
        }

        if (isPlayerCreative) {
            event.getToolTip().add(Component.literal("§6Description: §7" + itemId));
            event.getToolTip().add(Component.literal("§6Tags: "));
            for (TagKey<Item> tag : stack.getTags().toList()) {
                event.getToolTip().add(Component.literal("§7- " + tag.location().toString()));
            }
            return;
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.hasPermissions(2)) {
                boolean shift = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                if (!shift) {
                    event.getToolTip().add(Component.literal("Hold SHIFT to reveal item tags."));
                    return;
                }
                event.getToolTip().add(Component.literal("§6Description: §7" + itemId));
                event.getToolTip().add(Component.literal("§6Tags: "));
                for (TagKey<Item> tag : stack.getTags().toList()) {
                    event.getToolTip().add(Component.literal("§7- " + tag.location().toString()));
                }
            }
        } else {
            boolean shift = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
            if (!shift) {
                event.getToolTip().add(Component.literal("Hold SHIFT to reveal item tags."));
                return;
            }
            event.getToolTip().add(Component.literal("§6Description: §7" + itemId));
            event.getToolTip().add(Component.literal("§6Tags: "));
            for (TagKey<Item> tag : stack.getTags().toList()) {
                event.getToolTip().add(Component.literal("§7- " + tag.location().toString()));
            }
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(ClientItemEvents.class);
    }
}
