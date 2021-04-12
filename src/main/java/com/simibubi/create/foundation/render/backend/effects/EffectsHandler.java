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
		return Minecraft.getInstance().gameRenderer.getFarPlaneDistance();
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
		GL20.glEnable(GL20.GL_DEPTH_TEST);

		GL20.glDepthRange(getNearPlane(), getFarPlane());

//		float[] floats = new float[2];
//		GL20.glGetFloatv(GL20.GL_DEPTH_RANGE, floats);

		prepFramebufferSize();

		Framebuffer mainBuffer = Minecraft.getInstance().getFramebuffer();

		GL30.glBindFramebuffer(FramebufferConstants.FRAME_BUFFER, framebuffer.framebufferObject);

		PostProcessingProgram program = Backend.getProgram(AllProgramSpecs.CHROMATIC);
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
//		Matrix4f inverseView = new Matrix4f();
//		inverseView.loadIdentity();
		program.bindInverseView(inverseView);

		Vector3d pos = new Vector3d(286, 73, -149);
		Vector3d cameraPos = gameRenderer.getActiveRenderInfo().getProjectedView();

		Vector3d shaderPos = pos.subtract(cameraPos).scale(1 / getFarPlane());
		program.setSphere(shaderPos, 20f / getFarPlane(), 0.01f);

		program.setFarPlane(getFarPlane());
		program.setNearPlane(getNearPlane());

		vao.bind();
		GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, 6);
		vao.unbind();

		program.bindColorTexture(0);
		program.bindDepthTexture(0);

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
