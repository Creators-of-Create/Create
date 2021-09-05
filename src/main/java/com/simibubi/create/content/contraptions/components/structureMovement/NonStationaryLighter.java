package com.simibubi.create.content.contraptions.components.structureMovement;

import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.IMovingListener;
import com.jozufozu.flywheel.light.LightProvider;
import com.jozufozu.flywheel.light.ReadOnlyBox;
import com.simibubi.create.foundation.config.AllConfigs;

public class NonStationaryLighter<C extends Contraption> extends ContraptionLighter<C> implements IMovingListener {
    public NonStationaryLighter(C contraption) {
        super(contraption);
    }

	@Override
	public boolean update(LightProvider provider) {
		if (getVolume().volume() > AllConfigs.CLIENT.maxContraptionLightVolume.get())
			return false;

		ReadOnlyBox contraptionBounds = getContraptionBounds();

		if (bounds.sameAs(contraptionBounds)) {
			return false;
		}

		bounds.assign(contraptionBounds);
		lightVolume.move(contraption.entity.level, contraptionBoundsToVolume(bounds));

		return true;
	}

	@Override
    public GridAlignedBB getContraptionBounds() {
        GridAlignedBB bb = GridAlignedBB.from(contraption.bounds);

        bb.translate(contraption.entity.blockPosition());

        return bb;
    }
}
