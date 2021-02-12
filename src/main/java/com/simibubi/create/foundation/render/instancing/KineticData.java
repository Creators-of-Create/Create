package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.render.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.gl.attrib.VertexAttribSpec;
import com.simibubi.create.foundation.utility.ColorHelper;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.gl.attrib.VertexAttribSpec.*;

public class KineticData<D extends KineticData<D>> extends InstanceData {
    private float x;
    private float y;
    private float z;
    private byte blockLight;
    private byte skyLight;
    private byte r;
    private byte g;
    private byte b;
    private float rotationalSpeed;
    private float rotationOffset;

    public D setTileEntity(KineticTileEntity te) {
        setPosition(te.getPos());
        if (te.hasSource()) {
            setColor(te.network);
        }else {
            setColor(0xFF, 0xFF, 0x00);
        }
        return (D) this;
    }

    public D setPosition(BlockPos pos) {
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public D setPosition(Vector3f pos) {
        return setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public D setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return (D) this;
    }

    public D setBlockLight(int blockLight) {
        this.blockLight = (byte) ((blockLight & 0xF) << 4);
        return (D) this;
    }

    public D setSkyLight(int skyLight) {
        this.skyLight = (byte) ((skyLight & 0xF) << 4);
        return (D) this;
    }

    public D setColor(Long l) {
        if (l != null)
            return setColor(l.longValue());
        else
            return setColor(0xFF, 0xFF, 0xFF);
    }

    private D setColor(long l) {
        int color = ColorHelper.colorFromLong(l);
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) (color & 0xFF);
        return setColor(r, g, b);
    }

    public D setColor(int r, int g, int b) {
        return setColor((byte) r, (byte) g, (byte) b);
    }

    public D setColor(byte r, byte g, byte b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return (D) this;
    }

    public D setRotationalSpeed(float rotationalSpeed) {
        this.rotationalSpeed = rotationalSpeed;
        return (D) this;
    }

    public D setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        return (D) this;
    }


    @Override
    public void write(ByteBuffer buf) {
        putVec3(buf, x, y, z);
        putVec2(buf, blockLight, skyLight);
        putVec3(buf, r, g, b);
        put(buf, rotationalSpeed);
        put(buf, rotationOffset);
    }
}
