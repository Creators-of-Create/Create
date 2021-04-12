package com.simibubi.create.foundation.render.backend.effects;

import org.lwjgl.opengl.GL20;

import com.simibubi.create.foundation.render.backend.gl.shader.GlProgram;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class PostProcessingProgram extends GlProgram {

	final int uDepth;
	final int uColor;

	final int uInverseProjection;
	final int uInverseView;

	final int uNearPlane;
	final int uFarPlane;
	final int uSphereCenter;
	final int uSphereRadius;
	final int uSphereFeather;


	public PostProcessingProgram(ResourceLocation name, int handle) {
		super(name, handle);

		uInverseProjection = getUniformLocation("uInverseProjection");
		uInverseView = getUniformLocation("uInverseView");
		uNearPlane = getUniformLocation("uNearPlane");
		uFarPlane = getUniformLocation("uFarPlane");
		uSphereCenter = getUniformLocation("uSphereCenter");
		uSphereRadius = getUniformLocation("uSphereRadius");
		uSphereFeather = getUniformLocation("uSphereFeather");

		bind();
		uDepth = setSamplerBinding("uDepth", 8);
		uColor = setSamplerBinding("uColor", 9);
		unbind();
	}

	public void setNearPlane(float nearPlane) {
		GL20.glUniform1f(uNearPlane, nearPlane);
	}

	public void setFarPlane(float farPlane) {
		GL20.glUniform1f(uFarPlane, farPlane);
	}

	public void setSphere(Vector3d center, float radius, float feather) {
		GL20.glUniform3f(uSphereCenter, (float) center.x, (float) center.y, (float) center.z);

		GL20.glUniform1f(uSphereRadius, radius);
		GL20.glUniform1f(uSphereFeather, feather);
	}

	public void bindInverseProjection(Matrix4f mat) {
		uploadMatrixUniform(uInverseProjection, mat);
	}

	public void bindInverseView(Matrix4f mat) {
		uploadMatrixUniform(uInverseView, mat);
	}

	public void bindDepthTexture(int textureObject) {
		GL20.glActiveTexture(GL20.GL_TEXTURE8);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureObject);
	}

	public void bindColorTexture(int textureObject) {
		GL20.glActiveTexture(GL20.GL_TEXTURE9);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureObject);
	}

}
