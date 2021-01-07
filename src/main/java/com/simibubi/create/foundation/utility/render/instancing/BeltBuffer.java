package com.simibubi.create.foundation.utility.render.instancing;

import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.utility.render.LightUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import static com.simibubi.create.foundation.utility.render.instancing.VertexAttribute.*;

import java.nio.ByteBuffer;

public class BeltBuffer extends InstanceBuffer<BeltBuffer.BeltData> {
    public BeltBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected BeltData newInstance() {
        return new BeltData();
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return BeltData.FORMAT;
    }

    public static class BeltData extends InstanceData {
        public static VertexFormat FORMAT = new VertexFormat(VEC3, VEC3, VEC2, FLOAT, VEC2, VEC4, FLOAT);

        private float x;
        private float y;
        private float z;
        private float rotX;
        private float rotY;
        private float rotZ;
        private int packedLight;
        private float rotationalSpeed;
        private float sourceU;
        private float sourceV;
        private float minU;
        private float minV;
        private float maxU;
        private float maxV;
        private float scrollMult;

        public BeltData setPosition(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public BeltData setRotation(float rotX, float rotY, float rotZ) {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            return this;
        }

        public BeltData setPackedLight(int packedLight) {
            this.packedLight = packedLight;
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
        public void write(ByteBuffer buf) {
            float blockLightCoordinates = LightUtil.getProperBlockLight(packedLight);
            float skyLightCoordinates = LightUtil.getProperSkyLight(packedLight);

            putVec3(buf, x, y, z);

            putVec3(buf, rotX, rotY, rotZ);

            putVec2(buf, blockLightCoordinates, skyLightCoordinates);
            putFloat(buf, rotationalSpeed);

            putVec2(buf, sourceU, sourceV);
            putVec4(buf, minU, minV, maxU, maxV);

            putFloat(buf, scrollMult);
        }
    }
}
