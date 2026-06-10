package com.duox.easysunlitvalley.harvest;

import net.minecraft.core.BlockPos;

/** Immutable snapshot of a detected harvestable block. */
public record HarvestTarget(BlockPos pos, CropType type, boolean isMature) {}
