package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.outliner.LineOutline;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
	public void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer) {
		AABB bounds = schematicHandler.getBounds();
		double height = bounds.getYsize() + Math.max(20, bounds.getYsize());
		Vec3 center = bounds.getCenter()
			.add(schematicHandler.getTransformation()
				.getRotationOffset(false));
		Vec3 start = center.subtract(0, height / 2, 0);
		Vec3 end = center.add(0, height / 2, 0);

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
