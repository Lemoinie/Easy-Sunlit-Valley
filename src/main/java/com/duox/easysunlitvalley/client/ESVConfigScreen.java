package com.duox.easysunlitvalley.client;

import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.fishing.AutoFishHack;
import com.duox.easysunlitvalley.harvest.AutoHarvester;
import com.duox.easysunlitvalley.tapper.AutoTapper;
import com.duox.easysunlitvalley.preserve.AutoPreserves;
import com.duox.easysunlitvalley.wine.AutoWine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import org.lwjgl.glfw.GLFW;

/**
 * Tabbed configuration screen for Easy Sunlit Valley.
 * Tabs: [Fishing] [Harvest] [Tapper] [HUD]
 */
public class ESVConfigScreen extends Screen {

    private static final Component TITLE = Component.literal("Easy Sunlit Valley");

    private enum Tab { FISHING, HARVEST, ARTISAN, HUD }
    private Tab currentTab = Tab.FISHING;

    private final AutoFishHack fishHack;
    private final AutoHarvester harvester;
    private final AutoTapper tapper;
    private final AutoPreserves preserves;
    private final AutoWine wine;

    // Keybind listening state
    private String listeningFor = null; // "fishing", "harvest", "tapper" or null

    public ESVConfigScreen(AutoFishHack fishHack, AutoHarvester harvester, 
                            AutoTapper tapper, AutoPreserves preserves, AutoWine wine) {
        super(TITLE);
        this.fishHack = fishHack;
        this.harvester = harvester;
        this.tapper = tapper;
        this.preserves = preserves;
        this.wine = wine;
    }

