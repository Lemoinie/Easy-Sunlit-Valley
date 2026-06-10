package com.duox.easysunlitvalley.harvest;

import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

/** Forces nearby crops to max age (server-side, single-player). */
public final class GrowthForcer {

    private static final String NS_FARMERS_DELIGHT = "farmersdelight";
    private static final String NS_FARM_AND_CHARM  = "farm_and_charm";
    private static final String NS_PAM_TREES       = "pamhc2trees";
    private static final String NS_VINERY          = "vinery";

    private int cooldown = 0;

    public void tick(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return;
        if (cooldown > 0) { cooldown--; return; }
        cooldown = ESVConfig.INSTANCE.harvestScanIntervalTicks.get();

        int r = ESVConfig.INSTANCE.harvestScanRadius.get();
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (isSupportedBlock(state)) forceGrow(level, pos, state);
        }
    }

    private boolean isSupportedBlock(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null) return false;
        String ns = id.getNamespace(); String path = id.getPath();
        if (NS_FARMERS_DELIGHT.equals(ns) && ESVConfig.INSTANCE.harvestFarmersDelight.get()) return true;
        if (NS_FARM_AND_CHARM.equals(ns) && ESVConfig.INSTANCE.harvestFarmAndCharm.get()) return true;
        if (NS_PAM_TREES.equals(ns) && ESVConfig.INSTANCE.harvestPamTrees.get()) {
            return !path.endsWith("_sapling") && !path.contains("log");
        }
        if (NS_VINERY.equals(ns) && ESVConfig.INSTANCE.harvestVinery.get()) return true;
        return false;
    }

    private void forceGrow(ServerLevel level, BlockPos pos, BlockState state) {
        IntegerProperty ageProp = BlockScanner.findAgeProperty(state);
        if (ageProp != null) {
            int current = state.getValue(ageProp);
            int max = ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (current < max) level.setBlock(pos, state.setValue(ageProp, max), 3);
            return;
        }
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty intProp) {
                int current = state.getValue(intProp);
                int max = intProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                if (max > 0 && current < max) { level.setBlock(pos, state.setValue(intProp, max), 3); return; }
            }
        }
    }

    public void reset() { cooldown = 0; }
}
