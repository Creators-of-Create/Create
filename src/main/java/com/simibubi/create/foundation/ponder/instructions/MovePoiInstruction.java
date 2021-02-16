package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;

import net.minecraft.util.math.Vec3d;

public class MovePoiInstruction extends PonderInstruction {

	private Vec3d poi;

	public MovePoiInstruction(Vec3d poi) {
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
