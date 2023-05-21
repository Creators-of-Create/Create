package com.simibubi.create.content.kinetics.base.flwdata;

import com.mojang.math.Quaternion;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BeltData extends KineticData {
    float qX;
    float qY;
    float qZ;
    float qW;
    float sourceU;
    float sourceV;
    float minU;
    float minV;
    float maxU;
    float maxV;
    byte scrollMult;

    public BeltData setRotation(Quaternion q) {
        this.qX = q.i();
        this.qY = q.j();
        this.qZ = q.k();
        this.qW = q.r();
        markDirty();
        return this;
    }

    public BeltData setScrollTexture(SpriteShiftEntry spriteShift) {
        TextureAtlasSprite source = spriteShift.getOriginal();
        TextureAtlasSprite target = spriteShift.getTarget();

        this.sourceU = source.getU0();
        this.sourceV = source.getV0();
        this.minU = target.getU0();
        this.minV = target.getV0();
        this.maxU = target.getU1();
        this.maxV = target.getV1();
        markDirty();

		return this;
	}

	public BeltData setScrollMult(float scrollMult) {
		this.scrollMult = (byte) (scrollMult * 127);
		markDirty();
		return this;
	}

}
