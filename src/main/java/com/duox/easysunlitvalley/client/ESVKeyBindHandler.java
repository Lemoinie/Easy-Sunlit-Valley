package com.duox.easysunlitvalley.client;

import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.fishing.AutoFishHack;
import com.duox.easysunlitvalley.harvest.AutoHarvester;
import com.duox.easysunlitvalley.harvest.GrowthForcer;
import com.duox.easysunlitvalley.tapper.AutoTapper;
import com.duox.easysunlitvalley.preserve.AutoPreserves;
import com.duox.easysunlitvalley.wine.AutoWine;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinds and tick driver for all modules.
 * H = open config, G = toggle force-growth.
 * Quick-enable keys configurable per module.
 */
public final class ESVKeyBindHandler {

    public static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.esv.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.esv");

    public static final KeyMapping FORCE_GROW_KEY = new KeyMapping(
            "key.esv.forcegrow", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.esv");

    private final AutoFishHack fishHack;
    private final AutoHarvester harvester;
    private final GrowthForcer growthForcer;
    private final AutoTapper tapper;
    private final AutoPreserves preserves;
    private final AutoWine wine;

    public ESVKeyBindHandler(AutoFishHack fishHack, AutoHarvester harvester,
                             GrowthForcer growthForcer, AutoTapper tapper,
                             AutoPreserves preserves, AutoWine wine) {
        this.fishHack = fishHack;
        this.harvester = harvester;
        this.growthForcer = growthForcer;
        this.tapper = tapper;
        this.preserves = preserves;
        this.wine = wine;
    }

    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
        event.register(FORCE_GROW_KEY);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (CONFIG_KEY.consumeClick() && mc.screen == null) {
            mc.setScreen(new ESVConfigScreen(fishHack, harvester, tapper, preserves, wine));
        }

        if (FORCE_GROW_KEY.consumeClick() && mc.screen == null) {
            ModuleManager.forceGrowEnabled = !ModuleManager.forceGrowEnabled;
            if (!ModuleManager.forceGrowEnabled) growthForcer.reset();
            mc.player.displayClientMessage(Component.literal("Force Growth: "
                    + (ModuleManager.forceGrowEnabled ? "§aON" : "§cOFF")), true);
        }

        // Quick-enable keys
        if (event.getAction() == GLFW.GLFW_PRESS && mc.screen == null) {
            int key = event.getKey();
            if (key == CONFIG_KEY.getKey().getValue() || key == FORCE_GROW_KEY.getKey().getValue()) return;

            int fishKey = ESVConfig.INSTANCE.quickEnableFishing.get();
            if (fishKey > 0 && key == fishKey) {
                ModuleManager.fishingEnabled = !ModuleManager.fishingEnabled;
                if (!ModuleManager.fishingEnabled) fishHack.reset();
                mc.player.displayClientMessage(Component.literal("Auto Fish: "
                        + (ModuleManager.fishingEnabled ? "§aON" : "§cOFF")), true);
            }

            int harvestKey = ESVConfig.INSTANCE.quickEnableHarvest.get();
            if (harvestKey > 0 && key == harvestKey) {
                ModuleManager.harvestEnabled = !ModuleManager.harvestEnabled;
                if (!ModuleManager.harvestEnabled) harvester.reset();
                mc.player.displayClientMessage(Component.literal("Auto Harvest: "
                        + (ModuleManager.harvestEnabled ? "§aON" : "§cOFF")), true);
            }

            int tapperKey = ESVConfig.INSTANCE.quickEnableTapper.get();
            if (tapperKey > 0 && key == tapperKey) {
                ModuleManager.tapperEnabled = !ModuleManager.tapperEnabled;
                if (!ModuleManager.tapperEnabled) tapper.reset();
                mc.player.displayClientMessage(Component.literal("Auto Tapper: "
                        + (ModuleManager.tapperEnabled ? "§aON" : "§cOFF")), true);
            }

            int preservesKey = ESVConfig.INSTANCE.quickEnablePreserves.get();
            if (preservesKey > 0 && key == preservesKey) {
                ModuleManager.preservesEnabled = !ModuleManager.preservesEnabled;
                if (!ModuleManager.preservesEnabled) preserves.reset();
                mc.player.displayClientMessage(Component.literal("Auto Preserves: "
                        + (ModuleManager.preservesEnabled ? "§aON" : "§cOFF")), true);
            }

            int wineKey = ESVConfig.INSTANCE.quickEnableWine.get();
            if (wineKey > 0 && key == wineKey) {
                ModuleManager.wineEnabled = !ModuleManager.wineEnabled;
                if (!ModuleManager.wineEnabled) wine.reset();
                mc.player.displayClientMessage(Component.literal("Auto Wine: "
                        + (ModuleManager.wineEnabled ? "§aON" : "§cOFF")), true);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (ModuleManager.harvestEnabled) harvester.tick();
        if (ModuleManager.tapperEnabled) tapper.tick();
        if (ModuleManager.preservesEnabled) preserves.tick();
        if (ModuleManager.wineEnabled) wine.tick();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ModuleManager.forceGrowEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getSingleplayerServer() == null) return;
        var playerList = mc.getSingleplayerServer().getPlayerList();
        if (playerList == null) return;
        for (ServerPlayer sp : playerList.getPlayers()) {
            growthForcer.tick(sp);
        }
    }
}
