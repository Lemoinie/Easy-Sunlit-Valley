package com.duox.easysunlitvalley.harvest;

import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Right-click harvester for mature crops/fruits. Never left-clicks. */
public final class EasyHarvester {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<HarvestTarget> matureTargets = new ArrayList<>();

    public int getMatureCount() { return matureTargets.size(); }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            BlockPos playerPos = mc.player.blockPosition();
            matureTargets = new ArrayList<>(BlockScanner.scan().stream()
                    .filter(HarvestTarget::isMature)
                    .sorted(Comparator.comparingDouble(t -> t.pos().distSqr(playerPos)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.harvestScanIntervalTicks.get();
        } else { scanCooldown--; }

        if (cooldown > 0) { cooldown--; return; }
        if (matureTargets.isEmpty()) return;

        int batchSize = ESVConfig.INSTANCE.harvestBatchSize.get();
        int harvested = 0;
        while (harvested < batchSize && !matureTargets.isEmpty()) {
            HarvestTarget target = matureTargets.remove(0);
            if (mc.level.getBlockState(target.pos()).isAir()) continue;
            rightClickBlock(mc, target.pos());
            harvested++;
        }
        if (harvested > 0) { cooldown = ESVConfig.INSTANCE.harvestCooldownTicks.get(); scanCooldown = 0; }
    }

    private void rightClickBlock(Minecraft mc, BlockPos pos) {
        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    public void reset() { cooldown = 0; scanCooldown = 0; matureTargets = new ArrayList<>(); }
}
