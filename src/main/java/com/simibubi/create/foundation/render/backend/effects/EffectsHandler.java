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
		projection.a33 = 1;
		projection.invert();
		program.bindInverseProjection(projection);

		Matrix4f inverseView = view.copy();
		inverseView.invert();
		program.bindInverseView(inverseView);

		Vector3d pos1 = new Vector3d(852, 79, -204);
		Vector3d cameraPos = gameRenderer.getActiveRenderInfo().getProjectedView();

		program.setCameraPos(cameraPos.inverse());

//		int n = 20;
//		double rad = 15;
//		for (int i = 0; i < n; i++) {
//			double angle = ((double) i) / n * Math.PI * 2;
//			program.addSphere(new SphereFilterProgram.FilterSphere()
//					.setCenter(pos1.subtract(cameraPos).add(Math.sin(angle) * rad, 0, Math.cos(angle) * rad))
//					.setRadius(10)
//					.setFeather(0f)
//					.setFilter(ColorMatrices.hueShiftRGB((float) i / n * 360 + i / 2f)));
//		}

		Matrix4f filter = ColorMatrices.sepia(1f);

		program.addSphere(new SphereFilterProgram.FilterSphere()
				.setCenter(pos1.subtract(cameraPos))
				.setRadius(40)
				.setFeather(20f)
				.setHsv(false)
				.setFilter(filter));

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
