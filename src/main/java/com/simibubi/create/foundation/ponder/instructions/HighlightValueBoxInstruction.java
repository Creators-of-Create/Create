package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.content.PonderPalette;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class HighlightValueBoxInstruction extends TickingInstruction {

	private Vec3d vec;
	private Vec3d expands;

	public HighlightValueBoxInstruction(Vec3d vec, Vec3d expands, int duration) {
		super(false, duration);
		this.vec = vec;
		this.expands = expands;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		AxisAlignedBB point = new AxisAlignedBB(vec, vec);
		AxisAlignedBB expanded = point.grow(expands.x, expands.y, expands.z);
		scene.getOutliner()
			.chaseAABB(vec, remainingTicks == totalTicks ? point : expanded)
			.lineWidth(1 / 32f)
			.colored(PonderPalette.WHITE.getColor());
	}

}
