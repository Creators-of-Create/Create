package com.simibubi.create.content.logistics.block.flap;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.core.materials.IFlatLight;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;

public class FlapData extends InstanceData implements IFlatLight<FlapData> {

	float x;
	float y;
	float z;
	byte blockLight;
	byte skyLight;
	float segmentOffsetX;
	float segmentOffsetY;
	float segmentOffsetZ;
	float pivotX;
	float pivotY;
	float pivotZ;
	float horizontalAngle;
	float intensity;
	float flapScale;
	float flapness;

	public FlapData setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public FlapData setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
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
	public void write(VecBuffer buf) {
		buf.putVec3(x, y, z);
		buf.putVec2(blockLight, skyLight);

		buf.putVec3(segmentOffsetX, segmentOffsetY, segmentOffsetZ);
		buf.putVec3(pivotX, pivotY, pivotZ);

		buf.putFloat(horizontalAngle);
		buf.putFloat(intensity);
		buf.putFloat(flapScale);

		buf.putFloat(flapness);
	}
}
