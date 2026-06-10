package com.duox.easysunlitvalley.fishing;

import com.bonker.stardewfishing.mixin.FishingHookAccessor;
import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Auto Fish Hack — automatically casts and retrieves the fishing rod.
 * Migrated from EasyFishing into the unified EasySunlitValley mod.
 *
 * <p>Handles the cast/reel loop. The fishing minigame bar control is
 * handled by the mixins (FishingAutoMixin, FishingMinigameMixin).
 */
@OnlyIn(Dist.CLIENT)
public class AutoFishHack {

    /** Session counter for successfully caught fish. */
    public static int caughtCount = 0;

    private int castTimer = 0;
    private int reelTimer = 0;
    private volatile boolean biteDetected = false;
    private boolean isReeling = false;

    public int getReelTimer() { return this.reelTimer; }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!ModuleManager.fishingEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        InteractionHand rodHand = getRodHand(mc.player);
        if (rodHand == null) return;

        // --- Durability Protection & Auto-Switch ---
        if (ESVConfig.INSTANCE.durabilityProtection.get()) {
            ItemStack stack = mc.player.getItemInHand(rodHand);
            int durability = getDurability(stack);
            int threshold = ESVConfig.INSTANCE.durabilityThreshold.get();
            if (durability <= threshold) {
                if (ESVConfig.INSTANCE.autoSwitchRod.get()) {
                    int altSlot = findAlternativeRod(mc.player, mc.player.getInventory().selected, threshold);
                    if (altSlot != -1) {
                        mc.player.getInventory().selected = altSlot;
                        mc.player.displayClientMessage(
                            Component.literal("§aAuto Fish: Switched to healthy rod in slot " + (altSlot + 1)), true);
                        rodHand = getRodHand(mc.player);
                        if (rodHand == null) return;
                    } else {
                        ModuleManager.fishingEnabled = false;
                        reset();
                        mc.player.displayClientMessage(
                            Component.literal("§cAuto Fish Stopped: No healthy rods left!"), false);
                        return;
                    }
                } else {
                    ModuleManager.fishingEnabled = false;
                    reset();
                    mc.player.displayClientMessage(
                        Component.literal("§cAuto Fish Stopped: Rod durability is low (" + durability + ")!"), false);
                    return;
                }
            }
        }

        if (castTimer > 0) { castTimer--; return; }
        if (reelTimer > 0) { reelTimer--; }

        FishingHook hook = mc.player.fishing;

        if (hook == null || hook.isRemoved()) {
            mc.gameMode.useItem(mc.player, rodHand);
            castTimer = getRandomizedRetryDelay(mc);
            reelTimer = ESVConfig.INSTANCE.patience.get() * 20;
            biteDetected = false;
            isReeling = false;
            return;
        }

        if (isReeling) return;

        if (biteDetected) {
            reelTimer = getRandomizedCatchDelay(mc);
            biteDetected = false;
        }

        if (ESVConfig.INSTANCE.biteMode.get() == ESVConfig.BiteMode.NIBBLE) {
            checkNibble(hook, mc);
        }

        if (reelTimer <= 0) {
            mc.gameMode.useItem(mc.player, rodHand);
            castTimer = getRandomizedRetryDelay(mc);
            reelTimer = ESVConfig.INSTANCE.patience.get() * 20;
            isReeling = true;
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!ModuleManager.fishingEnabled) return;
        if (ESVConfig.INSTANCE.biteMode.get() != ESVConfig.BiteMode.SOUND) return;

        var soundInstance = event.getSound();
        if (soundInstance == null) return;

        ResourceLocation soundId = soundInstance.getLocation();
        boolean isBite = soundId.equals(SoundEvents.FISHING_BOBBER_SPLASH.getLocation())
                || (soundId.getNamespace().equals("stardew_fishing") && soundId.getPath().equals("fish_bite"));
        if (!isBite) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        FishingHook myHook = mc.player.fishing;
        if (myHook == null || myHook.isRemoved()) return;

        double range = 8.0;
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(
                soundInstance.getX() - range, soundInstance.getY() - range, soundInstance.getZ() - range,
                soundInstance.getX() + range, soundInstance.getY() + range, soundInstance.getZ() + range);
        java.util.List<FishingHook> hooks = mc.level.getEntitiesOfClass(FishingHook.class, searchBox);

        FishingHook closestHook = null;
        double minDistanceSq = Double.MAX_VALUE;
        for (FishingHook h : hooks) {
            double dx = h.getX() - soundInstance.getX();
            double dy = h.getY() - soundInstance.getY();
            double dz = h.getZ() - soundInstance.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minDistanceSq) { minDistanceSq = distSq; closestHook = h; }
        }

        if (closestHook == myHook && minDistanceSq <= 16.0) {
            biteDetected = true;
        }
    }

    private void checkNibble(FishingHook hook, Minecraft mc) {
        try {
            FishingHookAccessor accessor = (FishingHookAccessor) hook;
            if (accessor.getNibble() > 0) {
                int catchDelay = ESVConfig.INSTANCE.catchDelay.get();
                if (reelTimer > catchDelay) { reelTimer = getRandomizedCatchDelay(mc); }
            }
        } catch (ClassCastException e) { /* Mixin not applied — fallback to timeout */ }
    }

    public void reset() { castTimer = 0; reelTimer = 0; biteDetected = false; isReeling = false; }

    public InteractionHand getRodHand(Player player) {
        if (isRod(player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        if (isRod(player.getOffhandItem())) return InteractionHand.OFF_HAND;
        return null;
    }

    private boolean isRod(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.FISHING_ROD)) return true;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        String ns = id.getNamespace(); String path = id.getPath();
        return ("aquaculture".equals(ns) && path.contains("fishing_rod"))
                || ("netherdepthsupgrade".equals(ns) && path.contains("fishing_rod"));
    }

    private int getDurability(ItemStack stack) {
        return !stack.isDamageableItem() ? Integer.MAX_VALUE : stack.getMaxDamage() - stack.getDamageValue();
    }

    private int findAlternativeRod(Player player, int currentSlot, int threshold) {
        for (int i = 0; i < 9; i++) {
            if (i == currentSlot) continue;
            ItemStack stack = player.getInventory().getItem(i);
            if (isRod(stack) && getDurability(stack) > threshold) return i;
        }
        return -1;
    }

    private int getRandomizedRetryDelay(Minecraft mc) {
        int delay = ESVConfig.INSTANCE.retryDelay.get();
        if (ESVConfig.INSTANCE.antiDetection.get()) {
            int range = ESVConfig.INSTANCE.randomDelayRange.get();
            if (range > 0) delay += mc.level.random.nextInt(range * 2 + 1) - range;
        }
        return Math.max(0, delay);
    }

    private int getRandomizedCatchDelay(Minecraft mc) {
        int delay = ESVConfig.INSTANCE.catchDelay.get();
        if (ESVConfig.INSTANCE.antiDetection.get()) {
            int range = ESVConfig.INSTANCE.randomDelayRange.get();
            if (range > 0) delay += mc.level.random.nextInt(range * 2 + 1) - range;
        }
        return Math.max(0, delay);
    }
}
