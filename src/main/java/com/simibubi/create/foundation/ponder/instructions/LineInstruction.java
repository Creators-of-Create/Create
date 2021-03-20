package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.content.PonderPalette;

import net.minecraft.util.math.vector.Vector3d;

public class LineInstruction extends TickingInstruction {

	private PonderPalette color;
	private Vector3d start;
	private Vector3d end;

	public LineInstruction(PonderPalette color, Vector3d start, Vector3d end, int ticks) {
		super(false, ticks);
		this.color = color;
		this.start = start;
		this.end = end;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		scene.getOutliner()
			.showLine(start, start, end)
			.lineWidth(1 / 16f)
			.colored(color.getColor());
	}

}
