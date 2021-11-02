package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.materials.BasicData;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public class KineticData extends BasicData {
    private float x;
    private float y;
    private float z;
    private float rotationalSpeed;
    private float rotationOffset;

    protected KineticData(Instancer<?> owner) {
        super(owner);
    }

    public KineticData setPosition(BlockPos pos) {
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public KineticData setPosition(Vector3f pos) {
        return setPosition(pos.x(), pos.y(), pos.z());
    }

    public KineticData setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        markDirty();
        return this;
    }

    public KineticData nudge(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        markDirty();
        return this;
    }

    public KineticData setColor(KineticTileEntity te) {
        if (te.hasNetwork()) {
            setColor(Color.generateFromLong(te.network));
        }else {
            setColor(0xFF, 0xFF, 0xFF);
        }
        return this;
    }

    public KineticData setColor(Color c) {
    	setColor(c.getRed(), c.getGreen(), c.getBlue());
    	return this;
    }

    public KineticData setRotationalSpeed(float rotationalSpeed) {
		this.rotationalSpeed = rotationalSpeed;
		return this;
	}

	public KineticData setRotationOffset(float rotationOffset) {
		this.rotationOffset = rotationOffset;
		return this;
	}

	@Override
	public void write(MappedBuffer buf) {
		super.write(buf);

		buf.putFloatArray(new float[]{
				x,
				y,
				z,
				rotationalSpeed,
				rotationOffset
		});
	}
}
