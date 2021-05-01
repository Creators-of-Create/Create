package com.jozufozu.flywheel.backend.core;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.RenderUtil;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.mojang.blaze3d.matrix.MatrixStack;

public class ModelData extends BasicData {
	private static final float[] empty = new float[25];

	private float[] matrices = empty;

	public ModelData(InstancedModel<?> owner) {
		super(owner);
	}

	public ModelData setTransform(MatrixStack stack) {
		matrices = RenderUtil.writeMatrixStack(stack);
		markDirty();
		return this;
	}

	@Override
	public void write(ByteBuffer buf) {
		super.write(buf);
		buf.asFloatBuffer().put(matrices);
		buf.position(buf.position() + matrices.length * 4);
	}
}
