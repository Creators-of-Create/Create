package com.simibubi.create.foundation.utility.render.instancing;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class BasicData<D extends BasicData<D>> extends InstanceData {

    private float x;
    private float y;
    private float z;
    private float blockLight;
    private float skyLight;

    public D setBlockLight(int blockLight) {
        this.blockLight = blockLight / 15f;
        return (D) this;
    }

    public D setSkyLight(int skyLight) {
        this.skyLight = skyLight / 15f;
        return (D) this;
    }

    public D setPosition(Vector3f pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        return (D) this;
    }

    public D setPosition(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        return (D) this;
    }

    @Override
    public void write(ByteBuffer buf) {
        putVec3(buf, x, y, z);

        putVec2(buf, blockLight, skyLight);
    }
}
