package com.simibubi.create.foundation.render.backend.instancing.impl;

import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

public class OrientedData extends BasicData {

	private float posX;
	private float posY;
	private float posZ;
	private float pivotX = 0.5f;
	private float pivotY = 0.5f;
	private float pivotZ = 0.5f;
	private float qX;
	private float qY;
	private float qZ;
	private float qW;


	public OrientedData(InstancedModel<?> owner) {
		super(owner);
	}


	public OrientedData setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedData setPosition(Vector3f pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedData setPosition(float x, float y, float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		return this;
	}

	public OrientedData nudge(float x, float y, float z) {
		this.posX += x;
		this.posY += y;
		this.posZ += z;
		return this;
	}

	public OrientedData setPivot(Vector3f pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedData setPivot(Vec3d pos) {
		return setPosition((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
	}

	public OrientedData setPivot(float x, float y, float z) {
		this.pivotX = x;
		this.pivotY = y;
		this.pivotZ = z;
		return this;
	}

	public OrientedData setRotation(Quaternion q) {
		return setRotation(q.getX(), q.getY(), q.getZ(), q.getW());
	}

	public OrientedData setRotation(float x, float y, float z, float w) {
		this.qX = x;
		this.qY = y;
		this.qZ = z;
		this.qW = w;
		return this;
	}

	@Override
	public void write(ByteBuffer buf) {
		super.write(buf);

		buf.asFloatBuffer().put(new float[] {
				posX,
				posY,
				posZ,
				pivotX,
				pivotY,
				pivotZ,
				qX,
				qY,
				qZ,
				qW
		});

		buf.position(buf.position() + 10 * 4);
	}
}

