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
 * Clean, compact 2-column configuration screen for Easy Sunlit Valley.
 * Tabs: [Fishing] [Harvest] [Artisan] [HUD]
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

    private String listeningFor = null;

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
        int colW = 135;
        int leftCol = left;
        int rightCol = left + 145;
        int ly = y;
        int ry = y;

        // ── Left Column: Toggles ──
        this.addRenderableWidget(Button.builder(
            Component.literal("Fishing: " + (ModuleManager.fishingEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.fishingEnabled = !ModuleManager.fishingEnabled;
                if (!ModuleManager.fishingEnabled) fishHack.reset();
                btn.setMessage(Component.literal("Fishing: " + (ModuleManager.fishingEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(leftCol, ly, colW, 20).build());
        ly += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Bite: §e" + ESVConfig.INSTANCE.biteMode.get()),
            btn -> {
                var cur = ESVConfig.INSTANCE.biteMode.get();
                var next = cur == ESVConfig.BiteMode.SOUND ? ESVConfig.BiteMode.NIBBLE : ESVConfig.BiteMode.SOUND;
                ESVConfig.INSTANCE.biteMode.set(next);
                btn.setMessage(Component.literal("Bite: §e" + next));
            }
        ).bounds(leftCol, ly, colW, 20).build());
        ly += 25;

        this.addRenderableWidget(boolBtn("Minigame", ESVConfig.INSTANCE.autoMinigame, leftCol, ly, colW)); ly += 25;
        this.addRenderableWidget(boolBtn("Treasure", ESVConfig.INSTANCE.catchTreasure, leftCol, ly, colW)); ly += 25;
        this.addRenderableWidget(boolBtn("Dura Prot", ESVConfig.INSTANCE.durabilityProtection, leftCol, ly, colW)); ly += 25;
        this.addRenderableWidget(boolBtn("Switch Rod", ESVConfig.INSTANCE.autoSwitchRod, leftCol, ly, colW)); ly += 25;
        this.addRenderableWidget(boolBtn("Anti Detect", ESVConfig.INSTANCE.antiDetection, leftCol, ly, colW));

        // ── Right Column: Adjusters & Keybind ──
        ry = addIntAdjuster(rightCol, ry, colW, "Catch Delay", ESVConfig.INSTANCE.catchDelay, 0, 60);
        ry = addIntAdjuster(rightCol, ry, colW, "Retry Delay", ESVConfig.INSTANCE.retryDelay, 0, 100);
        ry = addIntAdjuster(rightCol, ry, colW, "Patience", ESVConfig.INSTANCE.patience, 10, 120);
        ry = addIntAdjuster(rightCol, ry, colW, "Dura Min", ESVConfig.INSTANCE.durabilityThreshold, 1, 100);
        ry += 25; // spacer
        addKeybindRow(rightCol, ry, colW, "Keybind", "fishing", ESVConfig.INSTANCE.quickEnableFishing);
    }

    // ── HARVEST TAB ────────────────────────────────────────────────────

    private void buildHarvestTab(int left, int y, int w) {
        int colW = 135;
        int leftCol = left;
        int rightCol = left + 145;
        int ly = y;
        int ry = y;

        // ── Left Column: Core Settings ──
        this.addRenderableWidget(Button.builder(
            Component.literal("Harvest: " + (ModuleManager.harvestEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.harvestEnabled = !ModuleManager.harvestEnabled;
                if (!ModuleManager.harvestEnabled) harvester.reset();
                btn.setMessage(Component.literal("Harvest: " + (ModuleManager.harvestEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(leftCol, ly, colW, 20).build());
        ly += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("Growth: " + (ModuleManager.forceGrowEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.forceGrowEnabled = !ModuleManager.forceGrowEnabled;
                if (!ModuleManager.forceGrowEnabled) com.duox.easysunlitvalley.EasySunlitValleyMod.growthForcerInstance.reset();
                btn.setMessage(Component.literal("Growth: " + (ModuleManager.forceGrowEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(leftCol, ly, colW, 20).build());
        ly += 25;

        this.addRenderableWidget(boolBtn("Grow Nearby", ESVConfig.INSTANCE.forceGrowNearby, leftCol, ly, colW)); ly += 25;
        this.addRenderableWidget(boolBtn("Grow OnClick", ESVConfig.INSTANCE.forceGrowOnClick, leftCol, ly, colW)); ly += 25;

        ly = addIntAdjuster(leftCol, ly, colW, "Radius", ESVConfig.INSTANCE.harvestScanRadius, 1, 10);
        ly = addIntAdjuster(leftCol, ly, colW, "Cooldown", ESVConfig.INSTANCE.harvestCooldownTicks, 1, 40);
        ly = addIntAdjuster(leftCol, ly, colW, "Batch Size", ESVConfig.INSTANCE.harvestBatchSize, 1, 20);
        addKeybindRow(leftCol, ly, colW, "Keybind", "harvest", ESVConfig.INSTANCE.quickEnableHarvest);

        // ── Right Column: Supported Mods checklist ──
        this.addRenderableWidget(boolBtn("Farmer's Del.", ESVConfig.INSTANCE.harvestFarmersDelight, rightCol, ry, colW)); ry += 25;
        this.addRenderableWidget(boolBtn("Farm & Charm", ESVConfig.INSTANCE.harvestFarmAndCharm, rightCol, ry, colW)); ry += 25;
        this.addRenderableWidget(boolBtn("Pam's Trees", ESVConfig.INSTANCE.harvestPamTrees, rightCol, ry, colW)); ry += 25;
        this.addRenderableWidget(boolBtn("Vinery", ESVConfig.INSTANCE.harvestVinery, rightCol, ry, colW)); ry += 25;
        this.addRenderableWidget(boolBtn("Etcetera", ESVConfig.INSTANCE.harvestEtcetera, rightCol, ry, colW)); ry += 25;
        this.addRenderableWidget(boolBtn("Vintage Del.", ESVConfig.INSTANCE.harvestVintageDelight, rightCol, ry, colW)); ry += 25;
        this.addRenderableWidget(boolBtn("Veggies Del.", ESVConfig.INSTANCE.harvestVeggiesDelight, rightCol, ry, colW));
    }

    // ── ARTISAN TAB ───────────────────────────────────────────────────

    private void buildArtisanTab(int left, int y, int w) {
        int colW = 135;

        // ── 1. Tapper Section ──
        int ty = y + 15;
        this.addRenderableWidget(Button.builder(
            Component.literal("Tapper: " + (ModuleManager.tapperEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.tapperEnabled = !ModuleManager.tapperEnabled;
                if (!ModuleManager.tapperEnabled) tapper.reset();
                btn.setMessage(Component.literal("Tapper: " + (ModuleManager.tapperEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, ty, colW, 20).build());
        addKeybindRow(left + 145, ty, colW, "Key", "tapper", ESVConfig.INSTANCE.quickEnableTapper);
        ty += 25;
        addIntAdjuster(left, ty, colW, "Radius", ESVConfig.INSTANCE.tapperScanRadius, 1, 10);
        addIntAdjuster(left + 145, ty, colW, "Cooldown", ESVConfig.INSTANCE.tapperCooldownTicks, 1, 40);

        // ── 2. Preserves Section ──
        int py = y + 80;
        this.addRenderableWidget(Button.builder(
            Component.literal("Preserves: " + (ModuleManager.preservesEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.preservesEnabled = !ModuleManager.preservesEnabled;
                if (!ModuleManager.preservesEnabled) preserves.reset();
                btn.setMessage(Component.literal("Preserves: " + (ModuleManager.preservesEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, py, colW, 20).build());
        addKeybindRow(left + 145, py, colW, "Key", "preserves", ESVConfig.INSTANCE.quickEnablePreserves);
        py += 25;
        addIntAdjuster(left, py, colW, "Radius", ESVConfig.INSTANCE.preservesScanRadius, 1, 10);
        addIntAdjuster(left + 145, py, colW, "Cooldown", ESVConfig.INSTANCE.preservesCooldownTicks, 1, 40);

        // ── 3. Wine Section ──
        int wy = y + 145;
        this.addRenderableWidget(Button.builder(
            Component.literal("Wine: " + (ModuleManager.wineEnabled ? "§aON" : "§cOFF")),
            btn -> {
                ModuleManager.wineEnabled = !ModuleManager.wineEnabled;
                if (!ModuleManager.wineEnabled) wine.reset();
                btn.setMessage(Component.literal("Wine: " + (ModuleManager.wineEnabled ? "§aON" : "§cOFF")));
            }
        ).bounds(left, wy, colW, 20).build());
        addKeybindRow(left + 145, wy, colW, "Key", "wine", ESVConfig.INSTANCE.quickEnableWine);
        wy += 25;
        addIntAdjuster(left, wy, colW, "Radius", ESVConfig.INSTANCE.wineScanRadius, 1, 10);
        addIntAdjuster(left + 145, wy, colW, "Cooldown", ESVConfig.INSTANCE.wineCooldownTicks, 1, 40);
    }

    // ── HUD TAB ────────────────────────────────────────────────────────

    private void buildHudTab(int left, int y, int w) {
        this.addRenderableWidget(boolBtn("HUD Status Overlay", ESVConfig.INSTANCE.hudEnabled, left, y, w)); y += 25;

        this.addRenderableWidget(Button.builder(
            Component.literal("HUD Position: §e" + ESVConfig.INSTANCE.hudPosition.get()),
            btn -> {
                var vals = ESVConfig.HudPosition.values();
                int idx = (ESVConfig.INSTANCE.hudPosition.get().ordinal() + 1) % vals.length;
                ESVConfig.INSTANCE.hudPosition.set(vals[idx]);
                btn.setMessage(Component.literal("HUD Position: §e" + vals[idx]));
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

    private int addIntAdjuster(int x, int y, int w, String label, ForgeConfigSpec.IntValue cfg, int min, int max) {
        this.addRenderableWidget(Button.builder(Component.literal(label), b -> {}).bounds(x, y, w - 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            int v = cfg.get(); if (v > min) { cfg.set(v - 1); cfg.save(); }
        }).bounds(x + w - 70, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            int v = cfg.get(); if (v < max) { cfg.set(v + 1); cfg.save(); }
        }).bounds(x + w - 20, y, 20, 20).build());
        return y + 25;
    }

    private void addKeybindRow(int x, int y, int w, String label, String id, ForgeConfigSpec.IntValue cfg) {
        String keyName = (listeningFor != null && listeningFor.equals(id))
                ? "> Press <" : "§e" + getKeyName(cfg.get());
        var btn = Button.builder(Component.literal(label + ": " + keyName), b -> {
            listeningFor = id;
            rebuildWidgets();
        }).bounds(x, y, w - 25, 20).build();
        this.addRenderableWidget(btn);

        this.addRenderableWidget(Button.builder(Component.literal("X"), b -> {
            listeningFor = null; cfg.set(0); cfg.save(); rebuildWidgets();
        }).bounds(x + w - 22, y, 22, 20).build());
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
        int yBase = top + 25;
        switch (currentTab) {
            case FISHING -> {
                int rx = left + 145;
                drawAdjusterValue(g, rx + 90, yBase + 6, ESVConfig.INSTANCE.catchDelay.get() + "t");
                drawAdjusterValue(g, rx + 90, yBase + 31, ESVConfig.INSTANCE.retryDelay.get() + "t");
                drawAdjusterValue(g, rx + 90, yBase + 56, ESVConfig.INSTANCE.patience.get() + "s");
                drawAdjusterValue(g, rx + 90, yBase + 81, String.valueOf(ESVConfig.INSTANCE.durabilityThreshold.get()));
            }
            case HARVEST -> {
                int lx = left;
                drawAdjusterValue(g, lx + 90, yBase + 106, ESVConfig.INSTANCE.harvestScanRadius.get() + "b");
                drawAdjusterValue(g, lx + 90, yBase + 131, ESVConfig.INSTANCE.harvestCooldownTicks.get() + "t");
                drawAdjusterValue(g, lx + 90, yBase + 156, String.valueOf(ESVConfig.INSTANCE.harvestBatchSize.get()));
            }
            case ARTISAN -> {
                // Tapper
                g.drawCenteredString(this.font, "§7─── Auto Tapper ───", left + w / 2, yBase + 5, 0xFF777777);
                drawAdjusterValue(g, left + 90, yBase + 49, ESVConfig.INSTANCE.tapperScanRadius.get() + "b");
                drawAdjusterValue(g, left + 145 + 90, yBase + 49, ESVConfig.INSTANCE.tapperCooldownTicks.get() + "t");

                // Preserves
                g.drawCenteredString(this.font, "§7─── Auto Preserves ───", left + w / 2, yBase + 70, 0xFF777777);
                drawAdjusterValue(g, left + 90, yBase + 114, ESVConfig.INSTANCE.preservesScanRadius.get() + "b");
                drawAdjusterValue(g, left + 145 + 90, yBase + 114, ESVConfig.INSTANCE.preservesCooldownTicks.get() + "t");

                // Wine
                g.drawCenteredString(this.font, "§7─── Auto Wine ───", left + w / 2, yBase + 135, 0xFF777777);
                drawAdjusterValue(g, left + 90, yBase + 179, ESVConfig.INSTANCE.wineScanRadius.get() + "b");
                drawAdjusterValue(g, left + 145 + 90, yBase + 179, ESVConfig.INSTANCE.wineCooldownTicks.get() + "t");
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
