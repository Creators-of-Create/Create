package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.math.Matrix3f;
import com.simibubi.create.lib.extensions.Matrix3fExtensions;
import com.simibubi.create.lib.util.MixinHelper;

import javax.annotation.Nonnull;

@Mixin(Matrix3f.class)
public abstract class Matrix3fMixin implements Matrix3fExtensions {
	@Shadow
	protected float m00;
	@Shadow
	protected float m01;
	@Shadow
	protected float m02;
	@Shadow
	protected float m10;
	@Shadow
	protected float m11;
	@Shadow
	protected float m12;
	@Shadow
	protected float m20;
	@Shadow
	protected float m21;
	@Shadow
	protected float m22;

	@Override
	public float[] create$writeMatrix() {
		return new float[]{
				m00,
				m10,
				m20,
				m01,
				m11,
				m21,
				m02,
				m12,
				m22,
		};
	}

	@Override
	public void create$set(@Nonnull Matrix3f other) {
		Matrix3fMixin o = MixinHelper.cast(other); // This will look weird in the merged class

		m00 = o.m00;
		m01 = o.m01;
		m02 = o.m02;

		m10 = o.m10;
		m11 = o.m11;
		m12 = o.m12;

		m20 = o.m20;
		m21 = o.m21;
		m22 = o.m22;
	}
}
