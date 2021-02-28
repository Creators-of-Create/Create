package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.Set;

import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.render.backend.light.GridAlignedBB;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BearingLighter extends ContraptionLighter<BearingContraption> {

    public BearingLighter(BearingContraption contraption) {
        super(contraption);
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        Set<BlockPos> blocks = contraption.getBlocks().keySet();

        Direction orientation = contraption.facing;
        Direction.Axis axis = orientation.getAxis();

        int radius = (int) (Math.ceil(Math.sqrt(getRadius(blocks, axis))));

        GridAlignedBB betterBounds = GridAlignedBB.ofRadius(radius);

        GridAlignedBB contraptionBounds = GridAlignedBB.fromAABB(contraption.bounds);
        if (axis == Direction.Axis.X) {
            betterBounds.maxX = contraptionBounds.maxX;
            betterBounds.minX = contraptionBounds.minX;
        } else if (axis == Direction.Axis.Y) {
            betterBounds.maxY = contraptionBounds.maxY;
            betterBounds.minY = contraptionBounds.minY;
        } else if (axis == Direction.Axis.Z) {
            betterBounds.maxZ = contraptionBounds.maxZ;
            betterBounds.minZ = contraptionBounds.minZ;
        }

        betterBounds.translate(contraption.anchor);
        return betterBounds;
    }

    private static float getRadius(Set<BlockPos> blocks, Direction.Axis axis) {
        switch (axis) {
        case X:
            return getMaxDistSqr(blocks, BlockPos::getY, BlockPos::getZ);
        case Y:
            return getMaxDistSqr(blocks, BlockPos::getX, BlockPos::getZ);
        case Z:
            return getMaxDistSqr(blocks, BlockPos::getX, BlockPos::getY);
        }

        throw new IllegalStateException("Impossible axis");
    }

    private static float getMaxDistSqr(Set<BlockPos> blocks, Coordinate one, Coordinate other) {
        float maxDistSq = -1;
        for (BlockPos pos : blocks) {
            float a = one.get(pos);
            float b = other.get(pos);

            float distSq = a * a + b * b;


            if (distSq > maxDistSq) maxDistSq = distSq;
        }

        return maxDistSq;
    }

    private interface Coordinate {
        float get(BlockPos from);
    }
}
