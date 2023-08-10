package com.simibubi.create.foundation.collision;

import com.mojang.math.Matrix4f;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Matrix3d {

	double m00, m01, m02;
	double m10, m11, m12;
	double m20, m21, m22;

	public Matrix3d asIdentity() {
		m00 = m11 = m22 = 1;
		m01 = m02 = m10 = m12 = m20 = m21 = 0;
		return this;
	}

	public Matrix3d asXRotation(float radians) {
		asIdentity();
		if (radians == 0)
			return this;

		double s = Mth.sin(radians);
		double c = Mth.cos(radians);
		m22 = m11 = c;
		m21 = s;
		m12 = -s;
		return this;
	}

	public Matrix3d asYRotation(float radians) {
		asIdentity();
		if (radians == 0)
			return this;

		double s = Mth.sin(radians);
		double c = Mth.cos(radians);
		m00 = m22 = c;
		m02 = s;
		m20 = -s;
		return this;
	}

	public Matrix3d asZRotation(float radians) {
		asIdentity();
		if (radians == 0)
			return this;

		double s = Mth.sin(radians);
		double c = Mth.cos(radians);
		m00 = m11 = c;
		m01 = -s;
		m10 = s;
		return this;
	}

	public Matrix3d transpose() {
		double d = m01;
		m01 = m10;
		m10 = d;
		d = m02;
		m02 = m20;
		m20 = d;
		d = m12;
		m12 = m21;
		m21 = d;
		return this;
	}

	public Matrix3d scale(double d) {
		m00 *= d;
		m11 *= d;
		m22 *= d;
		return this;
	}

	public Matrix3d add(Matrix3d matrix) {
		m00 += matrix.m00;
		m01 += matrix.m01;
		m02 += matrix.m02;
		m10 += matrix.m10;
		m11 += matrix.m11;
		m12 += matrix.m12;
		m20 += matrix.m20;
		m21 += matrix.m21;
		m22 += matrix.m22;
		return this;
	}

	public Matrix3d multiply(Matrix3d m) {
		double new00 = m00 * m.m00 + m01 * m.m10 + m02 * m.m20;
		double new01 = m00 * m.m01 + m01 * m.m11 + m02 * m.m21;
		double new02 = m00 * m.m02 + m01 * m.m12 + m02 * m.m22;
		double new10 = m10 * m.m00 + m11 * m.m10 + m12 * m.m20;
		double new11 = m10 * m.m01 + m11 * m.m11 + m12 * m.m21;
		double new12 = m10 * m.m02 + m11 * m.m12 + m12 * m.m22;
		double new20 = m20 * m.m00 + m21 * m.m10 + m22 * m.m20;
		double new21 = m20 * m.m01 + m21 * m.m11 + m22 * m.m21;
		double new22 = m20 * m.m02 + m21 * m.m12 + m22 * m.m22;
		m00 = new00;
		m01 = new01;
		m02 = new02;
		m10 = new10;
		m11 = new11;
		m12 = new12;
		m20 = new20;
		m21 = new21;
		m22 = new22;
		return this;
	}

	public Vec3 transform(Vec3 vec) {
		double x = vec.x * m00 + vec.y * m01 + vec.z * m02;
		double y = vec.x * m10 + vec.y * m11 + vec.z * m12;
		double z = vec.x * m20 + vec.y * m21 + vec.z * m22;
		return new Vec3(x, y, z);
	}

	public Matrix3d copy() {
		return new Matrix3d().add(this);
	}

	float[] conversionBuffer = new float[16];

	@OnlyIn(Dist.CLIENT)
	public Matrix4f getAsMatrix4f() {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				conversionBuffer[j * 4 + i] = i == j ? 1 : 0;

		conversionBuffer[0] = (float) m00;
		conversionBuffer[1] = (float) m01;
		conversionBuffer[2] = (float) m02;
		conversionBuffer[4] = (float) m10;
		conversionBuffer[5] = (float) m11;
		conversionBuffer[6] = (float) m12;
		conversionBuffer[8] = (float) m20;
		conversionBuffer[9] = (float) m21;
		conversionBuffer[10] = (float) m22;

		return new Matrix4f(conversionBuffer);
	}

}
