package com.simibubi.create.content.logistics.flwdata;

import org.joml.Vector3f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;

public class FlapInstance extends AbstractInstance implements FlatLit {

	public float x;
	public float y;
	public float z;
	public byte blockLight;
	public byte skyLight;
	public int packedLight;
	public float segmentOffsetX;
	public float segmentOffsetY;
	public float segmentOffsetZ;
	public float pivotX;
	public float pivotY;
	public float pivotZ;
	public float horizontalAngle;
	public float intensity;
	public float flapScale;
	public float flapness;

	public FlapInstance(InstanceType<?> type, InstanceHandle handle) {
		super(type, handle);
	}


	public FlapInstance setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public FlapInstance setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public FlapInstance setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	@Override
	public FlapInstance light(int blockLight, int skyLight) {
		return this.light(LightTexture.pack(blockLight, skyLight));
	}

	@Override
	public FlapInstance light(int packedLight) {
		this.packedLight = packedLight;
		return this;
	}

	public FlapInstance setSegmentOffset(float x, float y, float z) {
		this.segmentOffsetX = x;
		this.segmentOffsetY = y;
		this.segmentOffsetZ = z;
		return this;
	}

	public FlapInstance setIntensity(float intensity) {
		this.intensity = intensity;
		return this;
	}

	public FlapInstance setHorizontalAngle(float horizontalAngle) {
		this.horizontalAngle = horizontalAngle;
		return this;
	}

	public FlapInstance setFlapScale(float flapScale) {
		this.flapScale = flapScale;
		return this;
	}

	public FlapInstance setFlapness(float flapness) {
		this.flapness = flapness;
		return this;
	}

	public FlapInstance setPivotVoxelSpace(float x, float y, float z) {
		pivotX = x / 16f;
		pivotY = y / 16f;
		pivotZ = z / 16f;
		return this;
	}

}
