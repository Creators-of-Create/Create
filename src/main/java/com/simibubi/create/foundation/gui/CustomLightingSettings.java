package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class CustomLightingSettings implements ILightingSettings {

	private Vector3f light1;
	private Vector3f light2;
	private Matrix4f lightMatrix;

	protected CustomLightingSettings(float yRot, float xRot) {
		init(yRot, xRot, 0, 0, false);
	}

	protected CustomLightingSettings(float yRot1, float xRot1, float yRot2, float xRot2) {
		init(yRot1, xRot1, yRot2, xRot2, true);
	}

	protected void init(float yRot1, float xRot1, float yRot2, float xRot2, boolean doubleLight) {
		light1 = Vector3f.POSITIVE_Z.copy();
		light1.func_214905_a(Vector3f.POSITIVE_Y.getDegreesQuaternion(yRot1));
		light1.func_214905_a(Vector3f.POSITIVE_X.getDegreesQuaternion(xRot1));

		if (doubleLight) {
			light2 = Vector3f.POSITIVE_Z.copy();
			light2.func_214905_a(Vector3f.POSITIVE_Y.getDegreesQuaternion(yRot2));
			light2.func_214905_a(Vector3f.POSITIVE_X.getDegreesQuaternion(xRot2));
		} else {
			light2 = VecHelper.ZERO_3F;
		}

		lightMatrix = new Matrix4f();
		lightMatrix.loadIdentity();
	}

	@Override
	public void applyLighting() {
		RenderSystem.setupLevelDiffuseLighting(light1, light2, lightMatrix);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private float yRot1, xRot1;
		private float yRot2, xRot2;
		private boolean doubleLight;

		public Builder firstLightRotation(float yRot, float xRot) {
			yRot1 = yRot;
			xRot1 = xRot;
			return this;
		}

		public Builder secondLightRotation(float yRot, float xRot) {
			yRot2 = yRot;
			xRot2 = xRot;
			doubleLight = true;
			return this;
		}

		public Builder doubleLight() {
			doubleLight = true;
			return this;
		}

		public CustomLightingSettings build() {
			if (doubleLight) {
				return new CustomLightingSettings(yRot1, xRot1, yRot2, xRot2);
			} else {
				return new CustomLightingSettings(yRot1, xRot1);
			}
		}

	}

}
