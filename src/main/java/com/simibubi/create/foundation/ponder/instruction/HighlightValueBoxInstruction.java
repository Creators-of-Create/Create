package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HighlightValueBoxInstruction extends TickingInstruction {

	private Vec3 vec;
	private Vec3 expands;

	public HighlightValueBoxInstruction(Vec3 vec, Vec3 expands, int duration) {
		super(false, duration);
		this.vec = vec;
		this.expands = expands;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		AABB point = new AABB(vec, vec);
		AABB expanded = point.inflate(expands.x, expands.y, expands.z);
		scene.getOutliner()
			.chaseAABB(vec, remainingTicks + 1 >= totalTicks ? point : expanded)
			.lineWidth(1 / 15f)
			.colored(PonderPalette.WHITE.getColor());
	}

}
