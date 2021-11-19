package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.math.Matrix4f;
import com.simibubi.create.lib.extensions.Matrix4fExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements Matrix4fExtensions {
	@Shadow
	protected float m00;
	@Shadow
	protected float m01;
	@Shadow
	protected float m02;
	@Shadow
	protected float m03;
	@Shadow
	protected float m10;
	@Shadow
	protected float m11;
	@Shadow
	protected float m12;
	@Shadow
	protected float m13;
	@Shadow
	protected float m20;
	@Shadow
	protected float m21;
	@Shadow
	protected float m22;
	@Shadow
	protected float m23;
	@Shadow
	protected float m30;
	@Shadow
	protected float m31;
	@Shadow
	protected float m32;
	@Shadow
	protected float m33;

	@Override
	public void create$set(@NotNull Matrix4f other) {
		Matrix4fMixin o = MixinHelper.cast(other); // This will look weird in the merged class

		m00 = o.m00;
		m01 = o.m01;
		m02 = o.m02;
		m03 = o.m03;

		m10 = o.m10;
		m11 = o.m11;
		m12 = o.m12;
		m13 = o.m13;

		m20 = o.m20;
		m21 = o.m21;
		m22 = o.m22;
		m23 = o.m23;

		m30 = o.m30;
		m31 = o.m31;
		m32 = o.m32;
		m33 = o.m33;
	}

	@Override
	@Contract(mutates = "this")
	public void create$fromFloatArray(float[] floats) {
		m00 = floats[0];
		m01 = floats[1];
		m02 = floats[2];
		m03 = floats[3];

		m10 = floats[4];
		m11 = floats[5];
		m12 = floats[6];
		m13 = floats[7];

		m20 = floats[8];
		m21 = floats[9];
		m22 = floats[10];
		m23 = floats[11];

		m30 = floats[12];
		m31 = floats[13];
		m32 = floats[14];
		m33 = floats[15];
	}

	@Override
	public float[] create$writeMatrix() {
		return new float[]{
				m00,
				m10,
				m20,
				m30,
				m01,
				m11,
				m21,
				m31,
				m02,
				m12,
				m22,
				m32,
				m03,
				m13,
				m23,
				m33,
		};
	}

	public void create$setTranslation(float x, float y, float z) {
		m00 = 1.0F;
		m11 = 1.0F;
		m22 = 1.0F;
		m33 = 1.0F;
		m03 = x;
		m13 = y;
		m23 = z;
	}
}
