package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.core.shader.IProgramCallback;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;

public class MaterialRenderer<P extends WorldProgram> {

	private final P program;
	private final InstanceMaterial<?> material;

	public MaterialRenderer(P program, InstanceMaterial<?> material) {
		this.program = program;
		this.material = material;
	}

	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> setup) {
		if (!(layer == RenderType.getCutoutMipped())) return;

		program.bind();
		program.uploadViewProjection(viewProjection);
		program.uploadCameraPos(camX, camY, camZ);

		if (setup != null) setup.call(program);

		makeRenderCalls();
	}

	protected void makeRenderCalls() {
		material.forEachInstancer(Instancer::render);
	}
}
