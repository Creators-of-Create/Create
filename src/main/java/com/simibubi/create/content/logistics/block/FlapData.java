package com.simibubi.create.content.logistics.block;

import java.nio.ByteBuffer;

import com.simibubi.create.foundation.render.backend.core.IFlatLight;
import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public class FlapData extends InstanceData implements IFlatLight<FlapData> {

	private float x;
	private float y;
	private float z;
	private byte blockLight;
	private byte skyLight;

	private float segmentOffsetX;
	private float segmentOffsetY;
	private float segmentOffsetZ;

	private float pivotX;
	private float pivotY;
	private float pivotZ;

	private float horizontalAngle;
	private float intensity;
	private float flapScale;

	private float flapness;

	public FlapData(InstancedModel<?> owner) {
		super(owner);
	}

	public FlapData setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public FlapData setPosition(Vector3f pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public FlapData setPosition(int x, int y, int z) {
		BlockPos origin = owner.renderer.getOriginCoordinate();

		return setPosition((float) (x - origin.getX()),
				(float) (y - origin.getY()),
				(float) (z - origin.getZ()));
	}

	public FlapData setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		markDirty();
		return this;
	}

	@Override
	public FlapData setBlockLight(int blockLight) {
		this.blockLight = (byte) ((blockLight & 0xF) << 4);
		markDirty();
		return this;
	}

	@Override
	public FlapData setSkyLight(int skyLight) {
		this.skyLight = (byte) ((skyLight & 0xF) << 4);
		markDirty();
		return this;
	}

	public FlapData setSegmentOffset(float x, float y, float z) {
		this.segmentOffsetX = x;
		this.segmentOffsetY = y;
		this.segmentOffsetZ = z;
		markDirty();
		return this;
	}

	public FlapData setIntensity(float intensity) {
		this.intensity = intensity;
		markDirty();
		return this;
	}

	public FlapData setHorizontalAngle(float horizontalAngle) {
		this.horizontalAngle = horizontalAngle;
		markDirty();
		return this;
	}

	public FlapData setFlapScale(float flapScale) {
		this.flapScale = flapScale;
		markDirty();
		return this;
	}

	public FlapData setFlapness(float flapness) {
		this.flapness = flapness;
		markDirty();
		return this;
	}

	public FlapData setPivotVoxelSpace(float x, float y, float z) {
		pivotX = x / 16f;
		pivotY = y / 16f;
		pivotZ = z / 16f;
		markDirty();
		return this;
	}

	@Override
	public void write(ByteBuffer buf) {
		putVec3(buf, x, y, z);
		putVec2(buf, blockLight, skyLight);

		putVec3(buf, segmentOffsetX, segmentOffsetY, segmentOffsetZ);
		putVec3(buf, pivotX, pivotY, pivotZ);

		put(buf, horizontalAngle);
		put(buf, intensity);
		put(buf, flapScale);

		put(buf, flapness);
	}
}
