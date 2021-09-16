package com.simibubi.create.foundation.utility.worldWrappers.chunk;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class WrappedChunkSection extends LevelChunkSection {

    public WrappedChunk owner;

    public final int xStart;
    public final int yStart;
    public final int zStart;

    public WrappedChunkSection(WrappedChunk owner, int yBase) {
        super(yBase);
        this.owner = owner;
        this.xStart = owner.pos.getMinBlockX();
        this.yStart = yBase;
        this.zStart = owner.pos.getMinBlockZ();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        // ChunkSection#getBlockState expects local chunk coordinates, so we add to get back into world coords.
        return owner.world.getBlockState(x + xStart, y + yStart, z + zStart);
    }

    @Override
    public BlockState setBlockState(int p_177484_1_, int p_177484_2_, int p_177484_3_, BlockState p_177484_4_, boolean p_177484_5_) {
        throw new IllegalStateException("Chunk sections should not be mutated in a fake world.");
    }
}
