package com.simibubi.create.foundation.render.instancing.actors;

import com.simibubi.create.foundation.render.instancing.InstanceData;
import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;
import net.minecraft.client.renderer.Vector3f;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.gl.attrib.CommonAttributes.NORMAL;

public class DynamicRotatingActorData extends InstanceData {
    public static VertexFormat FORMAT = new VertexFormat(NORMAL);

    private byte relativeMotionX;
    private byte relativeMotionY;
    private byte relativeMotionZ;

    public DynamicRotatingActorData setRelativeMotion(Vector3f axis) {
        setRelativeMotion(axis.getX(), axis.getY(), axis.getZ());
        return this;
    }

    public DynamicRotatingActorData setRelativeMotion(float relativeMotionX, float relativeMotionY, float relativeMotionZ) {
        this.relativeMotionX = (byte) (relativeMotionX * 127);
        this.relativeMotionY = (byte) (relativeMotionY * 127);
        this.relativeMotionZ = (byte) (relativeMotionZ * 127);
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        putVec3(buf, relativeMotionX, relativeMotionY, relativeMotionZ);
    }
}
