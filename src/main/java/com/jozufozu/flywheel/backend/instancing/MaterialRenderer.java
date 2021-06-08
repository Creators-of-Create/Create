package com.jozufozu.flywheel.backend.instancing;

import java.util.function.Supplier;

import com.jozufozu.flywheel.core.shader.IProgramCallback;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;

public class MaterialRenderer<P extends WorldProgram> {

	private final Supplier<P> program;
	private final InstanceMaterial<?> material;

	public MaterialRenderer(Supplier<P> programSupplier, InstanceMaterial<?> material) {
		this.program = programSupplier;
		this.material = material;
	}

	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> setup) {
		if (!(layer == RenderType.getCutoutMipped())) return;

		if (material.nothingToRender()) return;

		P program = this.program.get();

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
