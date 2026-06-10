package com.duox.easysunlitvalley.tapper;

import net.minecraft.core.BlockPos;

/** Immutable snapshot of a detected tapper block. */
public record TapperTarget(BlockPos pos, boolean isFull) {}
