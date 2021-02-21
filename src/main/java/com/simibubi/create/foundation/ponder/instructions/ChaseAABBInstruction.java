package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.content.PonderPalette;

import net.minecraft.util.math.AxisAlignedBB;

public class ChaseAABBInstruction extends TickingInstruction {

	private AxisAlignedBB bb;
	private Object slot;
	private PonderPalette color;

	public ChaseAABBInstruction(PonderPalette color, Object slot, AxisAlignedBB bb, int ticks) {
		super(false, ticks);
		this.color = color;
		this.slot = slot;
		this.bb = bb;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		scene.getOutliner()
			.chaseAABB(slot, bb)
			.lineWidth(1 / 16f)
			.colored(color.getColor());
	}

}
