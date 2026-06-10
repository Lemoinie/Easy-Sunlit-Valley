package com.duox.easysunlitvalley.tapper;

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
import java.util.List;

/**
 * Scans for {@code society:tapper} blocks and determines if they are full.
 * Detection uses block-state properties (age, boolean flags, or generic integer).
 */
public final class TapperScanner {

    /** The exact block ID for tappers in the Society modpack. */
    private static final String TAPPER_NAMESPACE = "society";
    private static final String TAPPER_PATH = "tapper";

    private TapperScanner() {}

    public static List<TapperTarget> scan() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return List.of();

        ClientLevel level = mc.level;
        BlockPos center = mc.player.blockPosition();
        int r = ESVConfig.INSTANCE.tapperScanRadius.get();
        List<TapperTarget> targets = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (id == null) continue;

            // Match society:tapper exactly
            if (!TAPPER_NAMESPACE.equals(id.getNamespace()) || !TAPPER_PATH.equals(id.getPath())) continue;

            // Log properties of found tapper block for debugging purposes
            com.duox.easysunlitvalley.EasySunlitValleyMod.LOGGER.info("ESV Debug - Found tapper at {}. BlockState: {}, Properties: {}", 
                    pos, state, state.getProperties());

            boolean isFull = evaluateFullness(state);
            targets.add(new TapperTarget(pos.immutable(), isFull));
        }
        return targets;
    }

    /**
     * Determines if a tapper is full using multiple strategies:
     * 1. age property at max
     * 2. boolean property containing "full"/"filled"/"ready"/"output"/"done"
     * 3. any integer property at max (fallback)
     */
    private static boolean evaluateFullness(BlockState state) {
        // Strategy 1: age property
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty intProp && "age".equals(prop.getName())) {
                int current = state.getValue(intProp);
                int max = intProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                return current >= max;
            }
        }

        // Strategy 2: boolean fullness flags
        for (Property<?> prop : state.getProperties()) {
            String name = prop.getName().toLowerCase();
            if (name.contains("full") || name.contains("filled") || name.contains("ready") || name.contains("output") || name.contains("done")) {
                if (prop.getValueClass() == Boolean.class) {
                    @SuppressWarnings("unchecked")
                    Property<Boolean> boolProp = (Property<Boolean>) prop;
                    return state.getValue(boolProp);
                }
            }
        }

        // Strategy 3: any integer property at max
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty intProp) {
                int current = state.getValue(intProp);
                int max = intProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                if (max > 0 && current >= max) return true;
            }
        }

        return false;
    }
}
