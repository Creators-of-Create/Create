package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.content.PonderPalette;

import net.minecraft.world.phys.Vec3;

public class LineInstruction extends TickingInstruction {

	private PonderPalette color;
	private Vec3 start;
	private Vec3 end;

	public LineInstruction(PonderPalette color, Vec3 start, Vec3 end, int ticks) {
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
