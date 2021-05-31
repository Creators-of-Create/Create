package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.materials.BasicData;
import com.simibubi.create.foundation.utility.ColorHelper;

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
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
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
        if (te.hasSource()) {
            setColor(te.network);
        }else {
            setColor(0xFF, 0xFF, 0x00);
        }
        return this;
    }

    public KineticData setColor(Long l) {
        if (l != null)
            return setColor(l.longValue());
        else {
            setColor(0xFF, 0xFF, 0xFF);
            return this;
        }
    }

    private KineticData setColor(long l) {
        int color = ColorHelper.colorFromLong(l);
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) (color & 0xFF);
        setColor(r, g, b);

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
