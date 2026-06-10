package com.duox.easysunlitvalley.client;

import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.fishing.EasyFishing;
import com.duox.easysunlitvalley.harvest.EasyHarvester;
import com.duox.easysunlitvalley.husbandry.EasyHusbandry;
import com.duox.easysunlitvalley.tapper.EasyTapper;
import com.duox.easysunlitvalley.preserve.EasyPreserves;
import com.duox.easysunlitvalley.wine.EasyWine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Unified HUD overlay showing status for all active modules.
 * Compact card with sections that appear/disappear based on module state.
 */
@OnlyIn(Dist.CLIENT)
public class ESVHUD {

    private static boolean statsRequested = false;
    private static int initialTotalFish = -1;

    private final EasyFishing fishHack;
    private final EasyHarvester harvester;
    private final EasyTapper tapper;
    private final EasyPreserves preserves;
    private final EasyWine wine;
    private final EasyHusbandry husbandry;

    public ESVHUD(EasyFishing fishHack, EasyHarvester harvester, EasyTapper tapper,
                  EasyPreserves preserves, EasyWine wine, EasyHusbandry husbandry) {
        this.fishHack = fishHack;
        this.harvester = harvester;
        this.tapper = tapper;
        this.preserves = preserves;
        this.wine = wine;
        this.husbandry = husbandry;
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        statsRequested = false;
        initialTotalFish = -1;
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (!ESVConfig.INSTANCE.hudEnabled.get()) return;

        boolean fishActive = ModuleManager.fishingEnabled || ESVConfig.INSTANCE.easyMinigame.get();
        boolean harvestActive = ModuleManager.harvestEnabled;
        boolean tapperActive = ModuleManager.tapperEnabled;
        boolean preservesActive = ModuleManager.preservesEnabled;
        boolean wineActive = ModuleManager.wineEnabled;
        boolean husbandryActive = ModuleManager.husbandryEnabled;
        boolean forceGrow = ModuleManager.forceGrowEnabled;

        if (!fishActive && !harvestActive && !tapperActive && !forceGrow && !preservesActive && !wineActive && !husbandryActive) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        // Request stats sync
        if (mc.getConnection() != null && !statsRequested) {
            mc.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            statsRequested = true;
        }

        // Calculate HUD height dynamically
        int lineH = 12;
        int lines = 1; // Title
        if (fishActive) lines += 4; // Status, Rod, Session, Total
        if (harvestActive || forceGrow) lines += 2; // Harvest status + mature count
        if (husbandryActive) lines += 2; // Husbandry status + nearby count
        if (tapperActive || preservesActive || wineActive) {
            lines += 1; // Section header (Artisan)
            if (tapperActive) lines += 1;
            if (preservesActive) lines += 1;
            if (wineActive) lines += 1;
        }

        int width = 175;
        int height = 10 + lines * lineH;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        ESVConfig.HudPosition pos = ESVConfig.INSTANCE.hudPosition.get();
        int x = 6, y = 6;
        if (pos == ESVConfig.HudPosition.TOP_RIGHT) x = screenW - width - 6;
        else if (pos == ESVConfig.HudPosition.BOTTOM_LEFT) y = screenH - height - 6;
        else if (pos == ESVConfig.HudPosition.BOTTOM_RIGHT) { x = screenW - width - 6; y = screenH - height - 6; }

        GuiGraphics g = event.getGuiGraphics();

        // Card background
        g.fill(x, y, x + width, y + height, 0x90101015);
        // Green top border
        g.fill(x, y, x + width, y + 1, 0xFF5CA848);
        // Side + bottom borders
        g.fill(x, y + height - 1, x + width, y + height, 0xFF5CA848);
        g.fill(x, y, x + 1, y + height, 0xFF5CA848);
        g.fill(x + width - 1, y, x + width, y + height, 0xFF5CA848);

        int tx = x + 6;
        int ty = y + 5;

        // Title
        g.drawString(mc.font, "§a§lEasy Sunlit Valley", tx, ty, 0xFFFFFFFF, true);
        ty += lineH + 2;

        // ── FISHING SECTION ────────────────────────────────────────────
        if (fishActive) {
            String fishStatus = getFishingStatus(mc);
            g.drawString(mc.font, "§6Fishing: " + fishStatus, tx, ty, 0xFFCCCCCC, true);
            ty += lineH;

            String dur = getDurabilityString(mc);
            g.drawString(mc.font, "Rod: " + dur, tx, ty, 0xFFCCCCCC, true);
            ty += lineH;

            g.drawString(mc.font, "Session: §a" + EasyFishing.caughtCount, tx, ty, 0xFFCCCCCC, true);
            ty += lineH;

            int totalFish = getTotalFish(mc);
            g.drawString(mc.font, "Total Caught: §a" + totalFish, tx, ty, 0xFFCCCCCC, true);
            ty += lineH;
        }

        // ── HARVEST SECTION ────────────────────────────────────────────
        if (harvestActive || forceGrow) {
            String hStatus = harvestActive ? "§aActive" : "§7Off";
            if (forceGrow) hStatus += " §e+ForceGrow";
            g.drawString(mc.font, "§2Harvest: " + hStatus, tx, ty, 0xFFCCCCCC, true);
            ty += lineH;

            g.drawString(mc.font, "Mature nearby: §e" + harvester.getMatureCount(), tx, ty, 0xFFCCCCCC, true);
            ty += lineH;
        }

        // ── HUSBANDRY SECTION ──────────────────────────────────────────
        if (husbandryActive) {
            g.drawString(mc.font, "§eHusbandry: §aActive", tx, ty, 0xFFCCCCCC, true);
            ty += lineH;

            g.drawString(mc.font, "Animals nearby: §e" + husbandry.getAnimalCount(), tx, ty, 0xFFCCCCCC, true);
            ty += lineH;
        }

        // ── ARTISAN SECTION ─────────────────────────────────────────────
        if (tapperActive || preservesActive || wineActive) {
            g.drawString(mc.font, "§dArtisan:", tx, ty, 0xFFCCCCCC, true);
            ty += lineH;

            if (tapperActive) {
                g.drawString(mc.font, "  Tappers full: §e" + tapper.getFullCount(), tx, ty, 0xFFCCCCCC, true);
                ty += lineH;
            }
            if (preservesActive) {
                g.drawString(mc.font, "  Jars ready: §e" + preserves.getDoneCount(), tx, ty, 0xFFCCCCCC, true);
                ty += lineH;
            }
            if (wineActive) {
                g.drawString(mc.font, "  Kegs ready: §e" + wine.getDoneCount(), tx, ty, 0xFFCCCCCC, true);
                ty += lineH;
            }
        }
    }

