package com.duox.easysunlitvalley.harvest;

import com.duox.easysunlitvalley.ModuleManager;
import com.duox.easysunlitvalley.config.ESVConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Forces nearby crops to max age via direct block-state mutation (server-side).
 *
 * <p>Crops store their age as a BlockState IntegerProperty, NOT as block-entity NBT.
 * That's why {@code /data merge block X Y Z {age:7}} fails with "not a block entity" —
 * and why simulating a right-click on the client side also doesn't work for force-grow.
 *
 * <p>The only correct approach is {@code level.setBlock(pos, state.setValue(ageProp, max), 3)}
 * executed on the server level, which is what this class does.
 */
public final class GrowthForcer {

    private static final String NS_MINECRAFT       = "minecraft";
    private static final String NS_FARMERS_DELIGHT = "farmersdelight";
    private static final String NS_FARM_AND_CHARM  = "farm_and_charm";
    private static final String NS_PAM_TREES       = "pamhc2trees";
    private static final String NS_VINERY          = "vinery";
    private static final String NS_ETCETERA        = "etcetera";
    private static final String NS_VINTAGE_DELIGHT = "vintagedelight";
    private static final String NS_VEGGIES_DELIGHT = "veggiesdelight";

    private int cooldown = 0;

    /**
     * Called from the server-side PlayerTickEvent. Scans around the player
     * and sets all nearby immature crops to their maximum age.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ModuleManager.forceGrowEnabled) return;
        if (event.player.level().isClientSide()) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        if (cooldown > 0) { cooldown--; return; }
        cooldown = ESVConfig.INSTANCE.harvestScanIntervalTicks.get();

        int r = ESVConfig.INSTANCE.harvestScanRadius.get();
        BlockPos center = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (isSupportedBlock(state)) forceGrow(level, pos, state);
        }
    }

    private void forceGrow(ServerLevel level, BlockPos pos, BlockState state) {
        // First try the named "age" property (most vanilla/modded crops use this)
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

        if (NS_MINECRAFT.equals(ns)) {
            // Exclude non-crop blocks: saplings, stems (melon/pumpkin), fire, pitcher plant base
            return !path.contains("sapling") && !path.contains("stem")
                    && !"fire".equals(path) && !path.contains("pitcher");
        }
        if (NS_FARMERS_DELIGHT.equals(ns) && ESVConfig.INSTANCE.harvestFarmersDelight.get()) return true;
        if (NS_FARM_AND_CHARM.equals(ns)  && ESVConfig.INSTANCE.harvestFarmAndCharm.get())  return true;
        if (NS_PAM_TREES.equals(ns)       && ESVConfig.INSTANCE.harvestPamTrees.get()) {
            return !path.endsWith("_sapling") && !path.contains("log");
        }
        if (NS_VINERY.equals(ns)          && ESVConfig.INSTANCE.harvestVinery.get())          return true;
        if (NS_ETCETERA.equals(ns)        && ESVConfig.INSTANCE.harvestEtcetera.get())        return true;
        if (NS_VINTAGE_DELIGHT.equals(ns) && ESVConfig.INSTANCE.harvestVintageDelight.get()) return true;
        if (NS_VEGGIES_DELIGHT.equals(ns) && ESVConfig.INSTANCE.harvestVeggiesDelight.get()) return true;
        return false;
    }

    public void reset() { cooldown = 0; }
}
