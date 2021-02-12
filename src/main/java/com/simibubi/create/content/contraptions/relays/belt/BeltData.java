package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.content.contraptions.base.KineticVertexAttributes;
import com.simibubi.create.content.contraptions.base.KineticData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.nio.ByteBuffer;

public class BeltData extends KineticData<BeltData> {
    public static VertexFormat FORMAT = VertexFormat.builder()
                                                    .addAttributes(KineticVertexAttributes.class)
                                                    .addAttributes(BeltVertexAttributes.class)
                                                    .build();

    private float rotX;
    private float rotY;
    private float rotZ;
    private float sourceU;
    private float sourceV;
    private float minU;
    private float minV;
    private float maxU;
    private float maxV;
    private byte scrollMult;

    public BeltData setRotation(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
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

        putVec3(buf, rotX, rotY, rotZ);

        putVec2(buf, sourceU, sourceV);
        putVec4(buf, minU, minV, maxU, maxV);

        put(buf, scrollMult);
    }
}