    private String getFishingStatus(Minecraft mc) {
        if (mc.screen instanceof com.bonker.stardewfishing.client.FishingScreen) return "§aMinigame";

        if (!ModuleManager.fishingEnabled) return "§7Minigame Assist";

        FishingHook hook = mc.player.fishing;
        if (hook == null || hook.isRemoved()) return "§7Casting...";

        if (fishHack != null) {
            int catchDelay = ESVConfig.INSTANCE.catchDelay.get();
            if (fishHack.getReelTimer() <= catchDelay && fishHack.getReelTimer() > 0) return "§dHooked!";
        }
        return "§bWaiting...";
    }

    private String getDurabilityString(Minecraft mc) {
        if (fishHack == null) return "§7N/A";
        InteractionHand hand = fishHack.getRodHand(mc.player);
        if (hand == null) return "§cNo Rod";
        ItemStack stack = mc.player.getItemInHand(hand);
        if (!stack.isDamageableItem()) return "§6∞";
        int max = stack.getMaxDamage();
        int cur = max - stack.getDamageValue();
        if (cur <= ESVConfig.INSTANCE.durabilityThreshold.get()) return "§c" + cur + "/" + max;
        if (cur < max * 0.25) return "§6" + cur + "/" + max;
        return "§a" + cur + "/" + max;
    }

    private int getTotalFish(Minecraft mc) {
        var ps = mc.player.getStats();
        if (ps != null) {
            int playTime = ps.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
            if (playTime > 0 && initialTotalFish == -1)
                initialTotalFish = ps.getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT));
        }
        return (initialTotalFish != -1) ? (initialTotalFish + EasyFishing.caughtCount) : 0;
    }
}
