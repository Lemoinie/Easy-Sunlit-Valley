package com.duox.easysunlitvalley;

import com.duox.easysunlitvalley.config.ESVConfig;
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

    public EasySunlitValleyMod() {
        LOGGER.info("Easy Sunlit Valley initializing...");

        // ── Config ─────────────────────────────────────────────────────
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON, ESVConfig.SPEC, "easysunlitvalley-common.toml");

        // Make mod optional for connecting clients and servers
        ModLoadingContext.get().registerExtensionPoint(
                net.minecraftforge.fml.IExtensionPoint.DisplayTest.class,
                () -> net.minecraftforge.fml.IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION.get()
        );

        // ── Client Setup ───────────────────────────────────────────────
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            ClientInit.init();
        } else {
            LOGGER.info("Easy Sunlit Valley running on dedicated server - client-side automation disabled.");
        }

        LOGGER.info("Easy Sunlit Valley ready.");
    }
}
