package com.duox.easysunlitvalley;

import com.duox.easysunlitvalley.client.ESVHUD;
import com.duox.easysunlitvalley.client.ESVKeyBindHandler;
import com.duox.easysunlitvalley.fishing.EasyFishing;
import com.duox.easysunlitvalley.harvest.EasyHarvester;
import com.duox.easysunlitvalley.husbandry.EasyHusbandry;
import com.duox.easysunlitvalley.tapper.EasyTapper;
import com.duox.easysunlitvalley.preserve.EasyPreserves;
import com.duox.easysunlitvalley.wine.EasyWine;
import com.duox.easysunlitvalley.cheese.EasyCheese;
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
        EasyFishing fishHack = new EasyFishing();
        EasyHarvester harvester = new EasyHarvester();
        EasyTapper tapper = new EasyTapper();
        EasyPreserves preserves = new EasyPreserves();
        EasyWine wine = new EasyWine();
        EasyHusbandry husbandry = new EasyHusbandry();
        EasyCheese cheese = new EasyCheese();

        // ── Register event handlers ────────────────────────────────────
        ESVKeyBindHandler keyHandler = new ESVKeyBindHandler(fishHack, harvester, tapper, preserves, wine, husbandry, cheese);
        MinecraftForge.EVENT_BUS.register(keyHandler);
        MinecraftForge.EVENT_BUS.register(fishHack); // tick + sound events
        MinecraftForge.EVENT_BUS.register(new ESVHUD(fishHack, harvester, tapper, preserves, wine, husbandry, cheese));
    }
}
