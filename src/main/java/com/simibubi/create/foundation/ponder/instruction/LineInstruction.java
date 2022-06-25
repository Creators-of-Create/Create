package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.utility.outliner.Outline.OutlineParams;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LineInstruction extends TickingInstruction {

	private PonderPalette color;
	private Vec3 start;
	private Vec3 end;
	private boolean chaseEnd;

	public LineInstruction(PonderPalette color, Vec3 start, Vec3 end, int ticks, boolean chaseEnd) {
		super(false, ticks);
		this.color = color;
		this.start = start;
		this.end = end;
		this.chaseEnd = chaseEnd;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		int ticksElapsed = totalTicks - remainingTicks;
		float progress = Mth.clamp(ticksElapsed / 15f, 0, 1);

		OutlineParams line = chaseEnd ? scene.getOutliner()
			.endChasingLine(start, start, end, progress, true)
			: scene.getOutliner()
				.showLine(start, start, end);
		line.lineWidth(1 / 16f)
			.colored(color.getColor());
	}

}
