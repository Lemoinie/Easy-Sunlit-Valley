package com.duox.easysunlitvalley.tapper;

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

/** Right-clicks full tappers (society:tapper). Never left-clicks. */
public final class AutoTapper {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<TapperTarget> fullTappers = new ArrayList<>();

    public int getFullCount() { return fullTappers.size(); }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            BlockPos playerPos = mc.player.blockPosition();
            fullTappers = new ArrayList<>(TapperScanner.scan().stream()
                    .filter(TapperTarget::isFull)
                    .sorted(Comparator.comparingDouble(t -> t.pos().distSqr(playerPos)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.tapperScanIntervalTicks.get();
        } else { scanCooldown--; }

        if (cooldown > 0) { cooldown--; return; }
        if (fullTappers.isEmpty()) return;

        TapperTarget target = fullTappers.remove(0);
        if (mc.level.getBlockState(target.pos()).isAir()) return;

        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(target.pos()), Direction.UP, target.pos(), false));

        cooldown = ESVConfig.INSTANCE.tapperCooldownTicks.get();
        scanCooldown = 0;
    }

    public void reset() { cooldown = 0; scanCooldown = 0; fullTappers = new ArrayList<>(); }
}
