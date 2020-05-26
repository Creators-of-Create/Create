package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.Vec3d;

public class LineOutline extends Outline {

	private Vec3d start = Vec3d.ZERO;
	private Vec3d end = Vec3d.ZERO;

	public LineOutline set(Vec3d start, Vec3d end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		renderAACuboidLine(ms, buffer, start, end, params.disableCull);
	}

}
