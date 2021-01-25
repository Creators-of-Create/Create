package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.render.contraption.RenderedContraption;
import com.simibubi.create.foundation.render.light.ContraptionLighter;
import com.simibubi.create.foundation.render.light.GridAlignedBB;

public class NonStationaryLighter<C extends Contraption> extends ContraptionLighter<C> {
    public NonStationaryLighter(C contraption) {
        super(contraption);
    }

    @Override
    protected GridAlignedBB contraptionBoundsToVolume(GridAlignedBB bounds) {
        bounds = bounds.copy();
        bounds.grow(2); // so we have at least enough data on the edges to avoid artifacts and have smooth lighting
        bounds.minY = Math.max(bounds.minY, 0);
        bounds.maxY = Math.min(bounds.maxY, 255);

        return bounds;
    }

    @Override
    public void tick(RenderedContraption owner) {
        super.tick(owner);
        GridAlignedBB contraptionBounds = getContraptionBounds();

        if (!contraptionBounds.sameAs(bounds)) {
            lightVolume.move(contraption.entity.world, contraptionBoundsToVolume(contraptionBounds));
            bounds = contraptionBounds;
        }
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.fromAABB(contraption.bounds);

        bb.translate(contraption.entity.getPosition());

        return bb;
    }
}
