package com.duox.easysunlitvalley;

/**
 * Central enabled-flag holder for all modules.
 * Each module can be toggled independently via keybinds or the config screen.
 */
public final class ModuleManager {

    /** Easy-fishing cast/reel loop. */
    public static boolean fishingEnabled = false;

    /** Easy-harvest nearby mature crops/fruits. */
    public static boolean harvestEnabled = false;

    /** Easy-collect full tappers. */
    public static boolean tapperEnabled = false;

    /** Easy-preserve items in preserves jars. */
    public static boolean preservesEnabled = false;

    /** Easy-wine items in wine kegs. */
    public static boolean wineEnabled = false;

    /** Easy-husbandry right-clicks nearby farm animals. */
    public static boolean husbandryEnabled = false;

    /** Force-grow nearby crops (area effect, server-side). */
    public static boolean forceGrowEnabled = false;

    private ModuleManager() {}
}
