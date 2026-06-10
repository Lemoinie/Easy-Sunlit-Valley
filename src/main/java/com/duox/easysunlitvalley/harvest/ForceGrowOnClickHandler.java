package com.duox.easysunlitvalley.harvest;

import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Force-grows a crop to max age when the player right-clicks it (server-side).
 * Activated when forceGrowOnClick config is true AND forceGrowEnabled is toggled on.
 */
public final class ForceGrowOnClickHandler {

    private static final String NS_FARMERS_DELIGHT = "farmersdelight";
    private static final String NS_FARM_AND_CHARM  = "farm_and_charm";
    private static final String NS_PAM_TREES       = "pamhc2trees";
    private static final String NS_VINERY          = "vinery";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ModuleManager.forceGrowEnabled) return;
        if (!ESVConfig.INSTANCE.forceGrowOnClick.get()) return;
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!isSupportedBlock(state)) return;

        // Try named "age" property first
        IntegerProperty ageProp = BlockScanner.findAgeProperty(state);
        if (ageProp != null) {
            int current = state.getValue(ageProp);
            int max = ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (current < max) level.setBlock(pos, state.setValue(ageProp, max), 3);
            return;
        }
        // Fallback: any integer property that looks like a growth stage
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty intProp) {
                int current = state.getValue(intProp);
                int max = intProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                if (max > 1 && current < max) {
                    level.setBlock(pos, state.setValue(intProp, max), 3);
                    return;
                }
            }
        }
    }

    private boolean isSupportedBlock(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null) return false;
        String ns = id.getNamespace();
        String path = id.getPath();

        if ("minecraft".equals(ns)) {
            return !path.contains("sapling") && !path.contains("stem")
                    && !"fire".equals(path) && !path.contains("pitcher");
        }
        if (NS_FARMERS_DELIGHT.equals(ns) && ESVConfig.INSTANCE.harvestFarmersDelight.get()) return true;
        if (NS_FARM_AND_CHARM.equals(ns)  && ESVConfig.INSTANCE.harvestFarmAndCharm.get())  return true;
        if (NS_PAM_TREES.equals(ns)       && ESVConfig.INSTANCE.harvestPamTrees.get()) {
            return !path.endsWith("_sapling") && !path.contains("log");
        }
        if (NS_VINERY.equals(ns)          && ESVConfig.INSTANCE.harvestVinery.get())          return true;
        if ("etcetera".equals(ns)         && ESVConfig.INSTANCE.harvestEtcetera.get())        return true;
        if ("vintagedelight".equals(ns)   && ESVConfig.INSTANCE.harvestVintageDelight.get()) return true;
        if ("veggiesdelight".equals(ns)   && ESVConfig.INSTANCE.harvestVeggiesDelight.get()) return true;
        return false;
    }
}
