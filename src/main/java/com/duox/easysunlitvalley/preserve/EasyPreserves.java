package com.duox.easysunlitvalley.preserve;

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
 * Easy Preserves module logic. 
 * Automatically harvests done preserves, or inputs held edible items into empty preserves in batches.
 */
public final class EasyPreserves {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<PreservesTarget> targets = new ArrayList<>();

    public int getDoneCount() {
        return (int) targets.stream().filter(PreservesTarget::isDone).count();
    }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            BlockPos playerPos = mc.player.blockPosition();
            targets = new ArrayList<>(PreservesScanner.scan().stream()
                    .sorted(Comparator.comparingDouble(t -> t.pos().distSqr(playerPos)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.preservesScanIntervalTicks.get();
        } else {
            scanCooldown--;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (targets.isEmpty()) return;

        int batchSize = 8;
        int processed = 0;

        // 1. Harvest ready preserves jars
        List<PreservesTarget> toRemove = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) {
            if (processed >= batchSize) break;
            PreservesTarget target = targets.get(i);
            if (target.isDone()) {
                if (mc.level.getBlockState(target.pos()).isAir()) {
                    toRemove.add(target);
                    continue;
                }

                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(target.pos()), Direction.UP, target.pos(), false));

                toRemove.add(target);
                processed++;
            }
        }
        targets.removeAll(toRemove);

        // 2. Put edible items held by player into empty preserves jars
        if (processed < batchSize) {
            ItemStack held = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty() && held.getItem().isEdible()) {
                toRemove.clear();
                for (int i = 0; i < targets.size(); i++) {
                    if (processed >= batchSize) break;
                    PreservesTarget target = targets.get(i);
                    if (target.isEmpty()) {
                        if (mc.level.getBlockState(target.pos()).isAir()) {
                            toRemove.add(target);
                            continue;
                        }

                        mc.player.swing(InteractionHand.MAIN_HAND);
                        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                                new BlockHitResult(Vec3.atCenterOf(target.pos()), Direction.UP, target.pos(), false));

                        toRemove.add(target);
                        processed++;
                    }
                }
                targets.removeAll(toRemove);
            }
        }

        if (processed > 0) {
            cooldown = ESVConfig.INSTANCE.preservesCooldownTicks.get();
            scanCooldown = 0;
        }
    }

    public void reset() {
        cooldown = 0;
        scanCooldown = 0;
        targets = new ArrayList<>();
    }
}
