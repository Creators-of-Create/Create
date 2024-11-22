package com.simibubi.create.content.kinetics.base;

import org.joml.Vector3f;

import com.simibubi.create.foundation.utility.Color;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.ColoredLitInstance;
import net.minecraft.core.BlockPos;

public class KineticInstance extends ColoredLitInstance {
    public float x;
    public float y;
    public float z;
    public float rotationalSpeed;
    public float rotationOffset;

	protected KineticInstance(InstanceType<? extends KineticInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public KineticInstance setPosition(BlockPos pos) {
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public KineticInstance setPosition(Vector3f pos) {
        return setPosition(pos.x(), pos.y(), pos.z());
    }

    public KineticInstance setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public KineticInstance nudge(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public KineticInstance setColor(KineticBlockEntity blockEntity) {
        if (blockEntity.hasNetwork()) {
            setColor(Color.generateFromLong(blockEntity.network));
        }else {
            color(0xFF, 0xFF, 0xFF);
        }
        return this;
    }

    public KineticInstance setColor(Color c) {
    	color(c.getRed(), c.getGreen(), c.getBlue());
    	return this;
    }

    public KineticInstance setRotationalSpeed(float rotationalSpeed) {
		this.rotationalSpeed = rotationalSpeed;
		return this;
	}

	public KineticInstance setRotationOffset(float rotationOffset) {
		this.rotationOffset = rotationOffset;
		return this;
	}

}
