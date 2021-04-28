package com.simibubi.create.foundation.render.backend.effects;

import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

public class ColorMatrices {

	public static final float lumaR = 0.3086f;
	public static final float lumaG = 0.6094f;
	public static final float lumaB = 0.0820f;

	public static Matrix4f invert() {
		Matrix4f invert = new Matrix4f();
		invert.a00 = -1.0F;
		invert.a11 = -1.0F;
		invert.a22 = -1.0F;
		invert.a33 = -1.0F;
		invert.a30 = 1;
		invert.a31 = 1;
		invert.a32 = 1;

		return invert;
	}

	public static Matrix4f grayscale() {
		Matrix4f mat = new Matrix4f();

		mat.a00 = mat.a01 = mat.a02 = lumaR;
		mat.a10 = mat.a11 = mat.a12 = lumaG;
		mat.a20 = mat.a21 = mat.a22 = lumaB;
		mat.a33 = 1;

		return mat;
	}

	public static Matrix4f saturate(float s) {
		Matrix4f mat = new Matrix4f();

		mat.a00 = (1.0f - s) * lumaR + s;
		mat.a01 = (1.0f - s) * lumaR;
		mat.a02 = (1.0f - s) * lumaR;
		mat.a10 = (1.0f - s) * lumaG;
		mat.a11 = (1.0f - s) * lumaG + s;
		mat.a12 = (1.0f - s) * lumaG;
		mat.a20 = (1.0f - s) * lumaB;
		mat.a21 = (1.0f - s) * lumaB;
		mat.a22 = (1.0f - s) * lumaB + s;

		mat.a33 = 1;

		return mat;
	}

	public static Matrix4f sepia(float amount) {
		Matrix4f mat = new Matrix4f();

		mat.a00 = (float) (0.393 + 0.607 * (1 - amount));
		mat.a10 = (float) (0.769 - 0.769 * (1 - amount));
		mat.a20 = (float) (0.189 - 0.189 * (1 - amount));
		mat.a01 = (float) (0.349 - 0.349 * (1 - amount));
		mat.a11 = (float) (0.686 + 0.314 * (1 - amount));
		mat.a21 = (float) (0.168 - 0.168 * (1 - amount));
		mat.a02 = (float) (0.272 - 0.272 * (1 - amount));
		mat.a12 = (float) (0.534 - 0.534 * (1 - amount));
		mat.a22 = (float) (0.131 + 0.869 * (1 - amount));

		mat.a33 = 1;

		return mat;
	}

	// https://stackoverflow.com/a/8510751
	public static Matrix4f hueShift(float rot) {
		Matrix4f mat = new Matrix4f();

		mat.loadIdentity();

		float cosA = MathHelper.cos(AngleHelper.rad(rot));
		float sinA = MathHelper.sin(AngleHelper.rad(rot));
		mat.a00 = (float) (cosA + (1.0 - cosA) / 3.0);
		mat.a01 = (float) (1. / 3. * (1.0 - cosA) - MathHelper.sqrt(1. / 3.) * sinA);
		mat.a02 = (float) (1. / 3. * (1.0 - cosA) + MathHelper.sqrt(1. / 3.) * sinA);
		mat.a10 = (float) (1. / 3. * (1.0 - cosA) + MathHelper.sqrt(1. / 3.) * sinA);
		mat.a11 = (float) (cosA + 1. / 3. * (1.0 - cosA));
		mat.a12 = (float) (1. / 3. * (1.0 - cosA) - MathHelper.sqrt(1. / 3.) * sinA);
		mat.a20 = (float) (1. / 3. * (1.0 - cosA) - MathHelper.sqrt(1. / 3.) * sinA);
		mat.a21 = (float) (1. / 3. * (1.0 - cosA) + MathHelper.sqrt(1. / 3.) * sinA);
		mat.a22 = (float) (cosA + 1. / 3. * (1.0 - cosA));

		return mat;
	}

	public static Matrix4f darken(float amount) {
		Matrix4f mat = new Matrix4f();
		mat.loadIdentity();
		mat.multiply(1f - amount);
		return mat;
	}

	public static Matrix4f brightness(float amount) {
		Matrix4f mat = new Matrix4f();
		mat.loadIdentity();
		mat.a03 = amount;
		mat.a13 = amount;
		mat.a23 = amount;
		return mat;
	}

	public static Matrix4f contrast(float amount) {
		Matrix4f sub = new Matrix4f();
		sub.a00 = amount;
		sub.a11 = amount;
		sub.a22 = amount;
		sub.a33 = 1;
		sub.a30 = 0.5f - amount * 0.5f;
		sub.a31 = 0.5f - amount * 0.5f;
		sub.a32 = 0.5f - amount * 0.5f;

		return sub;
	}

	public static Matrix4f identity() {
		Matrix4f mat = new Matrix4f();
		mat.loadIdentity();
		return mat;
	}
}
