package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.content.contraptions.components.structureMovement.render.RenderedContraption;
import com.simibubi.create.foundation.render.backend.light.GridAlignedBB;

public class NonStationaryLighter<C extends Contraption> extends ContraptionLighter<C> {
    public NonStationaryLighter(C contraption) {
        super(contraption);
    }

    @Override
    public void tick(RenderedContraption owner) {
        super.tick(owner);
        GridAlignedBB contraptionBounds = getContraptionBounds();

        if (!contraptionBounds.sameAs(bounds)) {
            lightVolume.move(contraption.entity.world, contraptionBoundsToVolume(contraptionBounds));
            bounds = contraptionBounds;

            startListening();
        }
    }

    @Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.from(contraption.bounds);

        bb.translate(contraption.entity.getPosition());

        return bb;
    }
}
