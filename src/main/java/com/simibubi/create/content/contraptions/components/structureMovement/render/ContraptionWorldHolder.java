package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

public class ContraptionWorldHolder {
	public final Contraption contraption;
	public final PlacementSimulationWorld renderWorld;

	public ContraptionWorldHolder(Contraption contraption, PlacementSimulationWorld renderWorld) {
		this.contraption = contraption;
		this.renderWorld = renderWorld;
	}

    public int getEntityId() {
        return contraption.entity.getEntityId();
    }

    public boolean isDead() {
        return !contraption.entity.isAlive();
    }
}
