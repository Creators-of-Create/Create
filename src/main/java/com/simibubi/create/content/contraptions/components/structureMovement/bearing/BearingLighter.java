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

        float maxDistanceSq = -1;
        for (BlockPos pos : blocks) {
            float x = pos.getX();
            float y = pos.getY();
            float z = pos.getZ();

            float distSq = x * x + y * y + z * z;

            if (distSq > maxDistanceSq) maxDistanceSq = distSq;
        }

        int radius = (int) (Math.ceil(Math.sqrt(maxDistanceSq)));

        GridAlignedBB betterBounds = GridAlignedBB.ofRadius(radius);
        GridAlignedBB contraptionBounds = GridAlignedBB.fromAABB(contraption.bounds);

        Direction.Axis axis = orientation.getAxis();

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
}
