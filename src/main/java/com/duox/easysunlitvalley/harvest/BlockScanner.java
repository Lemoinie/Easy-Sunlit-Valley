package com.duox.easysunlitvalley.harvest;

import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Scans nearby blocks for harvestable crops and fruits.
 * Tapper detection is handled separately by TapperScanner.
 */
public final class BlockScanner {

    private static final String NS_FARMERS_DELIGHT = "farmersdelight";
    private static final String NS_FARM_AND_CHARM  = "farm_and_charm";
    private static final String NS_PAM_TREES       = "pamhc2trees";
    private static final String NS_VINERY          = "vinery";

    private BlockScanner() {}

    public static List<HarvestTarget> scan() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return List.of();

        ClientLevel level = mc.level;
        BlockPos center = mc.player.blockPosition();
        int r = ESVConfig.INSTANCE.harvestScanRadius.get();
        List<HarvestTarget> targets = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            evaluate(state, pos).ifPresent(targets::add);
        }
        return targets;
    }

    private static Optional<HarvestTarget> evaluate(BlockState state, BlockPos pos) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null) return Optional.empty();

        String ns = id.getNamespace();
        String path = id.getPath();

        if (NS_FARMERS_DELIGHT.equals(ns) && ESVConfig.INSTANCE.harvestFarmersDelight.get())
            return evaluateAgeCrop(state, pos, CropType.CROP);
        if (NS_FARM_AND_CHARM.equals(ns) && ESVConfig.INSTANCE.harvestFarmAndCharm.get())
            return evaluateAgeCrop(state, pos, CropType.CROP);
        if (NS_PAM_TREES.equals(ns) && ESVConfig.INSTANCE.harvestPamTrees.get()) {
            if (path.endsWith("_sapling") || path.contains("log")) return Optional.empty();
            return evaluateAgeCrop(state, pos, CropType.FRUIT);
        }
        if (NS_VINERY.equals(ns) && ESVConfig.INSTANCE.harvestVinery.get())
            return evaluateAgeCrop(state, pos, CropType.FRUIT);
        if ("minecraft".equals(ns)) {
            if (path.contains("sapling") || path.contains("stem") || "fire".equals(path) || path.contains("pitcher")) return Optional.empty();
            return evaluateAgeCrop(state, pos, CropType.CROP);
        }

        return Optional.empty();
    }

    private static Optional<HarvestTarget> evaluateAgeCrop(BlockState state, BlockPos pos, CropType type) {
        IntegerProperty ageProp = findAgeProperty(state);
        if (ageProp == null) return Optional.empty();
        int current = state.getValue(ageProp);
        int max = ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
        return Optional.of(new HarvestTarget(pos.immutable(), type, current >= max));
    }

    static IntegerProperty findAgeProperty(BlockState state) {
        Collection<Property<?>> props = state.getProperties();
        for (Property<?> prop : props) {
            if (prop instanceof IntegerProperty intProp && "age".equals(prop.getName())) return intProp;
        }
        return null;
    }
}
