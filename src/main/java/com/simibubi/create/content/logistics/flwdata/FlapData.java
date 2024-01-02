package com.simibubi.create.content.logistics.flwdata;

import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;
import com.jozufozu.flywheel.lib.instance.FlatLit;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;

public class FlapData extends AbstractInstance implements FlatLit {

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

	protected FlapData(InstanceType<?> type, InstanceHandle handle) {
		super(type, handle);
	}


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
		return this;
	}

	@Override
	public FlapData setBlockLight(int blockLight) {
		this.blockLight = (byte) (blockLight & 0xF);
		return this;
	}

	@Override
	public FlapData setSkyLight(int skyLight) {
		this.skyLight = (byte) (skyLight & 0xF);
		return this;
	}

	@Override
	public int getPackedLight() {
		return LightTexture.pack(this.blockLight, this.skyLight);
	}

	public FlapData setSegmentOffset(float x, float y, float z) {
		this.segmentOffsetX = x;
		this.segmentOffsetY = y;
		this.segmentOffsetZ = z;
		return this;
	}

	public FlapData setIntensity(float intensity) {
		this.intensity = intensity;
		return this;
	}

	public FlapData setHorizontalAngle(float horizontalAngle) {
		this.horizontalAngle = horizontalAngle;
		return this;
	}

	public FlapData setFlapScale(float flapScale) {
		this.flapScale = flapScale;
		return this;
	}

	public FlapData setFlapness(float flapness) {
		this.flapness = flapness;
		return this;
	}

	public FlapData setPivotVoxelSpace(float x, float y, float z) {
		pivotX = x / 16f;
		pivotY = y / 16f;
		pivotZ = z / 16f;
		return this;
	}

}
