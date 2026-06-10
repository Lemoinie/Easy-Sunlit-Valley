package com.duox.easysunlitvalley.wine;

import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Easy Wine module logic. 
 * Automatically harvests done wine, or inputs held edible items into empty wine kegs.
 */
public final class EasyWine {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<WineTarget> targets = new ArrayList<>();

    public int getDoneCount() {
        return (int) targets.stream().filter(WineTarget::isDone).count();
    }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            BlockPos playerPos = mc.player.blockPosition();
            targets = new ArrayList<>(WineScanner.scan().stream()
                    .sorted(Comparator.comparingDouble(t -> t.pos().distSqr(playerPos)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.wineScanIntervalTicks.get();
        } else {
            scanCooldown--;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (targets.isEmpty()) return;

        // 1. Harvest ready wine kegs
        for (int i = 0; i < targets.size(); i++) {
            WineTarget target = targets.get(i);
            if (target.isDone()) {
                if (mc.level.getBlockState(target.pos()).isAir()) continue;

                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(target.pos()), Direction.UP, target.pos(), false));

                cooldown = ESVConfig.INSTANCE.wineCooldownTicks.get();
                scanCooldown = 0;
                return;
            }
        }

        // 2. Put edible items held by player into empty wine kegs
        ItemStack held = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!held.isEmpty() && held.getItem().isEdible()) {
            for (int i = 0; i < targets.size(); i++) {
                WineTarget target = targets.get(i);
                if (target.isEmpty()) {
                    if (mc.level.getBlockState(target.pos()).isAir()) continue;

                    mc.player.swing(InteractionHand.MAIN_HAND);
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(target.pos()), Direction.UP, target.pos(), false));

                    cooldown = ESVConfig.INSTANCE.wineCooldownTicks.get();
                    scanCooldown = 0;
                    return;
                }
            }
        }
    }

    public void reset() {
        cooldown = 0;
        scanCooldown = 0;
        targets = new ArrayList<>();
    }
}
