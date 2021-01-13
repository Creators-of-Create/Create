package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.SafeDirectBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public class BeltData extends BasicData<BeltData> {
    public static VertexFormat FORMAT = new VertexFormat(BasicData.FORMAT, VEC3, FLOAT, VEC2, VEC4, FLOAT);

    private float rotX;
    private float rotY;
    private float rotZ;
    private float rotationalSpeed;
    private float sourceU;
    private float sourceV;
    private float minU;
    private float minV;
    private float maxU;
    private float maxV;
    private float scrollMult;

    public BeltData setRotation(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        return this;
    }

    public BeltData setRotationalSpeed(float rotationalSpeed) {
        this.rotationalSpeed = rotationalSpeed;
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
        this.scrollMult = scrollMult;
        return this;
    }

    @Override
    public void write(SafeDirectBuffer buf) {
        super.write(buf);

        putVec3(buf, rotX, rotY, rotZ);

        putFloat(buf, rotationalSpeed);

        putVec2(buf, sourceU, sourceV);
        putVec4(buf, minU, minV, maxU, maxV);

        putFloat(buf, scrollMult);
    }
}
