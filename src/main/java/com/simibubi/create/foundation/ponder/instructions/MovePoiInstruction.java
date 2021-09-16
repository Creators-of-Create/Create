package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderInstruction;
import com.simibubi.create.foundation.ponder.PonderScene;

import net.minecraft.world.phys.Vec3;

public class MovePoiInstruction extends PonderInstruction {

	private Vec3 poi;

	public MovePoiInstruction(Vec3 poi) {
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
