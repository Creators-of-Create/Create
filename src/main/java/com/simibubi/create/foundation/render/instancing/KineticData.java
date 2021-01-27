package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.ColorHelper;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public class KineticData<D extends KineticData<D>> extends InstanceData {
    public static final VertexAttribute ROTATION_CENTER = copy("rotationCenter", VEC3);
    public static final VertexAttribute SPEED = copy("speed", FLOAT);
    public static final VertexAttribute OFFSET = copy("offset", FLOAT);
    public static final VertexFormat FORMAT = new VertexFormat(INSTANCE_POSITION, LIGHT, RGB, SPEED, OFFSET);

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

    private void setColor(long l) {
        int color = ColorHelper.colorFromLong(l);
        r = (byte) ((color >> 16) & 0xFF);
        g = (byte) ((color >> 8) & 0xFF);
        b = (byte) (color & 0xFF);
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
