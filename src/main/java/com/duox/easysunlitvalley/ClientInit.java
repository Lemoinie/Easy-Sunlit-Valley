package com.duox.easysunlitvalley;

import com.duox.easysunlitvalley.client.ESVHUD;
import com.duox.easysunlitvalley.client.ESVKeyBindHandler;
import com.duox.easysunlitvalley.fishing.AutoFishHack;
import com.duox.easysunlitvalley.harvest.AutoHarvester;
import com.duox.easysunlitvalley.harvest.GrowthForcer;
import com.duox.easysunlitvalley.tapper.AutoTapper;
import com.duox.easysunlitvalley.preserve.AutoPreserves;
import com.duox.easysunlitvalley.wine.AutoWine;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Client-side initialization to prevent classloading crashes on dedicated servers. */
public final class ClientInit {

    private ClientInit() {}

    public static void init() {
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

        EasySunlitValleyMod.growthForcerInstance = growthForcer;

        // ── Register event handlers ────────────────────────────────────
        ESVKeyBindHandler keyHandler = new ESVKeyBindHandler(fishHack, harvester, growthForcer, tapper, preserves, wine);
        MinecraftForge.EVENT_BUS.register(keyHandler);
        MinecraftForge.EVENT_BUS.register(fishHack); // tick + sound events
        MinecraftForge.EVENT_BUS.register(new ESVHUD(fishHack, harvester, tapper, preserves, wine));
    }
}
