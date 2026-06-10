package com.duox.easysunlitvalley;

import com.duox.easysunlitvalley.client.ESVHUD;
import com.duox.easysunlitvalley.client.ESVKeyBindHandler;
import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.fishing.AutoFishHack;
import com.duox.easysunlitvalley.harvest.AutoHarvester;
import com.duox.easysunlitvalley.harvest.ForceGrowOnClickHandler;
import com.duox.easysunlitvalley.harvest.GrowthForcer;
import com.duox.easysunlitvalley.tapper.AutoTapper;
import com.duox.easysunlitvalley.preserve.AutoPreserves;
import com.duox.easysunlitvalley.wine.AutoWine;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main entry point for Easy Sunlit Valley.
 *
 * <p>Combines three automation modules:
 * <ul>
 *   <li><b>Auto Fishing</b> — cast/reel loop + minigame automation</li>
 *   <li><b>Auto Harvest</b> — right-click mature crops/fruits</li>
 *   <li><b>Easy Tapper</b> — right-click full tappers (society:tapper)</li>
 * </ul>
 *
 * <p><b>NEVER left-clicks.</b> All actions use right-click simulation.
 */
@Mod(EasySunlitValleyMod.MOD_ID)
public class EasySunlitValleyMod {

    public static final String MOD_ID = "easysunlitvalley";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Global references for cross-module access
    public static AutoFishHack fishHackInstance;
    public static AutoHarvester harvesterInstance;
    public static GrowthForcer growthForcerInstance;
    public static AutoTapper tapperInstance;
    public static AutoPreserves preservesInstance;
    public static AutoWine wineInstance;

    public EasySunlitValleyMod() {
        LOGGER.info("Easy Sunlit Valley initializing...");

        // ── Config ─────────────────────────────────────────────────────
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT, ESVConfig.SPEC, "easysunlitvalley-client.toml");

        // ── Keybinds (mod bus) ─────────────────────────────────────────
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(ESVKeyBindHandler::registerBindings);

        // ── Create module instances ────────────────────────────────────
        AutoFishHack fishHack = new AutoFishHack();
        AutoHarvester harvester = new AutoHarvester();
        GrowthForcer growthForcer = new GrowthForcer();
        AutoTapper tapper = new AutoTapper();
        AutoPreserves preserves = new AutoPreserves();
        AutoWine wine = new AutoWine();
 
        fishHackInstance = fishHack;
        harvesterInstance = harvester;
        growthForcerInstance = growthForcer;
        tapperInstance = tapper;
        preservesInstance = preserves;
        wineInstance = wine;

        // ── Register event handlers ────────────────────────────────────
        ESVKeyBindHandler keyHandler = new ESVKeyBindHandler(fishHack, harvester, growthForcer, tapper, preserves, wine);
        MinecraftForge.EVENT_BUS.register(keyHandler);
        MinecraftForge.EVENT_BUS.register(fishHack); // tick + sound events
        MinecraftForge.EVENT_BUS.register(new ESVHUD(fishHack, harvester, tapper, preserves, wine));
        MinecraftForge.EVENT_BUS.register(new ForceGrowOnClickHandler());

        LOGGER.info("Easy Sunlit Valley ready — H=settings.");
    }
}
