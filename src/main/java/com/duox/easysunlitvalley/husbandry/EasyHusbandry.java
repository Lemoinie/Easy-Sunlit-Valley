package com.duox.easysunlitvalley.husbandry;

import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Easy Husbandry module logic.
 * Automatically right-clicks nearby farm animals (cows, sheep, chickens, pigs) in batches.
 */
public final class EasyHusbandry {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<Entity> targets = new ArrayList<>();

    public int getAnimalCount() { return targets.size(); }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            double radius = ESVConfig.INSTANCE.husbandryScanRadius.get();
            targets = new ArrayList<>(mc.level.getEntities((Entity) null,
                    mc.player.getBoundingBox().inflate(radius),
                    entity -> entity instanceof Cow || entity instanceof Sheep || entity instanceof Chicken || entity instanceof Pig)
                    .stream()
                    .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.husbandryScanIntervalTicks.get();
        } else { scanCooldown--; }

        if (cooldown > 0) { cooldown--; return; }
        if (targets.isEmpty()) return;

        int batchSize = ESVConfig.INSTANCE.husbandryBatchSize.get();
        int processed = 0;
        while (processed < batchSize && !targets.isEmpty()) {
            Entity target = targets.remove(0);
            if (!target.isAlive()) continue;
            interactWithEntity(mc, target);
            processed++;
        }
        if (processed > 0) {
            cooldown = ESVConfig.INSTANCE.husbandryCooldownTicks.get();
            scanCooldown = 0;
        }
    }

    private void interactWithEntity(Minecraft mc, Entity entity) {
        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
    }

    public void reset() { cooldown = 0; scanCooldown = 0; targets = new ArrayList<>(); }
}
