package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;

import net.minecraft.util.math.vector.Vector3d;

public class MovePoiInstruction extends PonderInstruction {

	private Vector3d poi;

	public MovePoiInstruction(Vector3d poi) {
		this.poi = poi;
	}
	
	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void tick(PonderScene scene) {
		scene.setPointOfInterest(poi);
	}

}
