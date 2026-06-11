package com.duox.easysunlitvalley.client;

import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.fishing.EasyFishing;
import com.duox.easysunlitvalley.harvest.EasyHarvester;
import com.duox.easysunlitvalley.husbandry.EasyHusbandry;
import com.duox.easysunlitvalley.tapper.EasyTapper;
import com.duox.easysunlitvalley.preserve.EasyPreserves;
import com.duox.easysunlitvalley.wine.EasyWine;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinds and tick driver for all modules.
 * H = open config.
 * Quick-enable keys configurable per module.
 */
public final class ESVKeyBindHandler {

    public static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.esv.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.esv");

    private final EasyFishing fishHack;
    private final EasyHarvester harvester;
    private final EasyTapper tapper;
    private final EasyPreserves preserves;
    private final EasyWine wine;
    private final EasyHusbandry husbandry;

    public ESVKeyBindHandler(EasyFishing fishHack, EasyHarvester harvester, EasyTapper tapper,
                             EasyPreserves preserves, EasyWine wine, EasyHusbandry husbandry) {
        this.fishHack = fishHack;
        this.harvester = harvester;
        this.tapper = tapper;
        this.preserves = preserves;
        this.wine = wine;
        this.husbandry = husbandry;
    }

    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (CONFIG_KEY.consumeClick() && mc.screen == null) {
            mc.setScreen(new ESVConfigScreen(fishHack, harvester, tapper, preserves, wine, husbandry));
        }

        // Quick-enable keys
        if (event.getAction() == GLFW.GLFW_PRESS && mc.screen == null) {
            int key = event.getKey();
            if (key == CONFIG_KEY.getKey().getValue()) return;

            int fishKey = ESVConfig.INSTANCE.quickEnableFishing.get();
            if (fishKey > 0 && key == fishKey) {
                ModuleManager.fishingEnabled = !ModuleManager.fishingEnabled;
                if (!ModuleManager.fishingEnabled) fishHack.reset();
                mc.player.displayClientMessage(Component.literal("Easy Fishing: "
                        + (ModuleManager.fishingEnabled ? "§aON" : "§cOFF")), true);
            }

            int harvestKey = ESVConfig.INSTANCE.quickEnableHarvest.get();
            if (harvestKey > 0 && key == harvestKey) {
                ModuleManager.harvestEnabled = !ModuleManager.harvestEnabled;
                if (!ModuleManager.harvestEnabled) harvester.reset();
                mc.player.displayClientMessage(Component.literal("Easy Harvest: "
                        + (ModuleManager.harvestEnabled ? "§aON" : "§cOFF")), true);
            }

            int tapperKey = ESVConfig.INSTANCE.quickEnableTapper.get();
            if (tapperKey > 0 && key == tapperKey) {
                ModuleManager.tapperEnabled = !ModuleManager.tapperEnabled;
                if (!ModuleManager.tapperEnabled) tapper.reset();
                mc.player.displayClientMessage(Component.literal("Easy Tapper: "
                        + (ModuleManager.tapperEnabled ? "§aON" : "§cOFF")), true);
            }

            int preservesKey = ESVConfig.INSTANCE.quickEnablePreserves.get();
            if (preservesKey > 0 && key == preservesKey) {
                ModuleManager.preservesEnabled = !ModuleManager.preservesEnabled;
                if (!ModuleManager.preservesEnabled) preserves.reset();
                mc.player.displayClientMessage(Component.literal("Easy Preserves: "
                        + (ModuleManager.preservesEnabled ? "§aON" : "§cOFF")), true);
            }

            int wineKey = ESVConfig.INSTANCE.quickEnableWine.get();
            if (wineKey > 0 && key == wineKey) {
                ModuleManager.wineEnabled = !ModuleManager.wineEnabled;
                if (!ModuleManager.wineEnabled) wine.reset();
                mc.player.displayClientMessage(Component.literal("Easy Wine: "
                        + (ModuleManager.wineEnabled ? "§aON" : "§cOFF")), true);
            }

            int husbandryKey = ESVConfig.INSTANCE.quickEnableHusbandry.get();
            if (husbandryKey > 0 && key == husbandryKey) {
                ModuleManager.husbandryEnabled = !ModuleManager.husbandryEnabled;
                if (!ModuleManager.husbandryEnabled) husbandry.reset();
                mc.player.displayClientMessage(Component.literal("Easy Husbandry: "
                        + (ModuleManager.husbandryEnabled ? "§aON" : "§cOFF")), true);
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
        if (ModuleManager.husbandryEnabled) husbandry.tick();
    }
}
