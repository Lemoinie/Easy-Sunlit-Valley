package com.duox.easysunlitvalley.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Unified configuration for Easy Sunlit Valley.
 * Combines fishing, harvesting, and tapper settings into one spec.
 *
 * <p>Stored in {@code .minecraft/config/easysunlitvalley-client.toml}.
 */
public final class ESVConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ESVConfig INSTANCE;

    // ══════════════════════════════════════════════════════════════════
    //  HUD
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.BooleanValue hudEnabled;
    public final ForgeConfigSpec.EnumValue<HudPosition> hudPosition;

    public enum HudPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
        @Override public String toString() { return name().replace('_', ' '); }
    }

    // ══════════════════════════════════════════════════════════════════
    //  FISHING
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.EnumValue<BiteMode> biteMode;
    public final ForgeConfigSpec.IntValue catchDelay;
    public final ForgeConfigSpec.IntValue retryDelay;
    public final ForgeConfigSpec.IntValue patience;
    public final ForgeConfigSpec.BooleanValue durabilityProtection;
    public final ForgeConfigSpec.IntValue durabilityThreshold;
    public final ForgeConfigSpec.BooleanValue antiDetection;
    public final ForgeConfigSpec.IntValue randomDelayRange;
    public final ForgeConfigSpec.BooleanValue easySwitchRod;
    public final ForgeConfigSpec.BooleanValue easyMinigame;
    public final ForgeConfigSpec.BooleanValue catchTreasure;
    public final ForgeConfigSpec.IntValue quickEnableFishing;

    public enum BiteMode {
        SOUND, NIBBLE;
        @Override public String toString() { return name(); }
    }

    // ══════════════════════════════════════════════════════════════════
    //  HARVESTING
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.IntValue harvestScanRadius;
    public final ForgeConfigSpec.IntValue harvestCooldownTicks;
    public final ForgeConfigSpec.IntValue harvestScanIntervalTicks;
    public final ForgeConfigSpec.IntValue harvestBatchSize;
    public final ForgeConfigSpec.BooleanValue harvestFarmersDelight;
    public final ForgeConfigSpec.BooleanValue harvestFarmAndCharm;
    public final ForgeConfigSpec.BooleanValue harvestPamTrees;
    public final ForgeConfigSpec.BooleanValue harvestVinery;
    public final ForgeConfigSpec.BooleanValue harvestEtcetera;
    public final ForgeConfigSpec.BooleanValue harvestVintageDelight;
    public final ForgeConfigSpec.BooleanValue harvestVeggiesDelight;
    public final ForgeConfigSpec.IntValue quickEnableHarvest;

    // ══════════════════════════════════════════════════════════════════
    //  TAPPER
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.IntValue tapperScanRadius;
    public final ForgeConfigSpec.IntValue tapperCooldownTicks;
    public final ForgeConfigSpec.IntValue tapperScanIntervalTicks;
    public final ForgeConfigSpec.IntValue quickEnableTapper;

    // ══════════════════════════════════════════════════════════════════
    //  PRESERVES
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.IntValue preservesScanRadius;
    public final ForgeConfigSpec.IntValue preservesCooldownTicks;
    public final ForgeConfigSpec.IntValue preservesScanIntervalTicks;
    public final ForgeConfigSpec.IntValue quickEnablePreserves;

    // ══════════════════════════════════════════════════════════════════
    //  WINE
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.IntValue wineScanRadius;
    public final ForgeConfigSpec.IntValue wineCooldownTicks;
    public final ForgeConfigSpec.IntValue wineScanIntervalTicks;
    public final ForgeConfigSpec.IntValue quickEnableWine;

    // ══════════════════════════════════════════════════════════════════
    //  CHEESE
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.IntValue cheeseScanRadius;
    public final ForgeConfigSpec.IntValue cheeseCooldownTicks;
    public final ForgeConfigSpec.IntValue cheeseScanIntervalTicks;
    public final ForgeConfigSpec.IntValue quickEnableCheese;

    // ══════════════════════════════════════════════════════════════════
    //  HUSBANDRY
    // ══════════════════════════════════════════════════════════════════
    public final ForgeConfigSpec.DoubleValue husbandryScanRadius;
    public final ForgeConfigSpec.IntValue husbandryCooldownTicks;
    public final ForgeConfigSpec.IntValue husbandryScanIntervalTicks;
    public final ForgeConfigSpec.IntValue husbandryBatchSize;
    public final ForgeConfigSpec.IntValue quickEnableHusbandry;

    static {
        var builder = new ForgeConfigSpec.Builder();
        INSTANCE = new ESVConfig(builder);
        SPEC = builder.build();
    }

    private ESVConfig(ForgeConfigSpec.Builder builder) {

        // ── HUD ────────────────────────────────────────────────────────
        builder.comment("HUD overlay settings.").push("hud");
        hudEnabled = builder.comment("Show the on-screen status HUD.").define("enabled", true);
        hudPosition = builder.comment("Corner of the screen for the HUD.").defineEnum("position", HudPosition.TOP_LEFT);
        builder.pop();

        // ── FISHING ────────────────────────────────────────────────────
        builder.comment("Easy Fishing settings.").push("fishing");

        biteMode = builder
                .comment("Bite detection mode.",
                         "SOUND — listens for bobber splash sound (default, works everywhere).",
                         "NIBBLE — reads fishing hook nibble state directly (more accurate, requires stardew_fishing).")
                .defineEnum("biteMode", BiteMode.SOUND);

        catchDelay = builder.comment("Ticks to wait after bite before reeling. Range: 0-60.")
                .defineInRange("catchDelay", 0, 0, 60);

        retryDelay = builder.comment("Ticks to wait after cast/reel before next action. Range: 0-100.")
                .defineInRange("retryDelay", 15, 0, 100);

        patience = builder.comment("Seconds to wait for a bite before auto-recasting. Range: 10-120.")
                .defineInRange("patience", 60, 10, 120);

        durabilityProtection = builder.comment("Stop fishing when rod durability gets low.")
                .define("durabilityProtection", true);

        durabilityThreshold = builder.comment("Min durability to keep fishing. Range: 1-100.")
                .defineInRange("durabilityThreshold", 3, 1, 100);

        antiDetection = builder.comment("Randomize delays slightly to evade anti-cheat.")
                .define("antiDetection", false);

        randomDelayRange = builder.comment("Max ticks of random variation. Range: 0-40.")
                .defineInRange("randomDelayRange", 5, 0, 40);

        easySwitchRod = builder.comment("Easy switch to another rod when current one is low.")
                .define("easySwitchRod", true);

        easyMinigame = builder.comment("Automatically catch fish during the minigame.")
                .define("easyMinigame", true);

        catchTreasure = builder.comment("Prioritize catching treasure chests during minigame when safe.")
                .define("catchTreasure", true);

        quickEnableFishing = builder.comment("GLFW key code for quick-toggle fishing (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();

        // ── HARVESTING ─────────────────────────────────────────────────
        builder.comment("Easy Harvesting settings.").push("harvesting");

        harvestScanRadius = builder.comment("Block scan radius.").defineInRange("scanRadius", 5, 1, 10);
        harvestCooldownTicks = builder.comment("Ticks between harvest cycles.").defineInRange("cooldownTicks", 1, 1, 40);
        harvestScanIntervalTicks = builder.comment("Ticks between full rescans.").defineInRange("scanIntervalTicks", 5, 1, 40);
        harvestBatchSize = builder.comment("Blocks to harvest per cycle.").defineInRange("batchSize", 4, 1, 20);

        harvestFarmersDelight = builder.define("farmersDelight", true);
        harvestFarmAndCharm = builder.define("farmAndCharm", true);
        harvestPamTrees = builder.define("pamTrees", true);
        harvestVinery = builder.define("vinery", true);
        harvestEtcetera = builder.define("etcetera", true);
        harvestVintageDelight = builder.define("vintagedelight", true);
        harvestVeggiesDelight = builder.define("veggiesdelight", true);

        quickEnableHarvest = builder.comment("GLFW key code for quick-toggle harvest (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();

        // ── TAPPER ─────────────────────────────────────────────────────
        builder.comment("Easy Tapper settings (society:tapper).").push("tapper");

        tapperScanRadius = builder.comment("Block scan radius for tappers.").defineInRange("scanRadius", 5, 1, 10);
        tapperCooldownTicks = builder.comment("Ticks between tapper harvest cycles.").defineInRange("cooldownTicks", 2, 1, 40);
        tapperScanIntervalTicks = builder.comment("Ticks between tapper rescans.").defineInRange("scanIntervalTicks", 10, 1, 40);

        quickEnableTapper = builder.comment("GLFW key code for quick-toggle tapper (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();

        // ── PRESERVES ──────────────────────────────────────────────────
        builder.comment("Easy Preserves settings (society:preserves_jar).").push("preserves");

        preservesScanRadius = builder.comment("Block scan radius for preserves jars.").defineInRange("scanRadius", 5, 1, 10);
        preservesCooldownTicks = builder.comment("Ticks between preserves harvest/insert cycles.").defineInRange("cooldownTicks", 2, 1, 40);
        preservesScanIntervalTicks = builder.comment("Ticks between preserves rescans.").defineInRange("scanIntervalTicks", 10, 1, 40);

        quickEnablePreserves = builder.comment("GLFW key code for quick-toggle preserves (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();

        // ── WINE ───────────────────────────────────────────────────────
        builder.comment("Easy Wine settings (society:wine_keg).").push("wine");

        wineScanRadius = builder.comment("Block scan radius for wine kegs.").defineInRange("scanRadius", 5, 1, 10);
        wineCooldownTicks = builder.comment("Ticks between wine harvest/insert cycles.").defineInRange("cooldownTicks", 2, 1, 40);
        wineScanIntervalTicks = builder.comment("Ticks between wine rescans.").defineInRange("scanIntervalTicks", 10, 1, 40);

        quickEnableWine = builder.comment("GLFW key code for quick-toggle wine (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();

        // ── CHEESE ─────────────────────────────────────────────────────
        builder.comment("Easy Cheese settings (society:cheese_press).").push("cheese");

        cheeseScanRadius = builder.comment("Block scan radius for cheese presses.").defineInRange("scanRadius", 5, 1, 10);
        cheeseCooldownTicks = builder.comment("Ticks between cheese harvest/insert cycles.").defineInRange("cooldownTicks", 2, 1, 40);
        cheeseScanIntervalTicks = builder.comment("Ticks between cheese rescans.").defineInRange("scanIntervalTicks", 10, 1, 40);

        quickEnableCheese = builder.comment("GLFW key code for quick-toggle cheese (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();

        // ── HUSBANDRY ──────────────────────────────────────────────────
        builder.comment("Easy Husbandry settings.").push("husbandry");

        husbandryScanRadius = builder.comment("Scan radius for farm animals.").defineInRange("scanRadius", 5.0, 1.0, 15.0);
        husbandryCooldownTicks = builder.comment("Ticks between right-clicking animals.").defineInRange("cooldownTicks", 1, 1, 40);
        husbandryScanIntervalTicks = builder.comment("Ticks between animal rescans.").defineInRange("scanIntervalTicks", 5, 1, 40);
        husbandryBatchSize = builder.comment("Animals to interact with per cycle.").defineInRange("batchSize", 4, 1, 20);

        quickEnableHusbandry = builder.comment("GLFW key code for quick-toggle husbandry (0 = disabled).")
                .defineInRange("quickEnableKey", 0, 0, 512);

        builder.pop();
    }
}
