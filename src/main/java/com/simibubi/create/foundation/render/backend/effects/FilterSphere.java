package com.simibubi.create.foundation.render.backend.effects;

import java.nio.FloatBuffer;

import com.simibubi.create.foundation.render.backend.RenderUtil;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class FilterSphere {
	public Vector3d center;
	public float radius;
	public float feather;
	public float fade;
	public float density = 2;
	public float strength = 1;
	public boolean blendOver = false;

	public float r;
	public float g;
	public float b;
	public float colorFeather;

	public Matrix4f filter;

	public FilterSphere setCenter(Vector3d center) {
		this.center = center;
		return this;
	}

	public FilterSphere setCenter(double x, double y, double z) {
		this.center = new Vector3d(x, y, z);
		return this;
	}

	public FilterSphere setRadius(float radius) {
		this.radius = radius;
		return this;
	}

	public FilterSphere setFeather(float feather) {
		this.feather = feather;
		return this;
	}

	public FilterSphere setFade(float fade) {
		this.fade = fade;
		return this;
	}

	public FilterSphere setDensity(float density) {
		this.density = density;
		return this;
	}

	public FilterSphere setStrength(float strength) {
		this.strength = strength;
		return this;
	}

	public FilterSphere setFilter(Matrix4f filter) {
		this.filter = filter;
		return this;
	}

	public FilterSphere setBlendOver(boolean blendOver) {
		this.blendOver = blendOver;
		return this;
	}

	public void write(FloatBuffer buf) {
		buf.put(new float[]{
				(float) center.x,
				(float) center.y,
				(float) center.z,
				radius,
				feather,
				fade,
				density,
				blendOver ? 1f : 0f,
				1f,
				1f,
				0f,
				0f,
				0.5f, //r,
				0.1f, //g,
				0.1f, //b,
				0f, //colorFeather,
		});

		buf.put(RenderUtil.writeMatrix(filter));
	}
}
