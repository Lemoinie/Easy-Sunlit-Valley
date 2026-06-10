package com.duox.easysunlitvalley.preserve;

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
 * Scans for preserves jars (society:preserves_jar, society:preserves_jar_upgraded) 
 * and determines if they are done or empty.
 */
public final class PreservesScanner {

    private static final String NAMESPACE = "society";
    private static final String PATH_NORMAL = "preserves_jar";
    private static final String PATH_UPGRADED = "preserves_jar_upgraded";

    private PreservesScanner() {}

    public static List<PreservesTarget> scan() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return List.of();

        ClientLevel level = mc.level;
        BlockPos center = mc.player.blockPosition();
        int r = ESVConfig.INSTANCE.preservesScanRadius.get();
        List<PreservesTarget> targets = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (id == null) continue;

            if (!NAMESPACE.equals(id.getNamespace())) continue;
            String path = id.getPath();
            if (!PATH_NORMAL.equals(path) && !PATH_UPGRADED.equals(path)) continue;

            boolean isDone = evaluateDone(state);
            boolean isEmpty = !isDone && !evaluateWorking(state);

            targets.add(new PreservesTarget(pos.immutable(), isDone, isEmpty));
        }
        return targets;
    }

    private static boolean evaluateDone(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty intProp && ("age".equals(prop.getName()) || "stage".equals(prop.getName()))) {
                int current = state.getValue(intProp);
                int max = intProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                return current >= max;
            }
        }

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
        return false;
    }

    private static boolean evaluateWorking(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            String name = prop.getName().toLowerCase();
            if (name.contains("working") || name.contains("active") || name.contains("lit") || name.contains("processing")) {
                if (prop.getValueClass() == Boolean.class) {
                    @SuppressWarnings("unchecked")
                    Property<Boolean> boolProp = (Property<Boolean>) prop;
                    return state.getValue(boolProp);
                }
            }
        }
        return false;
    }
}
