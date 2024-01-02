package com.simibubi.create.content.contraptions.piston;

import com.jozufozu.flywheel.lib.box.MutableBox;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;

import net.minecraft.core.Vec3i;

public class PistonLighter extends ContraptionLighter<PistonContraption> {
    public PistonLighter(PistonContraption contraption) {
        super(contraption);
    }

    @Override
    public MutableBox getContraptionBounds() {
        MutableBox bounds = MutableBox.from(contraption.bounds);
        bounds.translate(contraption.anchor);

        int length = contraption.extensionLength;
        Vec3i direction = contraption.orientation.getNormal();

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
