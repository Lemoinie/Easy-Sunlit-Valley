package com.duox.easysunlitvalley.cheese;

import net.minecraft.core.BlockPos;

/** Immutable snapshot of a detected cheese press block. */
public record CheeseTarget(BlockPos pos, boolean isDone, boolean isEmpty) {}
