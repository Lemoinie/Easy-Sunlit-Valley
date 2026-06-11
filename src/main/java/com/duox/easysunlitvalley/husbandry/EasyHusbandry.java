package com.duox.easysunlitvalley.husbandry;

import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Easy Husbandry module logic.
 * Automatically right-clicks nearby farm animals (cows, sheep, chickens, pigs, goats) in batches.
 * Avoids spamming by tracking petted animals per Minecraft day, and only shearing/milking/feeding when appropriate.
 */
public final class EasyHusbandry {

    private int cooldown = 0;
    private int scanCooldown = 0;
    private List<Entity> targets = new ArrayList<>();
    private final Map<UUID, Long> pettedAnimals = new HashMap<>();

    public int getAnimalCount() { return targets.size(); }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        if (scanCooldown <= 0) {
            double radius = ESVConfig.INSTANCE.husbandryScanRadius.get();
            targets = new ArrayList<>(mc.level.getEntities((Entity) null,
                    mc.player.getBoundingBox().inflate(radius),
                    entity -> entity instanceof Cow || entity instanceof Sheep || entity instanceof Chicken || entity instanceof Pig || entity instanceof Goat)
                    .stream()
                    .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)))
                    .toList());
            scanCooldown = ESVConfig.INSTANCE.husbandryScanIntervalTicks.get();
        } else { scanCooldown--; }

        if (cooldown > 0) { cooldown--; return; }
        if (targets.isEmpty()) return;

        int batchSize = ESVConfig.INSTANCE.husbandryBatchSize.get();
        int processed = 0;

        long currentDay = mc.level.getDayTime() / 24000;
        ItemStack held = mc.player.getItemInHand(InteractionHand.MAIN_HAND);

        List<Entity> toRemove = new ArrayList<>();

        for (Entity target : targets) {
            if (processed >= batchSize) break;
            if (!target.isAlive()) {
                toRemove.add(target);
                continue;
            }

            double radius = ESVConfig.INSTANCE.husbandryScanRadius.get();
            if (target.distanceToSqr(mc.player) > radius * radius) {
                toRemove.add(target);
                continue;
            }

            if (isValidInteraction(target, held, currentDay)) {
                interactWithEntity(mc, target, held, currentDay);
                toRemove.add(target);
                processed++;
            }
        }

        targets.removeAll(toRemove);

        if (processed > 0) {
            cooldown = ESVConfig.INSTANCE.husbandryCooldownTicks.get();
            scanCooldown = 0;
        }
    }

    private boolean isValidInteraction(Entity target, ItemStack held, long currentDay) {
        if (!(target instanceof Animal animal)) return false;

        // 1. Shearing Mode (Holding Shears)
        if (held.getItem() instanceof ShearsItem) {
            return animal instanceof Sheep sheep && !sheep.isSheared() && !sheep.isBaby();
        }

        // 2. Milking Mode (Holding Bucket)
        if (held.is(Items.BUCKET)) {
            return (animal instanceof Cow || animal instanceof Goat || animal instanceof Sheep) && !animal.isBaby();
        }

        // 3. Feeding / Breeding Mode (Holding Food)
        if (animal.isFood(held)) {
            if (animal.isBaby()) return true;
            return !animal.isInLove() && animal.getAge() == 0;
        }

        // 4. Petting Mode (Empty hand or anything else)
        long lastPettedDay = pettedAnimals.getOrDefault(animal.getUUID(), -1L);
        return lastPettedDay != currentDay;
    }

    private void interactWithEntity(Minecraft mc, Entity entity, ItemStack held, long currentDay) {
        // Log all synched data entries for debugging modpack-specific petting/affection parameters
        if (entity.getEntityData() != null) {
            try {
                List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> items = entity.getEntityData().getNonDefaultValues();
                if (items != null) {
                    for (var item : items) {
                        com.duox.easysunlitvalley.EasySunlitValleyMod.LOGGER.info("ESV Debug - Animal {} DataValue: ID={}, Value={}, Serializer={}", 
                            entity.getName().getString(),
                            item.id(),
                            item.value(),
                            item.serializer() != null ? item.serializer().getClass().getSimpleName() : "null");
                    }
                }
            } catch (Exception e) {
                com.duox.easysunlitvalley.EasySunlitValleyMod.LOGGER.error("ESV Debug - Error logging entity data: ", e);
            }
        }

        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);

        // Record petted status only if it was a petting action
        boolean isShearing = held.getItem() instanceof ShearsItem;
        boolean isMilking = held.is(Items.BUCKET);
        boolean isFeeding = (entity instanceof Animal animal) && animal.isFood(held);

        if (!isShearing && !isMilking && !isFeeding) {
            pettedAnimals.put(entity.getUUID(), currentDay);
        }
    }

    public void reset() {
        cooldown = 0;
        scanCooldown = 0;
        targets = new ArrayList<>();
        pettedAnimals.clear();
    }
}