    @Override
    protected void init() {
        listeningFor = null;
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        int cardW = 280;
        int left = (this.width - cardW) / 2;
        int top = (this.height - 380) / 2;

        // ── Tab buttons ────────────────────────────────────────────────
        int tabW = cardW / 4;
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            String label = tab == currentTab ? "§a" + tab.name() : "§7" + tab.name();
            this.addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                currentTab = tab;
                rebuildWidgets();
            }).bounds(left + i * tabW, top, tabW, 18).build());
        }

        int y = top + 25;

        switch (currentTab) {
            case FISHING -> buildFishingTab(left, y, cardW);
            case HARVEST -> buildHarvestTab(left, y, cardW);
            case ARTISAN -> buildArtisanTab(left, y, cardW);
            case HUD -> buildHudTab(left, y, cardW);
        }
    }

    // ── FISHING TAB ────────────────────────────────────────────────────

    private void buildFishingTab(int left, int y, int w) {
        // Enable toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Fishing: " + (ModuleManager.fishingEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.fishingEnabled = !ModuleManager.fishingEnabled;
                if (!ModuleManager.fishingEnabled) fishHack.reset();
                btn.setMessage(Component.literal("Auto Fishing: " + (ModuleManager.fishingEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, y, w, 20).build());
        y += 25;

        // Bite Mode
        this.addRenderableWidget(Button.builder(
            Component.literal("Bite Mode: §e" + ESVConfig.INSTANCE.biteMode.get()),
            btn -> {
                var cur = ESVConfig.INSTANCE.biteMode.get();
                var next = cur == ESVConfig.BiteMode.SOUND ? ESVConfig.BiteMode.NIBBLE : ESVConfig.BiteMode.SOUND;
                ESVConfig.INSTANCE.biteMode.set(next);
                btn.setMessage(Component.literal("Bite Mode: §e" + next));
            }
        ).bounds(left, y, w, 20).build());
        y += 25;

        // Auto Minigame
        this.addRenderableWidget(boolBtn("Auto Minigame", ESVConfig.INSTANCE.autoMinigame, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Catch Treasure", ESVConfig.INSTANCE.catchTreasure, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Durability Protection", ESVConfig.INSTANCE.durabilityProtection, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Anti Detection", ESVConfig.INSTANCE.antiDetection, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Auto Switch Rod", ESVConfig.INSTANCE.autoSwitchRod, left, y, w)); y += 25;

        // Numeric adjusters
        y = addIntAdjuster(left, y, w, "Catch Delay", ESVConfig.INSTANCE.catchDelay, 0, 60);
        y = addIntAdjuster(left, y, w, "Retry Delay", ESVConfig.INSTANCE.retryDelay, 0, 100);
        y = addIntAdjuster(left, y, w, "Patience (sec)", ESVConfig.INSTANCE.patience, 10, 120);
        y = addIntAdjuster(left, y, w, "Durability Threshold", ESVConfig.INSTANCE.durabilityThreshold, 1, 100);

        // Quick enable key
        addKeybindRow(left, y, w, "Quick Enable", "fishing", ESVConfig.INSTANCE.quickEnableFishing);
    }

    // ── HARVEST TAB ────────────────────────────────────────────────────

    private void buildHarvestTab(int left, int y, int w) {
        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Harvest: " + (ModuleManager.harvestEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.harvestEnabled = !ModuleManager.harvestEnabled;
                if (!ModuleManager.harvestEnabled) harvester.reset();
                btn.setMessage(Component.literal("Auto Harvest: " + (ModuleManager.harvestEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, y, w, 20).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Force Growth: " + (ModuleManager.forceGrowEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.forceGrowEnabled = !ModuleManager.forceGrowEnabled;
                if (!ModuleManager.forceGrowEnabled) com.duox.easysunlitvalley.EasySunlitValleyMod.growthForcerInstance.reset();
                btn.setMessage(Component.literal("Force Growth: " + (ModuleManager.forceGrowEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, y, w, 20).build());
        y += 25;

        this.addRenderableWidget(boolBtn("Force Grow (Nearby)", ESVConfig.INSTANCE.forceGrowNearby, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Force Grow (On Click)", ESVConfig.INSTANCE.forceGrowOnClick, left, y, w)); y += 30;

        // Per-mod toggles
        this.addRenderableWidget(boolBtn("Farmer's Delight", ESVConfig.INSTANCE.harvestFarmersDelight, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Farm & Charm", ESVConfig.INSTANCE.harvestFarmAndCharm, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Pam's HC2 Trees", ESVConfig.INSTANCE.harvestPamTrees, left, y, w)); y += 25;
        this.addRenderableWidget(boolBtn("Vinery", ESVConfig.INSTANCE.harvestVinery, left, y, w)); y += 30;

        // Speed settings
        y = addIntAdjuster(left, y, w, "Scan Radius", ESVConfig.INSTANCE.harvestScanRadius, 1, 10);
        y = addIntAdjuster(left, y, w, "Cooldown", ESVConfig.INSTANCE.harvestCooldownTicks, 1, 40);
        y = addIntAdjuster(left, y, w, "Batch Size", ESVConfig.INSTANCE.harvestBatchSize, 1, 20);

        addKeybindRow(left, y, w, "Quick Enable", "harvest", ESVConfig.INSTANCE.quickEnableHarvest);
    }

    // ── ARTISAN TAB ───────────────────────────────────────────────────

    private void buildArtisanTab(int left, int y, int w) {
        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Tapper: " + (ModuleManager.tapperEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.tapperEnabled = !ModuleManager.tapperEnabled;
                if (!ModuleManager.tapperEnabled) tapper.reset();
                btn.setMessage(Component.literal("Auto Tapper: " + (ModuleManager.tapperEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, y, w / 2 - 5, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Preserves: " + (ModuleManager.preservesEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.preservesEnabled = !ModuleManager.preservesEnabled;
                if (!ModuleManager.preservesEnabled) preserves.reset();
                btn.setMessage(Component.literal("Auto Preserves: " + (ModuleManager.preservesEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left + w / 2 + 5, y, w / 2 - 5, 20).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Wine: " + (ModuleManager.wineEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.wineEnabled = !ModuleManager.wineEnabled;
                if (!ModuleManager.wineEnabled) wine.reset();
                btn.setMessage(Component.literal("Auto Wine: " + (ModuleManager.wineEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, y, w, 20).build());
        y += 30;

        y = addIntAdjuster(left, y, w, "Tapper Scan Radius", ESVConfig.INSTANCE.tapperScanRadius, 1, 10);
        y = addIntAdjuster(left, y, w, "Tapper Cooldown", ESVConfig.INSTANCE.tapperCooldownTicks, 1, 40);
        addKeybindRow(left, y, w, "Tapper Keybind", "tapper", ESVConfig.INSTANCE.quickEnableTapper);
        y += 30;

        y = addIntAdjuster(left, y, w, "Preserves Scan Radius", ESVConfig.INSTANCE.preservesScanRadius, 1, 10);
        y = addIntAdjuster(left, y, w, "Preserves Cooldown", ESVConfig.INSTANCE.preservesCooldownTicks, 1, 40);
        addKeybindRow(left, y, w, "Preserves Keybind", "preserves", ESVConfig.INSTANCE.quickEnablePreserves);
        y += 30;

        y = addIntAdjuster(left, y, w, "Wine Scan Radius", ESVConfig.INSTANCE.wineScanRadius, 1, 10);
        y = addIntAdjuster(left, y, w, "Wine Cooldown", ESVConfig.INSTANCE.wineCooldownTicks, 1, 40);
        addKeybindRow(left, y, w, "Wine Keybind", "wine", ESVConfig.INSTANCE.quickEnableWine);
    }

    // ── HUD TAB ────────────────────────────────────────────────────────

    private void buildHudTab(int left, int y, int w) {
        this.addRenderableWidget(boolBtn("HUD Enabled", ESVConfig.INSTANCE.hudEnabled, left, y, w)); y += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Position: §e" + ESVConfig.INSTANCE.hudPosition.get()),
            btn -> {
                var vals = ESVConfig.HudPosition.values();
                int idx = (ESVConfig.INSTANCE.hudPosition.get().ordinal() + 1) % vals.length;
                ESVConfig.INSTANCE.hudPosition.set(vals[idx]);
                btn.setMessage(Component.literal("Position: §e" + vals[idx]));
            }
        ).bounds(left, y, w, 20).build());
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private Button boolBtn(String label, ForgeConfigSpec.BooleanValue cfg, int x, int y, int w) {
        return Button.builder(
            Component.literal(label + ": " + (cfg.get() ? "§aON" : "§cOFF")),
            btn -> { boolean n = !cfg.get(); cfg.set(n); cfg.save(); btn.setMessage(Component.literal(label + ": " + (n ? "§aON" : "§cOFF"))); }
        ).bounds(x, y, w, 20).build();
    }

    private int addIntAdjuster(int left, int y, int w, String label, ForgeConfigSpec.IntValue cfg, int min, int max) {
        this.addRenderableWidget(Button.builder(Component.literal(label), b -> {}).bounds(left, y, w - 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            int v = cfg.get(); if (v > min) { cfg.set(v - 1); cfg.save(); }
        }).bounds(left + w - 90, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            int v = cfg.get(); if (v < max) { cfg.set(v + 1); cfg.save(); }
        }).bounds(left + w - 20, y, 20, 20).build());
        return y + 25;
    }

    private void addKeybindRow(int left, int y, int w, String label, String id, ForgeConfigSpec.IntValue cfg) {
        String keyName = (listeningFor != null && listeningFor.equals(id))
                ? "> Press Key <" : "§e" + getKeyName(cfg.get());
        var btn = Button.builder(Component.literal(label + ": " + keyName), b -> {
            listeningFor = id;
            rebuildWidgets();
        }).bounds(left, y, w - 50, 20).build();
        this.addRenderableWidget(btn);

        this.addRenderableWidget(Button.builder(Component.literal("X"), b -> {
            listeningFor = null; cfg.set(0); cfg.save(); rebuildWidgets();
        }).bounds(left + w - 45, y, 45, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningFor != null) {
            ForgeConfigSpec.IntValue cfg = switch (listeningFor) {
                case "fishing" -> ESVConfig.INSTANCE.quickEnableFishing;
                case "harvest" -> ESVConfig.INSTANCE.quickEnableHarvest;
                case "tapper" -> ESVConfig.INSTANCE.quickEnableTapper;
                case "preserves" -> ESVConfig.INSTANCE.quickEnablePreserves;
                case "wine" -> ESVConfig.INSTANCE.quickEnableWine;
                default -> null;
            };
            if (cfg != null) { cfg.set(keyCode); cfg.save(); }
            listeningFor = null;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ── Rendering ──────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int cardW = 280;
        int left = (this.width - cardW) / 2;
        int top = (this.height - 380) / 2;

        // Card background
        g.fill(left - 8, top - 4, left + cardW + 8, top + 380, 0xD0101015);
        g.fill(left - 8, top - 4, left + cardW + 8, top - 3, 0xFF5CA848);

        // Title
        g.drawCenteredString(this.font, TITLE, this.width / 2, top - 16, 0xFFFFFFFF);

        super.render(g, mouseX, mouseY, partialTick);

        // Draw numeric values for adjusters
        renderAdjusterValues(g, left, top, cardW);
    }

    private void renderAdjusterValues(GuiGraphics g, int left, int top, int w) {
        // Draw the current value between the +/- buttons for each adjuster visible in the current tab
        // We iterate through the rendered widgets to find adjuster rows
        // Simpler approach: just render based on known positions for each tab

        int yBase = top + 25;
        switch (currentTab) {
            case FISHING -> {
                int y = yBase + 25 * 7; // After 7 rows of toggles
                drawAdjusterValue(g, left + w - 55, y + 6, ESVConfig.INSTANCE.catchDelay.get() + "t");
                drawAdjusterValue(g, left + w - 55, y + 31, ESVConfig.INSTANCE.retryDelay.get() + "t");
                drawAdjusterValue(g, left + w - 55, y + 56, ESVConfig.INSTANCE.patience.get() + "s");
                drawAdjusterValue(g, left + w - 55, y + 81, String.valueOf(ESVConfig.INSTANCE.durabilityThreshold.get()));
            }
            case HARVEST -> {
                int y = yBase + 25 * 5 + 30 * 2; // After toggles + per-mod (Force Growth toggle added)
                drawAdjusterValue(g, left + w - 55, y + 6, ESVConfig.INSTANCE.harvestScanRadius.get() + "b");
                drawAdjusterValue(g, left + w - 55, y + 31, ESVConfig.INSTANCE.harvestCooldownTicks.get() + "t");
                drawAdjusterValue(g, left + w - 55, y + 56, String.valueOf(ESVConfig.INSTANCE.harvestBatchSize.get()));
            }
            case ARTISAN -> {
                int y = yBase + 25 + 30; // After the toggle rows (Tapper/Preserves + Wine)
                // Tapper adjusters
                drawAdjusterValue(g, left + w - 55, y + 6, ESVConfig.INSTANCE.tapperScanRadius.get() + "b");
                drawAdjusterValue(g, left + w - 55, y + 31, ESVConfig.INSTANCE.tapperCooldownTicks.get() + "t");
                y += 25 + 25 + 30; // Scan, Cooldown, Keybind
                // Preserves adjusters
                drawAdjusterValue(g, left + w - 55, y + 6, ESVConfig.INSTANCE.preservesScanRadius.get() + "b");
                drawAdjusterValue(g, left + w - 55, y + 31, ESVConfig.INSTANCE.preservesCooldownTicks.get() + "t");
                y += 25 + 25 + 30;
                // Wine adjusters
                drawAdjusterValue(g, left + w - 55, y + 6, ESVConfig.INSTANCE.wineScanRadius.get() + "b");
                drawAdjusterValue(g, left + w - 55, y + 31, ESVConfig.INSTANCE.wineCooldownTicks.get() + "t");
            }
        }
    }

    private void drawAdjusterValue(GuiGraphics g, int x, int y, String text) {
        g.drawCenteredString(this.font, text, x, y, 0xFFFFFFFF);
    }

    private String getKeyName(int keyCode) {
        if (keyCode <= 0 || keyCode == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null) return name.toUpperCase();
        return switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            default -> "KEY " + keyCode;
        };
    }

    @Override public boolean isPauseScreen() { return false; }
}
