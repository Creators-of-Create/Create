package com.simibubi.create.content.contraptions.relays.belt;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.simibubi.create.content.contraptions.base.KineticData;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.math.Quaternion;

public class BeltData extends KineticData {
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

	@Override
	public void write(VecBuffer buf) {
		super.write(buf);

		buf.putVec4(qX, qY, qZ, qW);
		buf.putVec2(sourceU, sourceV);
		buf.putVec4(minU, minV, maxU, maxV);
		buf.put(scrollMult);
	}
}
