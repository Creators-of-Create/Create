package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.outliner.LineOutline;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

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
	public void renderOnSchematic(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		super.renderOnSchematic(ms, buffer, light, overlay);

		AxisAlignedBB bounds = schematicHandler.getBounds();
		double height = bounds.getYSize() + Math.max(20, bounds.getYSize());
		Vec3d center = bounds.getCenter()
			.add(schematicHandler.getTransformation()
				.getRotationOffset(false));
		Vec3d start = center.subtract(0, height / 2, 0);
		Vec3d end = center.add(0, height / 2, 0);

		line.getParams()
			.colored(0x4d80e4)
			.disableCull()
			.disableNormals()
			.lineWidth(1 / 16f);
		line.set(start, end)
			.render(ms, buffer);
	}

}
