package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.foundation.render.backend.light.GridAlignedBB;

import net.minecraft.util.math.Vec3i;

public class PistonLighter extends ContraptionLighter<PistonContraption> {
    public PistonLighter(PistonContraption contraption) {
        super(contraption);
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bounds = GridAlignedBB.from(contraption.bounds);
        bounds.translate(contraption.anchor);

        int length = contraption.extensionLength;
        Vec3i direction = contraption.orientation.getDirectionVec();

        int shift = length / 2;
        int shiftX = direction.getX() * shift;
        int shiftY = direction.getY() * shift;
        int shiftZ = direction.getZ() * shift;
        bounds.translate(shiftX, shiftY, shiftZ);

        int grow = (length + 1) / 2;
        int extendX = Math.abs(direction.getX() * grow);
        int extendY = Math.abs(direction.getY() * grow);
        int extendZ = Math.abs(direction.getZ() * grow);
        bounds.grow(extendX, extendY, extendZ);

        return bounds;
    }
}
