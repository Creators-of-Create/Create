package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.outliner.LineOutline;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class RotateTool extends PlacementToolBase {

	private LineOutline line = new LineOutline();

	@Override
	public boolean handleMouseWheel(double delta) {
		schematicHandler.getTransformation()
			.rotate90(delta > 0);
		schematicHandler.markDirty();
		return true;
	}

	@Override
	public void renderOnSchematic(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		AxisAlignedBB bounds = schematicHandler.getBounds();
		double height = bounds.getYsize() + Math.max(20, bounds.getYsize());
		Vector3d center = bounds.getCenter()
			.add(schematicHandler.getTransformation()
				.getRotationOffset(false));
		Vector3d start = center.subtract(0, height / 2, 0);
		Vector3d end = center.add(0, height / 2, 0);

		line.getParams()
			.disableCull()
			.disableNormals()
			.colored(0xdddddd)
			.lineWidth(1 / 16f);
		line.set(start, end)
			.render(ms, buffer, AnimationTickHolder.getPartialTicks());

		super.renderOnSchematic(ms, buffer);
	}

}
