package com.jozufozu.flywheel.backend.effects;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderUtil;
import com.jozufozu.flywheel.backend.core.EffectsContext;
import com.jozufozu.flywheel.backend.gl.GlBuffer;
import com.jozufozu.flywheel.backend.gl.GlPrimitiveType;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
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

	private final ArrayList<FilterSphere> spheres;

	public EffectsHandler() {
		spheres = new ArrayList<>();

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

	public void addSphere(FilterSphere sphere) {
		this.spheres.add(sphere);
	}

	public void render(Matrix4f view) {
		if (spheres.size() == 0) {
			return;
		}

		GL20.glEnable(GL20.GL_DEPTH_TEST);

		GL20.glDepthRange(getNearPlane(), getFarPlane());

		prepFramebufferSize();

		Framebuffer mainBuffer = Minecraft.getInstance().getFramebuffer();

		Backend.compat.fbo.bindFramebuffer(FramebufferConstants.FRAME_BUFFER, framebuffer.framebufferObject);
		GL11.glClear(GL30.GL_COLOR_BUFFER_BIT);

		SphereFilterProgram program = EffectsContext.INSTANCE.getProgram(AllProgramSpecs.CHROMATIC);
		program.bind();

		program.bindColorTexture(mainBuffer.getColorAttachment());
		program.bindDepthTexture(mainBuffer.getDepthAttachment());

		GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
		ActiveRenderInfo activeRenderInfo = gameRenderer.getActiveRenderInfo();
		Matrix4f projection = gameRenderer.getBasicProjectionMatrix(activeRenderInfo, AnimationTickHolder.getPartialTicks(), true);
		projection.a33 = 1;
		projection.invert();
		program.bindInverseProjection(projection);

		Matrix4f inverseView = view.copy();
		inverseView.invert();
		program.bindInverseView(inverseView);

		Vector3d cameraPos = activeRenderInfo.getProjectedView();

		program.setCameraPos(cameraPos.inverse());

		for (FilterSphere sphere : spheres) {
			sphere.x -= cameraPos.x;
			sphere.y -= cameraPos.y;
			sphere.z -= cameraPos.z;
		}

		spheres.sort((o1, o2) -> {
			double l1 = RenderUtil.length(o1.x, o1.y, o1.z);
			double l2 = RenderUtil.length(o2.x, o2.y, o2.z);
			return (int) Math.signum(l2 - l1);
		});

		program.uploadFilters(spheres);

		program.setFarPlane(getFarPlane());
		program.setNearPlane(getNearPlane());

		vao.bind();
		GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, 6);
		vao.unbind();

		program.bindColorTexture(0);
		program.bindDepthTexture(0);
		GL20.glActiveTexture(GL20.GL_TEXTURE0);

		program.unbind();
		spheres.clear();

		Backend.compat.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebuffer.framebufferObject);
		Backend.compat.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainBuffer.framebufferObject);
		Backend.compat.blit.blitFramebuffer(0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, 0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);
		Backend.compat.fbo.bindFramebuffer(FramebufferConstants.FRAME_BUFFER, mainBuffer.framebufferObject);
	}

	public void delete() {
		framebuffer.deleteFramebuffer();

		vao.delete();
		vbo.delete();
	}

	private void prepFramebufferSize() {
		MainWindow window = Minecraft.getInstance().getWindow();
		if (framebuffer.framebufferWidth != window.getFramebufferWidth()
				|| framebuffer.framebufferHeight != window.getFramebufferHeight()) {
			framebuffer.func_216491_a(window.getFramebufferWidth(), window.getFramebufferHeight(),
					Minecraft.IS_RUNNING_ON_MAC);
		}
	}
}
