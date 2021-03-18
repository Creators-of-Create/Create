package com.simibubi.create.content.contraptions.relays.belt;

import java.nio.ByteBuffer;

import com.simibubi.create.content.contraptions.base.KineticData;
import com.simibubi.create.content.contraptions.base.KineticVertexAttributes;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BeltData extends KineticData<BeltData> {
    public static VertexFormat FORMAT = VertexFormat.builder()
                                                    .addAttributes(KineticVertexAttributes.class)
                                                    .addAttributes(BeltVertexAttributes.class)
                                                    .build();

    private float qX;
    private float qY;
    private float qZ;
    private float qW;
    private float sourceU;
    private float sourceV;
    private float minU;
    private float minV;
    private float maxU;
    private float maxV;
    private byte scrollMult;

    protected BeltData(InstancedModel<?> owner) {
        super(owner);
    }

    public BeltData setRotation(Quaternion q) {
        this.qX = q.getX();
        this.qY = q.getY();
        this.qZ = q.getZ();
        this.qW = q.getW();
        return this;
    }

    public BeltData setScrollTexture(SpriteShiftEntry spriteShift) {
        TextureAtlasSprite source = spriteShift.getOriginal();
        TextureAtlasSprite target = spriteShift.getTarget();

        this.sourceU = source.getMinU();
        this.sourceV = source.getMinV();
        this.minU = target.getMinU();
        this.minV = target.getMinV();
        this.maxU = target.getMaxU();
        this.maxV = target.getMaxV();

        return this;
    }

    public BeltData setScrollMult(float scrollMult) {
        this.scrollMult = (byte) (scrollMult * 127);
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        super.write(buf);

        putVec4(buf, qX, qY, qZ, qW);

        putVec2(buf, sourceU, sourceV);
        putVec4(buf, minU, minV, maxU, maxV);

        put(buf, scrollMult);
    }
}
