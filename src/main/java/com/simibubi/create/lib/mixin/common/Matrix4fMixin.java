package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.math.Matrix4f;
import com.simibubi.create.lib.extensions.Matrix4fExtensions;

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
}
