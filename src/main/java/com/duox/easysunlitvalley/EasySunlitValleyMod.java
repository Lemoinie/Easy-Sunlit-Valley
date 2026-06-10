package com.duox.easysunlitvalley;

import com.duox.easysunlitvalley.config.ESVConfig;
import com.duox.easysunlitvalley.harvest.ForceGrowOnClickHandler;
import com.duox.easysunlitvalley.harvest.GrowthForcer;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Main entry point for Easy Sunlit Valley.
 * Safe to load on both client and dedicated servers.
 */
@Mod(EasySunlitValleyMod.MOD_ID)
public class EasySunlitValleyMod {

    public static final String MOD_ID = "easysunlitvalley";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Global reference for cross-module access (server-safe)
    public static GrowthForcer growthForcerInstance;

    public EasySunlitValleyMod() {
        LOGGER.info("Easy Sunlit Valley initializing...");

        // ── Config ─────────────────────────────────────────────────────
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT, ESVConfig.SPEC, "easysunlitvalley-client.toml");

        // ── Register server-safe event handlers ────────────────────────
        MinecraftForge.EVENT_BUS.register(new ForceGrowOnClickHandler());

        // ── Client Setup ───────────────────────────────────────────────
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            ClientInit.init();
        } else {
            LOGGER.info("Easy Sunlit Valley running on dedicated server - client-side automation disabled.");
        }

        LOGGER.info("Easy Sunlit Valley ready.");
    }
}
