package com.simibubi.create.foundation.render.backend.effects;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.gl.GlBuffer;
import com.simibubi.create.foundation.render.backend.gl.GlPrimitiveType;
import com.simibubi.create.foundation.render.backend.gl.GlVertexArray;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class EffectsHandler {

	public static float getNearPlane() {
		return 0.05f;
	}

	public static float getFarPlane() {
		return Minecraft.getInstance().gameRenderer.getFarPlaneDistance() * 4;
	}

	public static final float[] vertices = {
			// pos        // tex
			-1.0f, -1.0f, 0.0f, 0.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			-1.0f, 1.0f, 0.0f, 1.0f,

			-1.0f, -1.0f, 0.0f, 0.0f,
			1.0f, -1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 1.0f, 1.0f
	};

	private static final int bufferSize = vertices.length * 4;

	private final Framebuffer framebuffer;
	private final GlVertexArray vao = new GlVertexArray();

	private final GlBuffer vbo = new GlBuffer(GL20.GL_ARRAY_BUFFER);

	public EffectsHandler() {
		Framebuffer render = Minecraft.getInstance().getFramebuffer();
		framebuffer = new Framebuffer(render.framebufferWidth, render.framebufferHeight, false, Minecraft.IS_RUNNING_ON_MAC);

		vbo.bind();
		vbo.alloc(bufferSize, GL15.GL_STATIC_DRAW);
		vbo.map(bufferSize, buf -> buf.asFloatBuffer().put(vertices));

		vao.bind();

		GL20.glEnableVertexAttribArray(0);

		GL20.glVertexAttribPointer(0, 4, GlPrimitiveType.FLOAT.getGlConstant(), false, 4 * 4, 0);

		vao.unbind();
		vbo.unbind();
	}

	public void prepFramebufferSize() {
		MainWindow window = Minecraft.getInstance().getWindow();
		if (framebuffer.framebufferWidth != window.getFramebufferWidth()
				|| framebuffer.framebufferHeight != window.getFramebufferHeight()) {
			framebuffer.func_216491_a(window.getFramebufferWidth(), window.getFramebufferHeight(),
					Minecraft.IS_RUNNING_ON_MAC);
		}
	}

	public void render(Matrix4f view) {
//		if (true) {
//			return;
//		}

		GL20.glEnable(GL20.GL_DEPTH_TEST);

		GL20.glDepthRange(getNearPlane(), getFarPlane());

//		float[] floats = new float[2];
//		GL20.glGetFloatv(GL20.GL_DEPTH_RANGE, floats);

		prepFramebufferSize();

		Framebuffer mainBuffer = Minecraft.getInstance().getFramebuffer();

		GL30.glBindFramebuffer(FramebufferConstants.FRAME_BUFFER, framebuffer.framebufferObject);
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);

		SphereFilterProgram program = Backend.getProgram(AllProgramSpecs.CHROMATIC);
		program.bind();

		program.bindColorTexture(mainBuffer.getColorAttachment());
		program.bindDepthTexture(mainBuffer.getDepthAttachment());

		GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
		Matrix4f projection = gameRenderer.getBasicProjectionMatrix(gameRenderer.getActiveRenderInfo(), AnimationTickHolder.getPartialTicks(), true);
		//Matrix4f projection = Backend.projectionMatrix.copy();
		//projection.a23 = projection.a32 = 0;
		projection.a33 = 1;
		projection.invert();
		program.bindInverseProjection(projection);

		Matrix4f inverseView = view.copy();
		inverseView.invert();
		program.bindInverseView(inverseView);

		Vector3d pos1 = new Vector3d(330, 0, 110);
//		Vector3d pos1 = new Vector3d(852, 79, -204);
//		Vector3d pos2 = new Vector3d(858, 95, -260);
//		Vector3d pos3 = new Vector3d(906, 84, -207);
		Vector3d cameraPos = gameRenderer.getActiveRenderInfo().getProjectedView();

		for (int i = 0; i < 16; i++) {
			double angle = (Math.PI * AnimationTickHolder.getRenderTime() / 40) + i * Math.PI / 4;

			program.addSphere(new SphereFilterProgram.FilterSphere()
					.setCenter(pos1.subtract(cameraPos).add(0, 0, i * 30))
					.setRadius(15)
					.setFeather(0.5f)
					.setFilter(ColorMatrices.hueShift((float) i / 16 * 360 + AnimationTickHolder.getRenderTime())));
		}

//		program.addSphere(new SphereFilterProgram.FilterSphere()
//				.setCenter(pos2.subtract(cameraPos))
//				.setRadius(50)
//				.setFeather(0.5f)
//				.setFilter(ColorMatrices.sepia(1)));
//
//		program.addSphere(new SphereFilterProgram.FilterSphere()
//				.setCenter(pos3.subtract(cameraPos))
//				.setRadius(20)
//				.setFeather(3f)
//				.setFilter(ColorMatrices.saturate(4)));

		program.uploadFilters();

		program.setFarPlane(getFarPlane());
		program.setNearPlane(getNearPlane());

		vao.bind();
		GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, 6);
		vao.unbind();

		program.bindColorTexture(0);
		program.bindDepthTexture(0);
		GL20.glActiveTexture(GL20.GL_TEXTURE0);

		program.clear();
		program.unbind();

		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebuffer.framebufferObject);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainBuffer.framebufferObject);
		GL30.glBlitFramebuffer(0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, 0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);
		GL30.glBindFramebuffer(FramebufferConstants.FRAME_BUFFER, mainBuffer.framebufferObject);
	}

	public void delete() {
		framebuffer.deleteFramebuffer();

		vao.delete();
		vbo.delete();
	}
}
