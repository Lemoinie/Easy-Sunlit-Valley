package com.duox.easysunlitvalley.wine;

import net.minecraft.core.BlockPos;

/** Immutable snapshot of a detected wine keg block. */
public record WineTarget(BlockPos pos, boolean isDone, boolean isEmpty) {}
