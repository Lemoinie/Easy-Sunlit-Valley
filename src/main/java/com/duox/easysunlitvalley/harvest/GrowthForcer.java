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

/**
 * Ticks on the client side to scan for nearby immature crops,
 * and simulates right-clicking them to force growth.
 */
public final class GrowthForcer {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<BlockPos> immatureCrops = new ArrayList<>();

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            BlockPos playerPos = mc.player.blockPosition();
            immatureCrops = new ArrayList<>(BlockScanner.scan().stream()
                    .filter(t -> !t.isMature())
                    .map(HarvestTarget::pos)
                    .sorted(Comparator.comparingDouble(pos -> pos.distSqr(playerPos)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.harvestScanIntervalTicks.get();
        } else { scanCooldown--; }

        if (cooldown > 0) { cooldown--; return; }
        if (immatureCrops.isEmpty()) return;

        int batchSize = ESVConfig.INSTANCE.harvestBatchSize.get();
        int processed = 0;
        while (processed < batchSize && !immatureCrops.isEmpty()) {
            BlockPos pos = immatureCrops.remove(0);
            if (mc.level.getBlockState(pos).isAir()) continue;
            rightClickBlock(mc, pos);
            processed++;
        }
        if (processed > 0) {
            cooldown = ESVConfig.INSTANCE.harvestCooldownTicks.get();
            scanCooldown = 0;
        }
    }

    private void rightClickBlock(Minecraft mc, BlockPos pos) {
        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    public void reset() { cooldown = 0; scanCooldown = 0; immatureCrops = new ArrayList<>(); }
}
