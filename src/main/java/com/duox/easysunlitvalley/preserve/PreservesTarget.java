package com.duox.easysunlitvalley.preserve;

import net.minecraft.core.BlockPos;

/** Immutable snapshot of a detected preserves jar block. */
public record PreservesTarget(BlockPos pos, boolean isDone, boolean isEmpty) {}
