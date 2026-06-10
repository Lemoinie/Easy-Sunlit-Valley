package com.duox.easysunlitvalley;

/**
 * Central enabled-flag holder for all modules.
 * Each module can be toggled independently via keybinds or the config screen.
 */
public final class ModuleManager {

    /** Auto-fishing cast/reel loop. */
    public static boolean fishingEnabled = false;

    /** Auto-harvest nearby mature crops/fruits. */
    public static boolean harvestEnabled = false;

    /** Auto-collect full tappers. */
    public static boolean tapperEnabled = false;

    /** Auto-preserve items in preserves jars. */
    public static boolean preservesEnabled = false;

    /** Auto-wine items in wine kegs. */
    public static boolean wineEnabled = false;

    /** Force-grow nearby crops (area effect, server-side). */
    public static boolean forceGrowEnabled = false;

    private ModuleManager() {}
}
