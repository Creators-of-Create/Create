package com.jozufozu.flywheel.backend.core.materials;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.util.RenderUtil;
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
	public void write(MappedBuffer buf) {
		super.write(buf);
		buf.putFloatArray(matrices);
	}
}
